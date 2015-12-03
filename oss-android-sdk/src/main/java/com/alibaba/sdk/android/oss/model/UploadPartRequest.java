/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.alibaba.sdk.android.oss.model;

import com.alibaba.sdk.android.oss.callback.OSSProgressCallback;
import com.alibaba.sdk.android.oss.common.utils.IOUtils;

import java.io.InputStream;

/**
 * 包含上传Multipart分块（Part）参数。
 */
public class UploadPartRequest extends OSSRequest {

    private String bucketName;

    private String objectKey;

    private String uploadId;

    private int partNumber;

    private byte[] partContent;

    private OSSProgressCallback<UploadPartRequest> progressCallback;

    private String md5Digest;

    /**
     * 默认构造函数。
     */
    public UploadPartRequest() {}

    /**
     * 构造函数
     */
    public UploadPartRequest(String bucketName, String objectKey, String uploadId, int partNumber) {
        this.bucketName = bucketName;
        this.objectKey = objectKey;
        this.uploadId = uploadId;
        this.partNumber = partNumber;
    }

    /**
     * @return Bucket名称。
     */
    public String getBucketName() {
        return bucketName;
    }

    /**
     * 设置Bucket名称。
     * @param bucketName
     *          Bucket名称。
     */
    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    /**
     * 返回objectKey。
     * @return Object objectKey。
     */
    public String getObjectKey() {
        return objectKey;
    }

    /**
     * 设置OSSObject objectKey。
     * @param objectKey
     *          Object objectKey。
     */
    public void setObjectKey(String objectKey) {
        this.objectKey = objectKey;
    }

    /**
     * 返回标识Multipart上传事件的Upload ID。
     * @return 标识Multipart上传事件的Upload ID。
     */
    public String getUploadId() {
        return uploadId;
    }

    /**
     * 设置标识Multipart上传事件的Upload ID。
     * @param uploadId
     *          标识Multipart上传事件的Upload ID。
     */
    public void setUploadId(String uploadId) {
        this.uploadId = uploadId;
    }

    /**
     * 返回上传分块（Part）的标识号码（Part Number）。
     * 每一个上传分块（Part）都有一个标识它的号码（范围1~10000）。
     * 对于同一个Upload ID，该号码不但唯一标识这一块数据，也标识了这块数据在整个文件中的
     * 相对位置。如果你用同一个Part号码上传了新的数据，那么OSS上已有的这个号码的Part数据
     * 将被覆盖。
     * @return 上传分块（Part）的标识号码（Part Number）。
     */
    public int getPartNumber() {
        return partNumber;
    }

    /**
     * 设置上传分块（Part）的标识号码（Part Number）。
     * 每一个上传分块（Part）都有一个标识它的号码（范围1~10000）。
     * 对于同一个Upload ID，该号码不但唯一标识这一块数据，也标识了这块数据在整个文件中的
     * 相对位置。如果你用同一个Part号码上传了新的数据，那么OSS上已有的这个号码的Part数据
     * 将被覆盖。
     * @param partNumber
     *          上传分块（Part）的标识号码（Part Number）。
     */
    public void setPartNumber(int partNumber) {
        this.partNumber = partNumber;
    }

    /**
     * 返回分块（Part）数据的MD5校验值。
     * @return 分块（Part）数据的MD5校验值。
     */
    public String getMd5Digest() {
        return md5Digest;
    }

    /**
     * 设置分块（Part）数据的MD5校验值。
     * @param md5Digest
     *          分块（Part）数据的MD5校验值。
     */
    public void setMd5Digest(String md5Digest) {
        this.md5Digest = md5Digest;
    }

    public OSSProgressCallback<UploadPartRequest> getProgressCallback() {
        return progressCallback;
    }

    /**
     * 设置上传分块的进度回调
     */
    public void setProgressCallback(OSSProgressCallback<UploadPartRequest> progressCallback) {
        this.progressCallback = progressCallback;
    }

    public byte[] getPartContent() {
        return partContent;
    }

    /**
     * 设置上传分块的内容
     */
    public void setPartContent(byte[] partContent) {
        this.partContent = partContent;
    }
}
