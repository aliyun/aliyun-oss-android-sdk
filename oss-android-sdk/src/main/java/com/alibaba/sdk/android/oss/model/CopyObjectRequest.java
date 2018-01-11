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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * The request class of copying an existing OSS object to another one
 */
public class CopyObjectRequest extends OSSRequest {

    // Source Object's bucket name
    private String sourceBucketName;

    // Source Object's key
    private String sourceKey;

    // Target Object's bucket name
    private String destinationBucketName;

    // Target Object Key
    private String destinationKey;

    // Target Object's server side encryption method
    private String serverSideEncryption;

    // Target Object Metadata
    private ObjectMetadata newObjectMetadata;

    // The ETag matching constraints. If the source object's ETag matches the one user provided, copy the file.
    // Otherwise returns 412 (precondition failed).
    private List<String> matchingETagConstraints = new ArrayList<String>();

    // The ETag non-matching constraints. If the source object's ETag does not match the one user provided, copy the file.
    // Otherwise returns 412 (precondition failed).
    private List<String> nonmatchingEtagConstraints = new ArrayList<String>();

    // The unmodified since constraint. If the parameter value is same or later than the actual file's modified time, copy the file.
    // Otherwise returns 412 (precondition failed).
    private Date unmodifiedSinceConstraint;

    // The modified since constraint. If the parameter value is earlier than the actual file's modified time, copy the file.
    // Otherwise returns 412 (precondition failed).
    private Date modifiedSinceConstraint;

    /**
     * Creates an instance of {@link CopyObjectRequest}
     *
     * @param sourceBucketName      Source Object's bucket name
     * @param sourceKey             Source Object's key
     * @param destinationBucketName Target Object's bucket name
     * @param destinationKey        Target Object key
     */
    public CopyObjectRequest(String sourceBucketName, String sourceKey,
                             String destinationBucketName, String destinationKey) {
        setSourceBucketName(sourceBucketName);
        setSourceKey(sourceKey);
        setDestinationBucketName(destinationBucketName);
        setDestinationKey(destinationKey);
    }

    /**
     * Gets Object bucket name
     *
     * @return Source Object's bucket name
     */
    public String getSourceBucketName() {
        return sourceBucketName;
    }

    /**
     * Sets source object's bucket name
     *
     * @param sourceBucketName Source object's bucket name
     */
    public void setSourceBucketName(String sourceBucketName) {
        this.sourceBucketName = sourceBucketName;
    }

    /**
     * Gets the source object key
     *
     * @return Source Object key
     */
    public String getSourceKey() {
        return sourceKey;
    }

    /**
     * Sets the object key
     *
     * @param sourceKey Source Object Key
     */
    public void setSourceKey(String sourceKey) {
        this.sourceKey = sourceKey;
    }

    /**
     * Gets the target bucket name
     *
     * @return Target object's bucket name
     */
    public String getDestinationBucketName() {
        return destinationBucketName;
    }

    /**
     * Sets the target bucket name
     *
     * @param destinationBucketName Target object's bucket name
     */
    public void setDestinationBucketName(String destinationBucketName) {
        this.destinationBucketName = destinationBucketName;
    }

    /**
     * Gets target object's key
     *
     * @return The target object key
     */
    public String getDestinationKey() {
        return destinationKey;
    }

    /**
     * Sets the target object Key
     *
     * @param destinationKey The target Object key
     */
    public void setDestinationKey(String destinationKey) {
        this.destinationKey = destinationKey;
    }

    /**
     * Gets the target Object {@link ObjectMetadata} instance
     *
     * @return the target Object {@link ObjectMetadata} instance
     */
    public ObjectMetadata getNewObjectMetadata() {
        return newObjectMetadata;
    }

    /**
     * Sets the target Object {@link ObjectMetadata} instance
     *
     * @param newObjectMetadata he target Object {@link ObjectMetadata} instance
     */
    public void setNewObjectMetadata(ObjectMetadata newObjectMetadata) {
        this.newObjectMetadata = newObjectMetadata;
    }

