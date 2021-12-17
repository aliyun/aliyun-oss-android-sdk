package com.alibaba.sdk.android.oss.internal;

import android.util.Log;

import com.alibaba.sdk.android.oss.ClientException;
import com.alibaba.sdk.android.oss.ServiceException;
import com.alibaba.sdk.android.oss.TaskCancelException;
import com.alibaba.sdk.android.oss.callback.OSSCompletedCallback;
import com.alibaba.sdk.android.oss.callback.OSSProgressCallback;
import com.alibaba.sdk.android.oss.common.OSSLog;
import com.alibaba.sdk.android.oss.common.utils.BinaryUtil;
import com.alibaba.sdk.android.oss.common.utils.CRC64;
import com.alibaba.sdk.android.oss.common.utils.OSSUtils;
import com.alibaba.sdk.android.oss.exception.InconsistentException;
import com.alibaba.sdk.android.oss.model.ResumableDownloadResult;
import com.alibaba.sdk.android.oss.model.GetObjectRequest;
import com.alibaba.sdk.android.oss.model.GetObjectResult;
import com.alibaba.sdk.android.oss.model.HeadObjectRequest;
import com.alibaba.sdk.android.oss.model.HeadObjectResult;
import com.alibaba.sdk.android.oss.model.ResumableDownloadRequest;
import com.alibaba.sdk.android.oss.model.OSSRequest;
import com.alibaba.sdk.android.oss.model.OSSResult;
import com.alibaba.sdk.android.oss.model.ObjectMetadata;
import com.alibaba.sdk.android.oss.model.Range;
import com.alibaba.sdk.android.oss.network.ExecutionContext;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.zip.CheckedInputStream;

