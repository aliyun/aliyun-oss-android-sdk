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
 * The wrapper class of a part's part number and its ETag
 */
public class PartETag {

    private int partNumber;

    private String eTag;

    private long partSize;

    private long crc64;

    /**
     * Constructor
     *
     * @param partNumber Part number
     * @param eTag       Part ETag
     */
    public PartETag(int partNumber, String eTag) {
        setPartNumber(partNumber);
        setETag(eTag);
    }

    /**
     * Gets the Part number
     *
     * @return Part number
     */
    public int getPartNumber() {
        return partNumber;
    }

    /**
     * Sets the Part number
     *
     * @param partNumber Part number
     */
    public void setPartNumber(int partNumber) {
        this.partNumber = partNumber;
    }

    /**
     * Gets Part ETag
     *
     * @return Part ETag
     */
    public String getETag() {
        return eTag;
    }

    /**
     * Sets Part ETag
     *
     * @param eTag Part ETag
     */
    public void setETag(String eTag) {
        this.eTag = eTag;
    }

    public long getPartSize() {
        return partSize;
    }

    public void setPartSize(long partSize) {
        this.partSize = partSize;
    }

    public long getCRC64() {
        return crc64;
    }

    public void setCRC64(long crc64) {
        this.crc64 = crc64;
    }
}
