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

import java.util.Date;

/**
 * The multipart upload's part summary class definition
 */
public class PartSummary {

    private int partNumber;

    private Date lastModified;

    private String eTag;

    private long size;

    /**
     * Constructor
     */
    public PartSummary() {
    }

    /**
     * Gets the part number.
     *
     * @return Part number
     */
    public int getPartNumber() {
        return partNumber;
    }

    /**
     * Gets the part number.
     *
     * @param partNumber Part number
     */
    public void setPartNumber(int partNumber) {
        this.partNumber = partNumber;
    }

    /**
     * Gets the part's last modified time
     *
     * @return Part's last modified time
     */
    public Date getLastModified() {
        return lastModified;
    }

    /**
     * Sets the part's last modified time
     *
     * @param lastModified Part's last modified time
     */
    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    /**
     * Gets Part ETag value
     *
     * @return Part ETag value
     */
    public String getETag() {
        return eTag;
    }

    /**
     * Sets Part ETag value
     *
     * @param eTag Part ETag value
     */
    public void setETag(String eTag) {
        this.eTag = eTag;
    }

    /**
     * Gets Part size in byte
     *
     * @return Part in byte
     */
    public long getSize() {
        return size;
    }

    /**
     * Sets Part size in byte
     *
     * @param size Part size in byte
     */
    public void setSize(long size) {
        this.size = size;
    }

}
