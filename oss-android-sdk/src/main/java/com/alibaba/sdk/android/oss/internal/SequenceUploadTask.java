package com.alibaba.sdk.android.oss.internal;

import android.text.TextUtils;

import com.alibaba.sdk.android.oss.ClientException;
import com.alibaba.sdk.android.oss.ServiceException;
import com.alibaba.sdk.android.oss.TaskCancelException;
import com.alibaba.sdk.android.oss.callback.OSSCompletedCallback;
import com.alibaba.sdk.android.oss.common.OSSLog;
import com.alibaba.sdk.android.oss.common.utils.BinaryUtil;
import com.alibaba.sdk.android.oss.common.utils.CRC64;
import com.alibaba.sdk.android.oss.common.utils.OSSSharedPreferences;
import com.alibaba.sdk.android.oss.common.utils.OSSUtils;
import com.alibaba.sdk.android.oss.model.AbortMultipartUploadRequest;
import com.alibaba.sdk.android.oss.model.CompleteMultipartUploadResult;
import com.alibaba.sdk.android.oss.model.InitiateMultipartUploadRequest;
import com.alibaba.sdk.android.oss.model.InitiateMultipartUploadResult;
import com.alibaba.sdk.android.oss.model.ListPartsRequest;
import com.alibaba.sdk.android.oss.model.ListPartsResult;
import com.alibaba.sdk.android.oss.model.PartETag;
import com.alibaba.sdk.android.oss.model.PartSummary;
import com.alibaba.sdk.android.oss.model.ResumableUploadRequest;
import com.alibaba.sdk.android.oss.model.ResumableUploadResult;
import com.alibaba.sdk.android.oss.model.UploadPartRequest;
import com.alibaba.sdk.android.oss.model.UploadPartResult;
import com.alibaba.sdk.android.oss.network.ExecutionContext;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.zip.CheckedInputStream;

/**
 * Created by jingdan on 2017/10/30.
 */