    /**
     * Gets the ETag matching constraints. If the object's ETag matches anyone of this list, copy the result.
     * Otherwise, 412 is returned (precondition failed)
     *
     * @return ETag list to match.
     */
    public List<String> getMatchingETagConstraints() {
        return matchingETagConstraints;
    }

    /**
     * Sets the ETag matching constraints. If the object's ETag matches anyone of this list, copy the result.
     * Otherwise, 412 is returned (precondition failed)
     *
     * @param matchingETagConstraints ETag list to match.
     */
    public void setMatchingETagConstraints(List<String> matchingETagConstraints) {
        this.matchingETagConstraints.clear();
        if (matchingETagConstraints != null && !matchingETagConstraints.isEmpty()) {
            this.matchingETagConstraints.addAll(matchingETagConstraints);
        }
    }

    public void clearMatchingETagConstraints() {
        this.matchingETagConstraints.clear();
    }

    /**
     * Gets the ETag's non-matching constraints.
     * If the object's ETag does not match anyone of this list, copy the result.
     * Otherwise, 412 is returned (precondition failed).
     *
     * @return ETag list to match
     */
    public List<String> getNonmatchingEtagConstraints() {
        return nonmatchingEtagConstraints;
    }

    /**
     * Sets the ETag's non-matching constraints.
     * If the object's ETag does not match anyone of this list, copy the result.
     * Otherwise, 412 is returned (precondition failed).
     *
     * @param nonmatchingEtagConstraints ETag list to match
     */
    public void setNonmatchingETagConstraints(List<String> nonmatchingEtagConstraints) {
        this.nonmatchingEtagConstraints.clear();
        if (nonmatchingEtagConstraints != null && !nonmatchingEtagConstraints.isEmpty()) {
            this.nonmatchingEtagConstraints.addAll(nonmatchingEtagConstraints);
        }
    }

    public void clearNonmatchingETagConstraints() {
        this.nonmatchingEtagConstraints.clear();
    }

    /**
     * Gets the unmodified-since constraint. If it's same or later than the actual modified time of the file, copy the file.
     * Otherwise, 412 is returned (precondition failed).
     *
     * @return The timestamp threshold of last modified time.
     */
    public Date getUnmodifiedSinceConstraint() {
        return unmodifiedSinceConstraint;
    }

    /**
     * Gets the unmodified-since constraint. If it's same or later than the actual modified time of the file, copy the file.
     * Otherwise, 412 is returned (precondition failed).
     *
     * @param unmodifiedSinceConstraint The timestamp threshold of last modified time.
     */
    public void setUnmodifiedSinceConstraint(Date unmodifiedSinceConstraint) {
        this.unmodifiedSinceConstraint = unmodifiedSinceConstraint;
    }

    /**
     * Gets the modified-since constraint. If it's earlier than the actual modified time of the file, copy the file.
     * Otherwise, 412 is returned (precondition failed).
     *
     * @return The timestamp threshold of last modified time.
     */
    public Date getModifiedSinceConstraint() {
        return modifiedSinceConstraint;
    }

    /**
     * Sets the modified-since constraint. If it's earlier than the actual modified time of the file, copy the file.
     * Otherwise, 412 is returned (precondition failed).
     *
     * @param modifiedSinceConstraint The timestamp threshold of last modified time.
     */
    public void setModifiedSinceConstraint(Date modifiedSinceConstraint) {
        this.modifiedSinceConstraint = modifiedSinceConstraint;
    }

    /**
     * Gets the server side encryption
     *
     * @return The server side encryption
     */
    public String getServerSideEncryption() {
        return this.serverSideEncryption;
    }

    /**
     * Sets the server side encryption
     *
     * @param serverSideEncryption the server side encryption
     */
    public void setServerSideEncryption(String serverSideEncryption) {
        this.serverSideEncryption = serverSideEncryption;
    }
}
