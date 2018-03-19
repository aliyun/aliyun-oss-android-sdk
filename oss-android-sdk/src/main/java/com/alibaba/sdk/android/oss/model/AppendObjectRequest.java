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

public class AppendObjectRequest extends OSSRequest {

    private String bucketName;
    private String objectKey;

    private String uploadFilePath;

    private byte[] uploadData;

    private ObjectMetadata metadata;

    private OSSProgressCallback<AppendObjectRequest> progressCallback;

    private long position;

    private Long initCRC64;

    public AppendObjectRequest(String bucketName, String objectKey, String uploadFilePath) {
        this(bucketName, objectKey, uploadFilePath, null);
    }

    public AppendObjectRequest(String bucketName, String objectKey, String uploadFilePath, ObjectMetadata metadata) {
        setBucketName(bucketName);
        setObjectKey(objectKey);
        setUploadFilePath(uploadFilePath);
        setMetadata(metadata);
    }

    public AppendObjectRequest(String bucketName, String objectKey, byte[] uploadData) {
        this(bucketName, objectKey, uploadData, null);
    }

    public AppendObjectRequest(String bucketName, String objectKey, byte[] uploadData, ObjectMetadata metadata) {
        setBucketName(bucketName);
        setObjectKey(objectKey);
        setUploadData(uploadData);
        setMetadata(metadata);
    }

    public long getPosition() {
        return position;
    }

    public void setPosition(long position) {
        this.position = position;
    }

    public String getBucketName() {
        return bucketName;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    public String getObjectKey() {
        return objectKey;
    }

    public void setObjectKey(String objectKey) {
        this.objectKey = objectKey;
    }

    public String getUploadFilePath() {
        return uploadFilePath;
    }

    public void setUploadFilePath(String uploadFilePath) {
        this.uploadFilePath = uploadFilePath;
    }

    public byte[] getUploadData() {
        return uploadData;
    }

    public void setUploadData(byte[] uploadData) {
        this.uploadData = uploadData;
    }

    public ObjectMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(ObjectMetadata metadata) {
        this.metadata = metadata;
    }

    public OSSProgressCallback<AppendObjectRequest> getProgressCallback() {
        return progressCallback;
    }

    public void setProgressCallback(OSSProgressCallback<AppendObjectRequest> progressCallback) {
        this.progressCallback = progressCallback;
    }

    public Long getInitCRC64() {
        return initCRC64;
    }

    public void setInitCRC64(Long initCRC64) {
        this.initCRC64 = initCRC64;
    }
}