public class ResumableDownloadTask<Requst extends ResumableDownloadRequest,
        Result extends ResumableDownloadResult> implements Callable<Result> {
    protected final int CPU_SIZE = Runtime.getRuntime().availableProcessors() * 2;
    protected final int MAX_CORE_POOL_SIZE = CPU_SIZE < 5 ? CPU_SIZE : 5;
    protected final int MAX_IMUM_POOL_SIZE = CPU_SIZE;
    protected final int KEEP_ALIVE_TIME = 3000;
    protected final int MAX_QUEUE_SIZE = 5000;
    protected ThreadPoolExecutor mPoolExecutor =
            new ThreadPoolExecutor(MAX_CORE_POOL_SIZE, MAX_IMUM_POOL_SIZE, KEEP_ALIVE_TIME,
                    TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(MAX_QUEUE_SIZE), new ThreadFactory() {
                @Override
                public Thread newThread(Runnable runnable) {
                    return new Thread(runnable, "oss-android-multipart-thread");
                }
            });
    private ResumableDownloadRequest mRequest;
    private InternalRequestOperation mOperation;
    private OSSCompletedCallback mCompletedCallback;
    private ExecutionContext mContext;
    private OSSProgressCallback mProgressCallback;
    private CheckPoint mCheckPoint;
    protected Object mLock = new Object();
    protected Exception mDownloadException;
    protected long completedPartSize;
    protected long downloadPartSize;
    protected long mPartExceptionCount;
    protected String checkpointPath;

    ResumableDownloadTask(InternalRequestOperation operation,
                          ResumableDownloadRequest request,
                          OSSCompletedCallback completedCallback,
                          ExecutionContext context) {
        this.mRequest = request;
        this.mOperation = operation;
        this.mCompletedCallback = completedCallback;
        this.mContext = context;
        this.mProgressCallback = request.getProgressListener();
    }


    @Override
    public Result call() throws Exception {
        try {
            checkInitData();
            ResumableDownloadResult result = doMultipartDownload();
            if (mCompletedCallback != null) {
                mCompletedCallback.onSuccess(mRequest, result);
            }
            return (Result) result;
        } catch (ServiceException e) {
            if (mCompletedCallback != null) {
                mCompletedCallback.onFailure(mRequest, null, e);
            }
            throw e;
        } catch (Exception e) {
            ClientException temp;
            if (e instanceof ClientException) {
                temp = (ClientException) e;
            } else {
                temp = new ClientException(e.toString(), e);
            }
            if (mCompletedCallback != null) {
                mCompletedCallback.onFailure(mRequest, temp, null);
            }
            throw temp;
        }
    }

    protected void checkInitData() throws ClientException, ServiceException, IOException {

        if (mRequest.getRange() != null && !mRequest.getRange().checkIsValid()) {
            throw new ClientException("Range is invalid");
        };
        String recordFileName = BinaryUtil.calculateMd5Str((mRequest.getBucketName() + mRequest.getObjectKey()
                + String.valueOf(mRequest.getPartSize()) + (mRequest.getCRC64() == OSSRequest.CRC64Config.YES ? "-crc64" : "")).getBytes());
        checkpointPath = mRequest.getCheckPointFilePath() + File.separator + recordFileName;

        mCheckPoint = new CheckPoint();

        if (mRequest.getEnableCheckPoint()) {
            try {
                mCheckPoint.load(checkpointPath);
            } catch (Exception e) {
                removeFile(checkpointPath);
                removeFile(mRequest.getTempFilePath());
            }
            if (!mCheckPoint.isValid(mOperation)) {
                removeFile(checkpointPath);
                removeFile(mRequest.getTempFilePath());

                initCheckPoint();
            }
        } else {
            initCheckPoint();
        }
    }

    protected boolean removeFile(String filePath) {
        boolean flag = false;
        File file = new File(filePath);

        if (file.isFile() && file.exists()) {
            flag = file.delete();
        }

        return flag;
    }

    private void initCheckPoint() throws ClientException, ServiceException, IOException {
        FileStat fileStat = FileStat.getFileStat(mOperation, mRequest.getBucketName(), mRequest.getObjectKey());
        Range range = correctRange(mRequest.getRange(), fileStat.fileLength);
        long downloadSize = range.getEnd() - range.getBegin();
        createFile(mRequest.getTempFilePath(), downloadSize);

        mCheckPoint.bucketName = mRequest.getBucketName();
        mCheckPoint.objectKey = mRequest.getObjectKey();
        mCheckPoint.fileStat = fileStat;
        mCheckPoint.parts = splitFile(range, mCheckPoint.fileStat.fileLength, mRequest.getPartSize());
    }

    protected ResumableDownloadResult doMultipartDownload() throws ClientException, ServiceException, IOException, InterruptedException {
        checkCancel();
        ResumableDownloadResult resumableDownloadResult = new ResumableDownloadResult();

        final DownloadFileResult result = new DownloadFileResult();
        result.partResults = new ArrayList<DownloadPartResult>();

        for (final DownloadPart part : mCheckPoint.parts) {
            checkException();
            if (mPoolExecutor != null && !part.isCompleted) {
                mPoolExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        downloadPart(result, part);
                        Log.i("partResults", "start: " + part.start + ", end: " + part.end);
                    }
                });
            } else {
                DownloadPartResult partResult = new DownloadPartResult();
                partResult.partNumber = part.partNumber;
                partResult.requestId = mCheckPoint.fileStat.requestId;
                partResult.length = part.length;
                if (mRequest.getCRC64() == OSSRequest.CRC64Config.YES) {
                    partResult.clientCRC = part.crc;
                }
                result.partResults.add(partResult);
                downloadPartSize += 1;
                completedPartSize += 1;
            }
        }
        // Wait for all tasks to be completed
        if (checkWaitCondition(mCheckPoint.parts.size())) {
            synchronized (mLock) {
                mLock.wait();
            }
        }
        checkException();
        Collections.sort(result.partResults, new Comparator<DownloadPartResult>() {
            @Override
            public int compare(DownloadPartResult downloadPartResult, DownloadPartResult t1) {
                return downloadPartResult.partNumber - t1.partNumber;
            }
        });
        if (mRequest.getCRC64() == OSSRequest.CRC64Config.YES && mRequest.getRange() == null) {
            Long clientCRC = calcObjectCRCFromParts(result.partResults);
            resumableDownloadResult.setClientCRC(clientCRC);
            try {
                OSSUtils.checkChecksum(clientCRC, mCheckPoint.fileStat.serverCRC, result.partResults.get(0).requestId);
            } catch (InconsistentException e) {
                removeFile(checkpointPath);
                removeFile(mRequest.getTempFilePath());
                throw e;
            }
        }
        removeFile(checkpointPath);

        File fromFile = new File(mRequest.getTempFilePath());
        File toFile = new File(mRequest.getDownloadToFilePath());
        moveFile(fromFile, toFile);

        resumableDownloadResult.setServerCRC(mCheckPoint.fileStat.serverCRC);
        resumableDownloadResult.setMetadata(result.metadata);
        resumableDownloadResult.setRequestId(result.partResults.get(0).requestId);
        resumableDownloadResult.setStatusCode(200);

        return resumableDownloadResult;
    }

    private static Long calcObjectCRCFromParts(List<DownloadPartResult> partResults) {
        long crc = 0;

        for (DownloadPartResult partResult : partResults) {
            if (partResult.clientCRC == null || partResult.length <= 0) {
                return null;
            }
            crc = CRC64.combine(crc, partResult.clientCRC, partResult.length);
        }
        return new Long(crc);
    }

    private ArrayList<DownloadPart> splitFile(Range range, long fileSize, long partSize) {

        if (fileSize <= 0) {
            DownloadPart part = new DownloadPart();
            part.start = 0;
            part.end = -1;
            part.length = 0;
            part.partNumber = 0;

            ArrayList<DownloadPart> parts = new ArrayList<DownloadPart>();
            parts.add(part);
            return parts;
        }
        long start = range.getBegin();
        long size = range.getEnd() - range.getBegin();

        long count = size / partSize;
        if (size % partSize > 0) {
            count += 1;
        }

        ArrayList<DownloadPart> parts = new ArrayList<DownloadPart>();
        for (int i = 0; i < count; i++) {
            DownloadPart part = new DownloadPart();
            part.start = start + partSize * i;
            part.end = start + partSize * (i + 1) - 1;
            part.length = part.end - part.start + 1;
            if (part.end >= start + size) {
                part.end = -1;
                part.length = start + size - part.start;
            }
            part.partNumber = i;
            part.fileStart = i * partSize;
            parts.add(part);
        }
        return parts;
    }

    private Range correctRange(Range range, long totalSize) {
        long start = 0;
        long size = totalSize;
        if (range != null) {
            start = range.getBegin();
            if (range.getBegin() == -1) {
                start = 0;
            }
            size = range.getEnd() - range.getBegin();
            if (range.getEnd() == -1) {
                size = totalSize - start;
            }
        }
        return new Range(start, start + size);
    }

    private void downloadPart(DownloadFileResult downloadResult, DownloadPart part) {

        RandomAccessFile output = null;
        InputStream content = null;
        try {

            if (mContext.getCancellationHandler().isCancelled()) {
                mPoolExecutor.getQueue().clear();
            }

            downloadPartSize += 1;

            output = new RandomAccessFile(mRequest.getTempFilePath(), "rw");
            output.seek(part.fileStart);

            Map<String, String> requestHeader = mRequest.getRequestHeader();

            GetObjectRequest request = new GetObjectRequest(mRequest.getBucketName(), mRequest.getObjectKey());
            request.setRange(new Range(part.start, part.end));
            request.setRequestHeaders(requestHeader);
            GetObjectResult result =  mOperation.getObject(request, null).getResult();

            content = result.getObjectContent();

            byte[] buffer = new byte[(int)(part.length)];
            long readLength = 0;
            if (mRequest.getCRC64() == OSSRequest.CRC64Config.YES) {
                content = new CheckedInputStream(content, new CRC64());
            }

            while ((readLength = content.read(buffer)) != -1) {
                output.write(buffer, 0, (int) readLength);
            }

            synchronized (mLock) {

                DownloadPartResult partResult = new DownloadPartResult();
                partResult.partNumber = part.partNumber;
                partResult.requestId = result.getRequestId();
                partResult.length = result.getContentLength();
                if (mRequest.getCRC64() == OSSRequest.CRC64Config.YES) {
                    Long clientCRC = ((CheckedInputStream)content).getChecksum().getValue();
                    partResult.clientCRC = clientCRC;

                    part.crc = clientCRC;
                }
                downloadResult.partResults.add(partResult);
                if (downloadResult.metadata == null) {
                    downloadResult.metadata = result.getMetadata();
                }

                completedPartSize += 1;

                if (mContext.getCancellationHandler().isCancelled()) {
                    // Cancel after the last task is completed
                    if (downloadPartSize == completedPartSize - mPartExceptionCount) {
                        checkCancel();
                    }
                } else {
                    // After all tasks are completed, wake up the thread where the doMultipartDownload method is located
                    if (mCheckPoint.parts.size() == (completedPartSize - mPartExceptionCount)) {
                        notifyMultipartThread();
                    }
                    mCheckPoint.update(part.partNumber, true);
                    if (mRequest.getEnableCheckPoint()) {
                        mCheckPoint.dump(checkpointPath);
                    }
                    Range range = correctRange(mRequest.getRange(), mCheckPoint.fileStat.fileLength);
                    if (mProgressCallback != null) {
                        mProgressCallback.onProgress(mRequest, mCheckPoint.downloadLength, range.getEnd() - range.getBegin());
                    }
                }
            }
        } catch (Exception e) {
            processException(e);
        } finally {
            try {
                if (output != null) {
                    output.close();
                }
                if (content != null) {
                    content.close();
                }
            } catch (IOException e) {
                OSSLog.logThrowable2Local(e);
            }
        }
    }

    private void createFile(String filePath, long length) throws IOException {
        File file = new File(filePath);
        RandomAccessFile accessFile = null;

        try {
            accessFile = new RandomAccessFile(file, "rw");
            accessFile.setLength(length);
        } finally {
            if (accessFile != null) {
                accessFile.close();
            }
        }
    }

    private void moveFile(File fromFile, File toFile) throws IOException {

        boolean rename = fromFile.renameTo(toFile);
        if (!rename) {
            Log.i("moveFile", "rename");
            InputStream ist = null;
            OutputStream ost = null;
            try {
                ist = new FileInputStream(fromFile);
                ost = new FileOutputStream(toFile);
                copyFile(ist, ost);
                if (!fromFile.delete()) {
                    throw new IOException("Failed to delete original file '" + fromFile + "'");
                }
            } catch (FileNotFoundException e) {
                throw e;
            } finally {
                if (ist != null) {
                    ist.close();
                }
                if (ost != null) {
                    ost.close();
                }
            }
        }
    }

    private void copyFile(InputStream ist, OutputStream ost) throws IOException {
        byte[] buffer = new byte[4096];
        int byteCount;
        while ((byteCount = ist.read(buffer)) != -1) {
            ost.write(buffer, 0, byteCount);
        }
    }

    protected void notifyMultipartThread() {
        mLock.notify();
        mPartExceptionCount = 0;
    }

    protected void processException(Exception e) {
        synchronized (mLock) {
            mPartExceptionCount++;
            if (mDownloadException == null) {
                mDownloadException = e;
                mLock.notify();
            }
        }
    }

    protected void releasePool() {
        if (mPoolExecutor != null) {
            mPoolExecutor.getQueue().clear();
            mPoolExecutor.shutdown();
        }
    }

    protected void checkException() throws IOException, ServiceException, ClientException {
        if (mDownloadException != null) {
            releasePool();
            if (mDownloadException instanceof IOException) {
                throw (IOException) mDownloadException;
            } else if (mDownloadException instanceof ServiceException) {
                throw (ServiceException) mDownloadException;
            } else if (mDownloadException instanceof ClientException) {
                throw (ClientException) mDownloadException;
            } else {
                ClientException clientException =
                        new ClientException(mDownloadException.getMessage(), mDownloadException);
                throw clientException;
            }
        }
    }

    protected boolean checkWaitCondition(int partNum) {
        if (completedPartSize == partNum) {
            return false;
        }
        return true;
    }

    protected void checkCancel() throws ClientException {
        if (mContext.getCancellationHandler().isCancelled()) {
            TaskCancelException e = new TaskCancelException("Resumable download cancel");
            throw new ClientException(e.getMessage(), e, true);
        }
    }

    static class DownloadPart implements Serializable {
        private static final long serialVersionUID = -3506020776131733942L;

        public int partNumber;
        public long start; // start index;
        public long end; // end index;
        public boolean isCompleted; // flag of part download finished or not;
        public long length; // length of part
        public long fileStart; // start index of file
        public long crc; // part crc.

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + partNumber;
            result = prime * result + (isCompleted ? 1231 : 1237);
            result = prime * result + (int) (end ^ (end >>> 32));
            result = prime * result + (int) (start ^ (start >>> 32));
            result = prime * result + (int) (crc ^ (crc >>> 32));
            return result;
        }
    }

    static class CheckPoint implements Serializable {

        private static final long serialVersionUID = -8470273912385636504L;

        public int md5;
        public String downloadFile;
        public String bucketName;
        public String objectKey;
        public FileStat fileStat;
        public ArrayList<DownloadPart> parts;
        public long downloadLength;

        /**
         * Loads the checkpoint data from the checkpoint file.
         */
        public synchronized void load(String cpFile) throws IOException, ClassNotFoundException {
            FileInputStream fileIn = null;
            ObjectInputStream in = null;
            try {
                fileIn = new FileInputStream(cpFile);
                in = new ObjectInputStream(fileIn);
                CheckPoint dcp = (CheckPoint) in.readObject();
                assign(dcp);
            } finally {
                if (in != null) {
                    in.close();
                }
                if (fileIn != null) {
                    fileIn.close();
                }
            }
        }

        /**
         * Writes the checkpoint data to the checkpoint file.
         */
        public synchronized void dump(String cpFile) throws IOException {
            this.md5 = hashCode();
            FileOutputStream fileOut = null;
            ObjectOutputStream outStream = null;
            try {
                fileOut = new FileOutputStream(cpFile);
                outStream = new ObjectOutputStream(fileOut);
                outStream.writeObject(this);
            } finally {
                if (outStream != null) {
                    outStream.close();
                }
                if (fileOut != null) {
                    fileOut.close();
                }
            }
        }

        /**
         * Updates the part's download status.
         *
         * @throws IOException
         */
        public synchronized void update(int index, boolean completed) throws IOException {
            parts.get(index).isCompleted = completed;
            downloadLength += parts.get(index).length;
        }

        /**
         * Check if the object matches the checkpoint information.
         */
        public synchronized boolean isValid(InternalRequestOperation operation) throws ClientException, ServiceException {
            // Compare magic and md5 of checkpoint
            if (this.md5 != hashCode()) {
                return false;
            }

            FileStat fileStat = FileStat.getFileStat(operation, bucketName, objectKey);

            // Object's size, last modified time or ETAG are not same as the one
            // in the checkpoint.
            if (this.fileStat.lastModified == null) {
                if (this.fileStat.fileLength != fileStat.fileLength || !this.fileStat.etag.equals(fileStat.etag)) {
                    return false;
                }
            } else {
                if (this.fileStat.fileLength != fileStat.fileLength || !this.fileStat.lastModified.equals(fileStat.lastModified)
                        || !this.fileStat.etag.equals(fileStat.etag)) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((bucketName == null) ? 0 : bucketName.hashCode());
            result = prime * result + ((downloadFile == null) ? 0 : downloadFile.hashCode());
            result = prime * result + ((objectKey == null) ? 0 : objectKey.hashCode());
            result = prime * result + ((fileStat == null) ? 0 : fileStat.hashCode());
            result = prime * result + ((parts == null) ? 0 : parts.hashCode());
            result = prime * result + (int) (downloadLength ^ (downloadLength >>> 32));
            return result;
        }

        private void assign(CheckPoint dcp) {
            this.md5 = dcp.md5;
            this.downloadFile = dcp.downloadFile;
            this.bucketName = dcp.bucketName;
            this.objectKey = dcp.objectKey;
            this.fileStat = dcp.fileStat;
            this.parts = dcp.parts;
            this.downloadLength = dcp.downloadLength;
        }
    }

    static class FileStat implements Serializable {

        private static final long serialVersionUID = 3896323364904643963L;

        public long fileLength;
        public String md5;
        public Date lastModified;
        public String etag;
        public Long serverCRC;
        public String requestId;

        public static FileStat getFileStat(InternalRequestOperation operation, String bucketName, String objectKey) throws ClientException, ServiceException {
            HeadObjectRequest request = new HeadObjectRequest(bucketName, objectKey);
            HeadObjectResult result = operation.headObject(request, null).getResult();

            FileStat fileStat = new FileStat();
            fileStat.fileLength = result.getMetadata().getContentLength();
            fileStat.etag = result.getMetadata().getETag();
            fileStat.lastModified = result.getMetadata().getLastModified();
            fileStat.serverCRC = result.getServerCRC();
            fileStat.requestId = result.getRequestId();

            return fileStat;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((etag == null) ? 0 : etag.hashCode());
            result = prime * result + ((lastModified == null) ? 0 : lastModified.hashCode());
            result = prime * result + (int) (fileLength ^ (fileLength >>> 32));
            return result;
        }
    }

    static class DownloadPartResult {

        public int partNumber;
        public String requestId;
        public Long clientCRC;
        public long length;
    }

    class DownloadFileResult extends OSSResult {

        public ArrayList<DownloadPartResult> partResults;
        public ObjectMetadata metadata;
    }
}
