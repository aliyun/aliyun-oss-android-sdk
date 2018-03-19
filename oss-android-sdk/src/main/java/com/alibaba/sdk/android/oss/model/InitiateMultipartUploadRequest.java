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

/**
 * The request class to initiate a multipart upload
 */
public class InitiateMultipartUploadRequest extends OSSRequest {

    public boolean isSequential;
    private String bucketName;
    private String objectKey;
    private ObjectMetadata metadata;

    /**
     * Constructor
     *
     * @param bucketName Bucket name
     * @param objectKey  Object key
     */
    public InitiateMultipartUploadRequest(String bucketName, String objectKey) {
        this(bucketName, objectKey, null);
    }

    /**
     * Constructor
     *
     * @param bucketName Bucket name
     * @param objectKey  Object key
     * @param metadata   Object metadata
     */
    public InitiateMultipartUploadRequest(String bucketName, String objectKey, ObjectMetadata metadata) {
        setBucketName(bucketName);
        setObjectKey(objectKey);
        setMetadata(metadata);
    }

    /**
     * Gets the bucket name
     *
     * @return The bucket name for a multipart upload
     */
    public String getBucketName() {
        return bucketName;
    }

    /**
     * Sets the bucket name
     *
     * @param bucketName Bucket name
     */
    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    /**
     * Gets the target object key
     *
     * @return The target object key which is the final object after the multipart uplaod finishes.
     */
    public String getObjectKey() {
        return objectKey;
    }

    /**
     * Sets the target object key
     *
     * @param objectKey The target object key
     */
    public void setObjectKey(String objectKey) {
        this.objectKey = objectKey;
    }

    /**
     * Gets the object's metadata.
     *
     * @return The object's metadata
     */
    public ObjectMetadata getMetadata() {
        return metadata;
    }

    /**
     * Sets the metadata
     *
     * @param metadata The object's metadata
     */
    public void setMetadata(ObjectMetadata metadata) {
        this.metadata = metadata;
    }
}