public class SequenceUploadTask extends BaseMultipartUploadTask<ResumableUploadRequest,
        ResumableUploadResult> implements Callable<ResumableUploadResult> {

    private File mRecordFile;
    private List<Integer> mAlreadyUploadIndex = new ArrayList<Integer>();
    private long mFirstPartSize;
    private OSSSharedPreferences mSp;
    private File mCRC64RecordFile;

    public SequenceUploadTask(ResumableUploadRequest request,
                              OSSCompletedCallback<ResumableUploadRequest, ResumableUploadResult> completedCallback,
                              ExecutionContext context, InternalRequestOperation apiOperation) {
        super(apiOperation, request, completedCallback, context);
        mSp = OSSSharedPreferences.instance(mContext.getApplicationContext());
    }

    @Override
    protected void initMultipartUploadId() throws IOException, ClientException, ServiceException {

        Map<Integer, Long> recordCrc64 = null;

        if (!OSSUtils.isEmptyString(mRequest.getRecordDirectory())) {
            String fileMd5 = BinaryUtil.calculateMd5Str(mUploadFilePath);
            String recordFileName = BinaryUtil.calculateMd5Str((fileMd5 + mRequest.getBucketName()
                    + mRequest.getObjectKey() + String.valueOf(mRequest.getPartSize()) + (mCheckCRC64 ? "-crc64" : "") + "-sequence").getBytes());
            String recordPath = mRequest.getRecordDirectory() + File.separator + recordFileName;

            mRecordFile = new File(recordPath);
            if (mRecordFile.exists()) {
                BufferedReader br = new BufferedReader(new FileReader(mRecordFile));
                mUploadId = br.readLine();
                br.close();
                OSSLog.logDebug("sequence [initUploadId] - Found record file, uploadid: " + mUploadId);
            }

            if (!OSSUtils.isEmptyString(mUploadId)) {
                if (mCheckCRC64) {
                    String filePath = mRequest.getRecordDirectory() + File.separator + mUploadId;
                    File crc64Record = new File(filePath);
                    if (crc64Record.exists()) {
                        FileInputStream fs = new FileInputStream(crc64Record);//创建文件字节输出流对象
                        ObjectInputStream ois = new ObjectInputStream(fs);

                        try {
                            recordCrc64 = (Map<Integer, Long>) ois.readObject();
                            crc64Record.delete();
                        } catch (ClassNotFoundException e) {
                            OSSLog.logThrowable2Local(e);
                        } finally {
                            if (ois != null)
                                ois.close();
                            crc64Record.delete();
                        }
                    }
                }

                boolean isTruncated = false;
                int nextPartNumberMarker = 0;

                do {
                    ListPartsRequest listParts = new ListPartsRequest(mRequest.getBucketName(), mRequest.getObjectKey(), mUploadId);
                    if (nextPartNumberMarker > 0){
                        listParts.setPartNumberMarker(nextPartNumberMarker);
                    }

                    OSSAsyncTask<ListPartsResult> task = mApiOperation.listParts(listParts, null);

                    try {

                        ListPartsResult result = task.getResult();
                        isTruncated = result.isTruncated();
                        nextPartNumberMarker = result.getNextPartNumberMarker();
                        List<PartSummary> parts = result.getParts();

                        for (int i = 0; i < parts.size(); i++) {
                            PartSummary part = parts.get(i);
                            PartETag partETag = new PartETag(part.getPartNumber(), part.getETag());
                            partETag.setPartSize(part.getSize());

                            if (recordCrc64 != null && recordCrc64.size() > 0) {
                                if (recordCrc64.containsKey(partETag.getPartNumber())) {
                                    partETag.setCRC64(recordCrc64.get(partETag.getPartNumber()));
                                }
                            }

                            mPartETags.add(partETag);
                            mUploadedLength += part.getSize();
                            mAlreadyUploadIndex.add(part.getPartNumber());
                            if (i == 0) {
                                mFirstPartSize = part.getSize();
                            }
                        }
                    } catch (ServiceException e) {
                        isTruncated = false;
                        if (e.getStatusCode() == 404) {
                            mUploadId = null;
                        } else {
                            throw e;
                        }
                    } catch (ClientException e) {
                        isTruncated = false;
                        throw e;
                    }
                    task.waitUntilFinished();

                }while (isTruncated);
            }

            if (!mRecordFile.exists() && !mRecordFile.createNewFile()) {
                throw new ClientException("Can't create file at path: " + mRecordFile.getAbsolutePath()
                        + "\nPlease make sure the directory exist!");
            }
        }

        if (OSSUtils.isEmptyString(mUploadId)) {
            InitiateMultipartUploadRequest init = new InitiateMultipartUploadRequest(
                    mRequest.getBucketName(), mRequest.getObjectKey(), mRequest.getMetadata());
            init.isSequential = true;
            InitiateMultipartUploadResult initResult = mApiOperation.initMultipartUpload(init, null).getResult();

            mUploadId = initResult.getUploadId();

            if (mRecordFile != null) {
                BufferedWriter bw = new BufferedWriter(new FileWriter(mRecordFile));
                bw.write(mUploadId);
                bw.close();
            }
        }

        mRequest.setUploadId(mUploadId);
    }

    @Override
    protected ResumableUploadResult doMultipartUpload() throws IOException, ClientException, ServiceException, InterruptedException {

        long tempUploadedLength = mUploadedLength;

        checkCancel();

//        int[] mPartAttr = new int[2];
//        checkPartSize(mPartAttr);

        int readByte = mPartAttr[0];
        final int partNumber = mPartAttr[1];

        if (mPartETags.size() > 0 && mAlreadyUploadIndex.size() > 0) { //revert progress
            if (mUploadedLength > mFileLength) {
                throw new ClientException("The uploading file is inconsistent with before");
            }

            if (mFirstPartSize != readByte) {
                throw new ClientException("The part size setting is inconsistent with before");
            }

            long revertUploadedLength = mUploadedLength;

            if (!TextUtils.isEmpty(mSp.getStringValue(mUploadId))) {
                revertUploadedLength = Long.valueOf(mSp.getStringValue(mUploadId));
            }

            if (mProgressCallback != null) {
                mProgressCallback.onProgress(mRequest, revertUploadedLength, mFileLength);
            }

            mSp.removeKey(mUploadId);
        }

        for (int i = 0; i < partNumber; i++) {

            if (mAlreadyUploadIndex.size() != 0 && mAlreadyUploadIndex.contains(i + 1)) {
                continue;
            }

            //need read byte
            if (i == partNumber - 1) {
                readByte = (int) (mFileLength - tempUploadedLength);
            }
            OSSLog.logDebug("upload part readByte : " + readByte);
            int byteCount = readByte;
            int readIndex = i;
            tempUploadedLength += byteCount;
            uploadPart(readIndex, byteCount, partNumber);
            //break immediately for sequence upload
            if (mUploadException != null){
                break;
            }
        }

        checkException();
        //complete sort
        CompleteMultipartUploadResult completeResult = completeMultipartUploadResult();
        ResumableUploadResult result = null;
        if (completeResult != null) {
            result = new ResumableUploadResult(completeResult);
        }
        if (mRecordFile != null) {
            mRecordFile.delete();
        }
        if (mCRC64RecordFile != null) {
            mCRC64RecordFile.delete();
        }
        return result;
    }

    public void uploadPart(int readIndex, int byteCount, int partNumber) {

        RandomAccessFile raf = null;
        UploadPartRequest uploadPartRequest = null;
        try {

            if (mContext.getCancellationHandler().isCancelled()) {
                return;
            }

            mRunPartTaskCount++;

            preUploadPart(readIndex, byteCount, partNumber);

            raf = new RandomAccessFile(mUploadFile, "r");

            uploadPartRequest = new UploadPartRequest(
                    mRequest.getBucketName(), mRequest.getObjectKey(), mUploadId, readIndex + 1);
            long skip = readIndex * mRequest.getPartSize();
            byte[] partContent = new byte[byteCount];
            raf.seek(skip);
            raf.readFully(partContent, 0, byteCount);
            uploadPartRequest.setPartContent(partContent);
            uploadPartRequest.setMd5Digest(BinaryUtil.calculateBase64Md5(partContent));
            uploadPartRequest.setCRC64(mRequest.getCRC64());
            UploadPartResult uploadPartResult = mApiOperation.syncUploadPart(uploadPartRequest);
            //check isComplete，throw exception when error occur
            PartETag partETag = new PartETag(uploadPartRequest.getPartNumber(), uploadPartResult.getETag());
            partETag.setPartSize(byteCount);
            if (mCheckCRC64) {
                partETag.setCRC64(uploadPartResult.getClientCRC());
            }

            mPartETags.add(partETag);
            mUploadedLength += byteCount;

            uploadPartFinish(partETag);

            if (mContext.getCancellationHandler().isCancelled()) {
                //cancel immediately for sequence upload
                TaskCancelException e = new TaskCancelException("sequence upload task cancel");
                throw new ClientException(e.getMessage(), e, true);
            } else {
                onProgressCallback(mRequest, mUploadedLength, mFileLength);
            }
        } catch (ServiceException e) {
            // it is not necessary to throw 409 PartAlreadyExist exception out
            if (e.getStatusCode() != 409) {
                processException(e);
            } else {
                PartETag partETag = new PartETag(uploadPartRequest.getPartNumber(), e.getPartEtag());
                partETag.setPartSize(uploadPartRequest.getPartContent().length);
                if (mCheckCRC64) {
                    byte[] partContent = uploadPartRequest.getPartContent();
                    ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(partContent);
                    CheckedInputStream checkedInputStream = new CheckedInputStream(byteArrayInputStream, new CRC64());

                    partETag.setCRC64(checkedInputStream.getChecksum().getValue());
                }

                mPartETags.add(partETag);
                mUploadedLength += byteCount;
            }
        } catch (Exception e) {
            processException(e);
        } finally {
            try {
                if (raf != null)
                    raf.close();
            } catch (IOException e) {
                OSSLog.logThrowable2Local(e);
            }
        }
    }


    @Override
    protected void checkException() throws IOException, ServiceException, ClientException {
        if (mContext.getCancellationHandler().isCancelled()) {
            if (mRequest.deleteUploadOnCancelling()) {
                abortThisUpload();
                if (mRecordFile != null) {
                    mRecordFile.delete();
                }
            } else {
                if (mPartETags != null && mPartETags.size() > 0 && mCheckCRC64 && mRequest.getRecordDirectory() != null) {
                    Map<Integer, Long> maps = new HashMap<Integer, Long>();
                    for (PartETag eTag : mPartETags) {
                        maps.put(eTag.getPartNumber(), eTag.getCRC64());
                    }
                    ObjectOutputStream oot = null;
                    try {
                        String filePath = mRequest.getRecordDirectory() + File.separator + mUploadId;
                        mCRC64RecordFile = new File(filePath);
                        if (!mCRC64RecordFile.exists()) {
                            mCRC64RecordFile.createNewFile();
                        }
                        oot = new ObjectOutputStream(new FileOutputStream(mCRC64RecordFile));
                        oot.writeObject(maps);
                    } catch (IOException e) {
                        OSSLog.logThrowable2Local(e);
                    } finally {
                        if (oot != null) {
                            oot.close();
                        }
                    }
                }
            }
        }
        super.checkException();
    }

    @Override
    protected void abortThisUpload() {
        if (mUploadId != null) {
            AbortMultipartUploadRequest abort = new AbortMultipartUploadRequest(
                    mRequest.getBucketName(), mRequest.getObjectKey(), mUploadId);
            mApiOperation.abortMultipartUpload(abort, null).waitUntilFinished();
        }
    }

    @Override
    protected void processException(Exception e) {
//        mPartExceptionCount++;
        if (mUploadException == null || !e.getMessage().equals(mUploadException.getMessage())) {
            mUploadException = e;
        }
        OSSLog.logThrowable2Local(e);
        if (mContext.getCancellationHandler().isCancelled()) {
            if (!mIsCancel) {
                mIsCancel = true;
            }
        }
    }

    @Override
    protected void uploadPartFinish(PartETag partETag) throws Exception {
        if (mContext.getCancellationHandler().isCancelled()) {
            if (!mSp.contains(mUploadId)) {
                mSp.setStringValue(mUploadId, String.valueOf(mUploadedLength));
                onProgressCallback(mRequest, mUploadedLength, mFileLength);
            }
        }
    }
}
