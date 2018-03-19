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

/**
 * The uploading part request class definition
 */
public class UploadPartRequest extends OSSRequest {

    private String bucketName;

    private String objectKey;

    private String uploadId;

    private int partNumber;

    private byte[] partContent;

    //run with not ui thread
    private OSSProgressCallback<UploadPartRequest> progressCallback;

    private String md5Digest;

    /**
     * Default constructor
     */
    public UploadPartRequest() {
    }

    /**
     * Constructor
     */
    public UploadPartRequest(String bucketName, String objectKey, String uploadId, int partNumber) {
        this.bucketName = bucketName;
        this.objectKey = objectKey;
        this.uploadId = uploadId;
        this.partNumber = partNumber;
    }

    /**
     * @return bucket name
     */
    public String getBucketName() {
        return bucketName;
    }

    /**
     * Sets bucket name
     *
     * @param bucketName bucket name
     */
    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    /**
     * Gets objectKey。
     *
     * @return Object objectKey。
     */
    public String getObjectKey() {
        return objectKey;
    }

    /**
     * Sets OSSObject objectKey。
     *
     * @param objectKey Object objectKey。
     */
    public void setObjectKey(String objectKey) {
        this.objectKey = objectKey;
    }

    /**
     * Gets upload Id in the multipart upload
     *
     * @return upload Id in the multipart upload
     */
    public String getUploadId() {
        return uploadId;
    }

    /**
     * Sets upload Id in the multipart upload
     *
     * @param uploadId upload Id in the multipart upload
     */
    public void setUploadId(String uploadId) {
        this.uploadId = uploadId;
    }

    /**
     * Gets the part number which ranges from 1 to 10000.
     * For a given upload Id, the part number identifies the part and its position in the whole file.
     * If the same part number is used in another part upload, then the data will be overwritten by
     * that upload.
     *
     * @return The part number
     */
    public int getPartNumber() {
        return partNumber;
    }

    /**
     * Sets the part number which ranges from 1 to 10000.
     * For a given upload Id, the part number identifies the part and its position in the whole file.
     * If the same part number is used in another part upload, then the data will be overwritten by
     * that upload.
     *
     * @param partNumber Part number
     */
    public void setPartNumber(int partNumber) {
        this.partNumber = partNumber;
    }

    /**
     * Gets the MD5's digest value
     *
     * @return MD5 digest value
     */
    public String getMd5Digest() {
        return md5Digest;
    }

    /**
     * Sets the MD5 digest value of the part
     *
     * @param md5Digest The MD5 digest value of the part
     */
    public void setMd5Digest(String md5Digest) {
        this.md5Digest = md5Digest;
    }

    public OSSProgressCallback<UploadPartRequest> getProgressCallback() {
        return progressCallback;
    }

    /**
     * Sets the progress callback
     */
    public void setProgressCallback(OSSProgressCallback<UploadPartRequest> progressCallback) {
        this.progressCallback = progressCallback;
    }

    public byte[] getPartContent() {
        return partContent;
    }

    /**
     * Sets the part's content to upload
     */
    public void setPartContent(byte[] partContent) {
        this.partContent = partContent;
    }
}
