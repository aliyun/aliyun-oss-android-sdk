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
 * The uploading part result class definition
 */
public class UploadPartResult extends OSSResult {

    private String eTag;

    /**
     * Constructor
     */
    public UploadPartResult() {
    }

    /**
     * Gets the ETag generated from OSS
     * <p>
     * OSS will stamp the MD5 value of the part content in ETag header.
     * This is for the integrity check to make sure the data is transferred successfully.
     * It's strongly recommended to calculate the MD5 value locally and compare with this value.
     * If it does not match, re-upload the data.
     * </p>
     *
     * @return ETag value
     */
    public String getETag() {
        return eTag;
    }

    /**
     * Sets the ETag value (SDK internal only)
     * <p>
     * OSS will stamp the MD5 value of the part content in ETag header.
     * This is for the integrity check to make sure the data is transferred successfully.
     * It's strongly recommended to calculate the MD5 value locally and compare with this value.
     * If it does not match, re-upload the data.
     * </p>
     *
     * @param eTag ETag value
     */
    public void setETag(String eTag) {
        this.eTag = eTag;
    }
}
