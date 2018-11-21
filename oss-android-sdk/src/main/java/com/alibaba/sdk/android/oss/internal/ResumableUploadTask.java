package com.alibaba.sdk.android.oss.internal;

import android.text.TextUtils;

import com.alibaba.sdk.android.oss.ClientException;
import com.alibaba.sdk.android.oss.ServiceException;
import com.alibaba.sdk.android.oss.callback.OSSCompletedCallback;
import com.alibaba.sdk.android.oss.common.OSSLog;
import com.alibaba.sdk.android.oss.common.utils.BinaryUtil;
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
import com.alibaba.sdk.android.oss.network.ExecutionContext;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * Created by jingdan on 2017/10/30.
 */

public class ResumableUploadTask extends BaseMultipartUploadTask<ResumableUploadRequest,
        ResumableUploadResult> implements Callable<ResumableUploadResult> {

    private File mRecordFile;
    private List<Integer> mAlreadyUploadIndex = new ArrayList<Integer>();
    private OSSSharedPreferences mSp;
    private File mCRC64RecordFile;

    public ResumableUploadTask(ResumableUploadRequest request,
                               OSSCompletedCallback<ResumableUploadRequest, ResumableUploadResult> completedCallback,
                               ExecutionContext context, InternalRequestOperation apiOperation) {
        super(apiOperation, request, completedCallback, context);
        mSp = OSSSharedPreferences.instance(mContext.getApplicationContext());
    }

    @Override
    protected void initMultipartUploadId() throws IOException, ClientException, ServiceException {

        Map<Integer, Long> recordCrc64 = null;

        if (!OSSUtils.isEmptyString(mRequest.getRecordDirectory())) {
            OSSLog.logDebug("[initUploadId] - mUploadFilePath : " + mUploadFilePath);
            String fileMd5 = BinaryUtil.calculateMd5Str(mUploadFilePath);
            OSSLog.logDebug("[initUploadId] - mRequest.getPartSize() : " + mRequest.getPartSize());
            String recordFileName = BinaryUtil.calculateMd5Str((fileMd5 + mRequest.getBucketName()
                    + mRequest.getObjectKey() + String.valueOf(mRequest.getPartSize()) + (mCheckCRC64 ? "-crc64" : "")).getBytes());
            String recordPath = mRequest.getRecordDirectory() + File.separator + recordFileName;
            

            mRecordFile = new File(recordPath);
            if (mRecordFile.exists()) {
                BufferedReader br = new BufferedReader(new FileReader(mRecordFile));
                mUploadId = br.readLine();
                br.close();
            }

            OSSLog.logDebug("[initUploadId] - mUploadId : " + mUploadId);

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


                do{
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
                            OSSLog.logDebug("[initUploadId] -  " + i + " part.getPartNumber() : " + part.getPartNumber());
                            OSSLog.logDebug("[initUploadId] -  " + i + " part.getSize() : " + part.getSize());
                            mPartETags.add(partETag);
                            mUploadedLength += part.getSize();
                            mAlreadyUploadIndex.add(part.getPartNumber());
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

            long firstPartSize = mPartETags.get(0).getPartSize();
            OSSLog.logDebug("[initUploadId] - firstPartSize : " + firstPartSize);
            if (firstPartSize > 0 && firstPartSize != readByte && firstPartSize < mFileLength) {
                throw new ClientException("current part size " + readByte + " setting is inconsistent with before " + firstPartSize);
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
        //已经运行的任务需要添加已经上传的任务数量
        mRunPartTaskCount = mPartETags.size();

        for (int i = 0; i < partNumber; i++) {

            if (mAlreadyUploadIndex.size() != 0 && mAlreadyUploadIndex.contains(i + 1)) {
                continue;
            }

            if (mPoolExecutor != null) {
                //need read byte
                if (i == partNumber - 1) {
                    readByte = (int) (mFileLength - tempUploadedLength);
                }
                final int byteCount = readByte;
                final int readIndex = i;
                tempUploadedLength += byteCount;
                mPoolExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        uploadPart(readIndex, byteCount, partNumber);
                    }
                });
            }
        }

        if (checkWaitCondition(partNumber)) {
            synchronized (mLock) {
                mLock.wait();
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

        releasePool();
        return result;
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
        synchronized (mLock) {
            mPartExceptionCount++;
            mUploadException = e;
            OSSLog.logThrowable2Local(e);
            if (mContext.getCancellationHandler().isCancelled()) {
                if (!mIsCancel) {
                    mIsCancel = true;
                    mLock.notify();
                }
            }
            if (mPartETags.size() == (mRunPartTaskCount - mPartExceptionCount)) {
                notifyMultipartThread();
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
