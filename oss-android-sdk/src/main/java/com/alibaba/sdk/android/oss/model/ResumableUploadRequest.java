package com.alibaba.sdk.android.oss.model;

import com.alibaba.sdk.android.oss.callback.OSSProgressCallback;
import com.alibaba.sdk.android.oss.common.OSSConstants;

import java.io.File;
import java.util.Map;

/**
 * Created by zhouzhuo on 11/27/15.
 *
 * 断点上传请求
 *
 * 断点上传是通过OSS的分块上传功能实现的，移动端网络条件可能较差，容易遇到超时等问题，使用断点上传，
 * 可以保证在遇到超时等问题进行重试时，只需要重新上传当次分块，避免全部重新上传，节省流量。
 *
 * 如果设置断点记录的保存文件夹，任务失败后，下次重新开启同样任务(上传文件、保存地址、分块大小都一致)时，
 * 任务可以从上次失败的地方继续上传。
 */
public class ResumableUploadRequest extends OSSRequest {
    private String bucketName;
    private String objectKey;

    private Boolean deleteUploadOnCancelling = true;

    private String uploadFilePath;
    private String recordDirectory;
    private long partSize = 256 * 1024;

    private ObjectMetadata metadata;

    private Map<String, String> callbackParam;
    private Map<String, String> callbackVars;

    private OSSProgressCallback<ResumableUploadRequest> progressCallback;

    /**
     * 构造新的断点上传请求
     * @param bucketName 上传到的Bucket名
     * @param objectKey 上传的Object名
     * @param uploadFilePath 上传本地文件的路径
     */
    public ResumableUploadRequest(String bucketName, String objectKey, String uploadFilePath) {
        this.bucketName = bucketName;
        this.objectKey = objectKey;
        this.uploadFilePath = uploadFilePath;
    }

    /**
     * 构造新的断点上传请求
     * @param bucketName 上传到的Bucket名
     * @param objectKey 上传的Object名
     * @param uploadFilePath 上传本地文件的路径
     * @param metadata 上传文件的元信息
     */
    public ResumableUploadRequest(String bucketName, String objectKey, String uploadFilePath, ObjectMetadata metadata) {
        this.bucketName = bucketName;
        this.objectKey = objectKey;
        this.uploadFilePath = uploadFilePath;
        this.metadata = metadata;
    }

    /**
     * 构造新的断点上传请求
     * @param bucketName 上传到的Bucket名
     * @param objectKey 上传的Object名
     * @param uploadFilePath 上传本地文件的路径
     * @param recordDirectory 断点记录文件的保存位置，需是一个文件夹的绝对路径
     */
    public ResumableUploadRequest(String bucketName, String objectKey, String uploadFilePath, String recordDirectory) {
        this.bucketName = bucketName;
        this.objectKey = objectKey;
        this.uploadFilePath = uploadFilePath;
        setRecordDirectory(recordDirectory);
    }

    /**
     * 构造新的断点上传请求
     * @param bucketName 上传到的Bucket名
     * @param objectKey 上传的Object名
     * @param uploadFilePath 上传本地文件的路径
     * @param metadata 上传文件的元信息
     * @param recordDirectory 断点记录文件的保存位置，需是一个文件夹的绝对路径
     */
    public ResumableUploadRequest(String bucketName, String objectKey, String uploadFilePath, ObjectMetadata metadata, String recordDirectory) {
        this.bucketName = bucketName;
        this.objectKey = objectKey;
        this.uploadFilePath = uploadFilePath;
        this.metadata = metadata;
        setRecordDirectory(recordDirectory);
    }

    public String getBucketName() {
        return bucketName;
    }

    /**
     * 设置上传到OSS的Bucket名
     * @param bucketName
     */
    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    public String getObjectKey() {
        return objectKey;
    }

    /**
     * 设置上传到OSS的Object名
     * @param objectKey
     */
    public void setObjectKey(String objectKey) {
        this.objectKey = objectKey;
    }

    public String getUploadFilePath() {
        return uploadFilePath;
    }

    /**
     * 设置上传文件的本地文件路径
     * @param uploadFilePath 本地文件路径
     */
    public void setUploadFilePath(String uploadFilePath) {
        this.uploadFilePath = uploadFilePath;
    }

    public String getRecordDirectory() {
        return recordDirectory;
    }

    /**
     * 设置断点进度记录文件在本地文件系统的存储地址(需要保证这个目录已经存在)
     * @param recordDirectory 记录文件存储目录
     */
    public void setRecordDirectory(String recordDirectory) {
        File file = new File(recordDirectory);
        if (!file.exists() || !file.isDirectory()) {
            throw new IllegalArgumentException("Record directory must exist, and it should be a directory!");
        }
        this.recordDirectory = recordDirectory;
    }

    public ObjectMetadata getMetadata() {
        return metadata;
    }

    /**
     * 设置上传文件的元信息
     * @param metadata 文件元信息
     */
    public void setMetadata(ObjectMetadata metadata) {
        this.metadata = metadata;
    }

    public OSSProgressCallback<ResumableUploadRequest> getProgressCallback() {
        return progressCallback;
    }

    /**
     * 设置上传进度回调
     */
    public void setProgressCallback(OSSProgressCallback<ResumableUploadRequest> progressCallback) {
        this.progressCallback = progressCallback;
    }

    public long getPartSize() {
        return partSize;
    }

    /**
     * 设置分块大小，默认256KB，最小为100KB
     * @param partSize 分块大小
     */
    public void setPartSize(long partSize) {
        if (partSize < OSSConstants.MIN_PART_SIZE_LIMIT) {
            throw new IllegalArgumentException("Part size must be greater than or equal to 100KB!");
        }
        this.partSize = partSize;
    }

    public Map<String, String> getCallbackParam() {
        return callbackParam;
    }

    /**
     * 设置servercallback参数
     */
    public void setCallbackParam(Map<String, String> callbackParam) {
        this.callbackParam = callbackParam;
    }

    public Map<String, String> getCallbackVars() {
        return callbackVars;
    }

    /**
     * 设置servercallback自定义变量
     */
    public void setCallbackVars(Map<String, String> callbackVars) {
        this.callbackVars = callbackVars;
    }

    public Boolean deleteUploadOnCancelling() {
        return deleteUploadOnCancelling;
    }

    public void setDeleteUploadOnCancelling(Boolean deleteUploadOnCancelling) {
        this.deleteUploadOnCancelling = deleteUploadOnCancelling;
    }
}
