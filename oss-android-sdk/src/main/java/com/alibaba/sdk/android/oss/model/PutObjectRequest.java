package com.alibaba.sdk.android.oss.model;

/**
 * Created by zhouzhuo on 11/23/15.
 */
import com.alibaba.sdk.android.oss.callback.OSSProgressCallback;

import java.util.Map;

public class PutObjectRequest extends OSSRequest {
    
	private String bucketName;
    private String objectKey;
    
    private String uploadFilePath;

	private byte[] uploadData;

    private ObjectMetadata metadata;

	private Map<String, String> callbackParam;

	private Map<String, String> callbackVars;

	private OSSProgressCallback<PutObjectRequest> progressCallback;

	/**
	 * 构造上传文件请求
	 * @param bucketName 上传到Bucket的名字
	 * @param objectKey 上传到OSS后的ObjectKey
	 * @param uploadFilePath 上传文件的本地路径
	 */
    public PutObjectRequest(String bucketName, String objectKey, String uploadFilePath) {
        this.bucketName = bucketName;
        this.objectKey = objectKey;
		this.uploadFilePath = uploadFilePath;
    }

	/**
	 * 构造上传文件请求
	 * @param bucketName 上传到Bucket的名字
	 * @param objectKey 上传到OSS后的ObjectKey
	 * @param uploadFilePath 上传文件的本地路径
	 * @param metadata 设置上传文件的元信息
	 */
    public PutObjectRequest(String bucketName, String objectKey, String uploadFilePath, ObjectMetadata metadata) {
        this.bucketName = bucketName;
        this.objectKey = objectKey;
		this.uploadFilePath = uploadFilePath;
        this.metadata = metadata;
    }

	/**
	 * 构造上传文件请求
	 * @param bucketName 上传到Bucket的名字
	 * @param objectKey 上传到OSS后的ObjectKey
	 * @param uploadData 从byte[]数组上传数据
	 */
	public PutObjectRequest(String bucketName, String objectKey, byte[] uploadData) {
		this.bucketName = bucketName;
		this.objectKey = objectKey;
		this.uploadData = uploadData;
	}

	/**
	 * 构造上传文件请求
	 * @param bucketName 上传到Bucket的名字
	 * @param objectKey 上传到OSS后的ObjectKey
	 * @param uploadData 从byte[]数组上传数据
	 * @param metadata 设置上传文件的元信息
	 */
	public PutObjectRequest(String bucketName, String objectKey, byte[] uploadData, ObjectMetadata metadata) {
		this.bucketName = bucketName;
		this.objectKey = objectKey;
		this.uploadData = uploadData;
		this.metadata = metadata;
	}

	/**
	 * 返回请求的BucketName
	 * @return 请求的BucketName
	 */
	public String getBucketName() {
		return bucketName;
	}

	/**
	 * 设置请求的BucketName
	 */
	public void setBucketName(String bucketName) {
		this.bucketName = bucketName;
	}

	/**
	 * 返回请求的ObjectKey
	 * @return 请求的ObjectKey
	 */
    public String getObjectKey() {
        return objectKey;
    }

	/**
	 * 设置请求的ObjectKey
	 */
    public void setObjectKey(String objectKey) {
        this.objectKey = objectKey;
    }

    public String getUploadFilePath() {
		return uploadFilePath;
	}

	/**
	 * 上传本地文件到OSS，设置本地文件路径
	 * @param uploadFilePath 本地文件路径，上传到OSS
	 */
	public void setUploadFilePath(String uploadFilePath) {
		this.uploadFilePath = uploadFilePath;
	}

	public byte[] getUploadData() {
		return uploadData;
	}

	/**
	 * 上传Byte数据到OSS，设置数据内容
	 * @param uploadData
	 */
	public void setUploadData(byte[] uploadData) {
		this.uploadData = uploadData;
	}

	public ObjectMetadata getMetadata() {
		return metadata;
	}

	/**
	 * 设置上传的文件的元信息
	 * @param metadata 元信息
	 */
	public void setMetadata(ObjectMetadata metadata) {
		this.metadata = metadata;
	}
	
	public OSSProgressCallback<PutObjectRequest> getProgressCallback() {
		return progressCallback;
	}

	/**
	 * 设置上传进度回调
	 * @param progressCallback
	 */
	public void setProgressCallback(OSSProgressCallback<PutObjectRequest> progressCallback) {
		this.progressCallback = progressCallback;
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
}
