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
 * The result class of copying an existing object to another one
 */
public class CopyObjectResult extends OSSResult {

    // Target object's ETag
    private String etag;

    // Target Object's last modified time
    private Date lastModified;

    /**
     * Creates a default instance of {@link CopyObjectResult}
     */
    public CopyObjectResult() {
    }

    /**
     * Gets the object's ETag value
     *
     * @return Object ETag
     */
    public String getETag() {
        return etag;
    }

    /**
     * Sets Object ETag
     *
     * @param etag Target object's ETag value
     */
    public void setEtag(String etag) {
        this.etag = etag;
    }

    /**
     * Gets the target object's last modified time
     *
     * @return Target object's last modified time
     */
    public Date getLastModified() {
        return lastModified;
    }

    /**
     * Sets the object's last modified time
     *
     * @param lastModified Target object's last modified time.
     */
    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

}
