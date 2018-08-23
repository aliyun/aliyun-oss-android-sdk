/**
 * Copyright (C) Alibaba Cloud Computing, 2015
 * All rights reserved.
 * <p>
 * 版权所有 （C）阿里巴巴云计算，2015
 */

package com.alibaba.sdk.android.oss;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Client configuration for access to Ali cloud services
 */
public class ClientConfiguration {

    private static final int DEFAULT_MAX_RETRIES = 2;
    private int maxConcurrentRequest = 5;
    private int socketTimeout = 60 * 1000;
    private int connectionTimeout = 60 * 1000;
    private long max_log_size = 5 * 1024 * 1024;
    private int maxErrorRetry = DEFAULT_MAX_RETRIES;
    private List<String> customCnameExcludeList = new ArrayList<String>();
    private String proxyHost;
    private int proxyPort;
    private String mUserAgentMark;
    private boolean httpDnsEnable = true;
    private boolean checkCRC64 = false;//crc64 default false

    /**
     * Constructor
     */
    public ClientConfiguration() {
    }

    /**
     * Gets the default configuration instance
     */
    public static ClientConfiguration getDefaultConf() {
        return new ClientConfiguration();
    }

    /**
     * Gets the max concurrent request count
     *
     * @return
     */
    public int getMaxConcurrentRequest() {
        return maxConcurrentRequest;
    }

    /**
     * Sets the max concurrent request count
     *
     * @param maxConcurrentRequest The max HTTP request count
     */
    public void setMaxConcurrentRequest(int maxConcurrentRequest) {
        this.maxConcurrentRequest = maxConcurrentRequest;
    }

    /**
     * Gets the socket timeout in milliseconds
     * 0 means infinite (not recommended)
     *
     * @return the socket timeout in milliseconds
     */
    public int getSocketTimeout() {
        return socketTimeout;
    }

    /**
     * Gets the socket timeout in milliseconds
     * 0 means infinite (not recommended)
     *
     * @param socketTimeout the socket timeout in milliseconds
     */
    public void setSocketTimeout(int socketTimeout) {
        this.socketTimeout = socketTimeout;
    }

    /**
     * Gets the connection timeout in milliseconds
     *
     * @return The connection timeout in milliseconds
     */
    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    /**
     * Sets the connection timeout in milliseconds
     *
     * @param connectionTimeout The connection timeout in milliseconds
     */
    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public long getMaxLogSize() {
        return max_log_size;
    }

    /**
     * set max log file size  default 5mb
     *
     * @param max_log_size
     */
    public void setMaxLogSize(long max_log_size) {
        this.max_log_size = max_log_size;
    }

    /**
     * Gets the max retry count after the recoverable failure. By default it's 2.
     *
     * @return The max retry count after the recoverable failure.
     */
    public int getMaxErrorRetry() {
        return maxErrorRetry;
    }

    /**
     * Sets the max retry count after the recoverable failure. By default it's 2.
     *
     * @param maxErrorRetry The max retry count after the recoverable failure.
     */
    public void setMaxErrorRetry(int maxErrorRetry) {
        this.maxErrorRetry = maxErrorRetry;
    }

    /**
     * Gets the immutable CName excluded list. The element in this list will skip the CName resolution.
     *
     * @return CNAME excluded list.
     */
    public List<String> getCustomCnameExcludeList() {
        return Collections.unmodifiableList(this.customCnameExcludeList);
    }

    /**
     * Sets CNAME excluded list
     *
     * @param customCnameExcludeList CNAME excluded list
     */
    public void setCustomCnameExcludeList(List<String> customCnameExcludeList) {
        if (customCnameExcludeList == null || customCnameExcludeList.size() == 0) {
            throw new IllegalArgumentException("cname exclude list should not be null.");
        }

        this.customCnameExcludeList.clear();
        for (String host : customCnameExcludeList) {
            if (host.contains("://")) {
                this.customCnameExcludeList.add(host.substring(host.indexOf("://") + 3));
            } else {
                this.customCnameExcludeList.add(host);
            }
        }
    }

    public String getProxyHost() {
        return proxyHost;
    }

    public void setProxyHost(String proxyHost) {
        this.proxyHost = proxyHost;
    }

    public int getProxyPort() {
        return proxyPort;
    }

    public void setProxyPort(int proxyPort) {
        this.proxyPort = proxyPort;
    }

    public String getCustomUserMark() {
        return mUserAgentMark;
    }

    public void setUserAgentMark(String mark) {
        this.mUserAgentMark = mark;
    }

    public boolean isHttpDnsEnable() {
        return httpDnsEnable;
    }

    public void setHttpDnsEnable(boolean httpdnsEnable) {
        this.httpDnsEnable = httpdnsEnable;
    }

    public boolean isCheckCRC64() {
        return checkCRC64;
    }

    /**
     * set check file with CRC64
     *
     * @param checkCRC64
     */
    public void setCheckCRC64(boolean checkCRC64) {
        this.checkCRC64 = checkCRC64;
    }
}
