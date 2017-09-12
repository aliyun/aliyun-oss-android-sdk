/**
 * Copyright (C) Alibaba Cloud Computing, 2015
 * All rights reserved.
 * 
 * 版权所有 （C）阿里巴巴云计算，2015
 */

package com.alibaba.sdk.android.oss;

import com.alibaba.sdk.android.oss.common.utils.VersionInfoUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Client configuration for access to Ali cloud services
 */
public class ClientConfiguration {

    private static final String DEFAULT_USER_AGENT = VersionInfoUtils.getDefaultUserAgent();
    private static final int DEFAULT_MAX_RETRIES = 2;

    private int maxConcurrentRequest = 5;
    private int socketTimeout = 15 * 1000;
    private int connectionTimeout = 15 * 1000;
    private long max_log_size = 5 * 1024 * 1024;
    private int maxErrorRetry = DEFAULT_MAX_RETRIES;
    private List<String> customCnameExcludeList = new ArrayList<String>();
    private String proxyHost;
    private int proxyPort;

    /**
     * construct instance
     */
    public ClientConfiguration(){
    }

    /**
     * get default instance
     */
    public static ClientConfiguration getDefaultConf() {
        return new ClientConfiguration();
    }

    /**
     * get max concurrent request count
     * @return
     */
    public int getMaxConcurrentRequest() {
        return maxConcurrentRequest;
    }

    /**
     * set max concurrent request count
     * @param maxConcurrentRequest
     */
    public void setMaxConcurrentRequest(int maxConcurrentRequest) {
        this.maxConcurrentRequest = maxConcurrentRequest;
    }

    /**
     * get socket timeout time（unit: millisecond）。
     * 0 is infinite wait（not recommend）。
     * @return connection socket timeout time（unit: millisecond）。
     */
    public int getSocketTimeout() {
        return socketTimeout;
    }

    /**
     * set socket timeout time（unit: millisecond）。
     * 0 is infinite wait（not recommend）。
     * @param socketTimeout connection socket timeout time（unit: millisecond）。
     */
    public void setSocketTimeout(int socketTimeout) {
        this.socketTimeout = socketTimeout;
    }

    /**
     * get connection timeout（unit: millisecond）。
     * @return build connection timeout（unit: millisecond）。
     */
    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    /**
     * connection timeout（unit: millisecond）。
     * @param connectionTimeout set connection timeout（unit: millisecond）。
     */
    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    /**
     * set max log file size, default 5mb
     * @param max_log_size
     */
    public void setMaxLogSize(long max_log_size) {
        this.max_log_size = max_log_size;
    }

    public long getMaxLogSize() {
        return max_log_size;
    }

    /**
     * get retry request max count（default count is 2）
     * @return
     */
    public int getMaxErrorRetry() {
        return maxErrorRetry;
    }

    /**
     * set retry request max count（default count is 2）
     * @param maxErrorRetry
     */
    public void setMaxErrorRetry(int maxErrorRetry) {
        this.maxErrorRetry = maxErrorRetry;
    }

    /**
     * set CNAME ExcludeList。
     * @param customCnameExcludeList CNAME ExcludeList
     */
    public void setCustomCnameExcludeList(List<String> customCnameExcludeList) {
        if (customCnameExcludeList == null || customCnameExcludeList.size()==0) {
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

    /**
     * get CNAME ExcludeList（cannot modify）
     * The domain name with the list element as the suffix will not be parsed by CNAME。
     * @return CNAME ExcludeList。
     */
    public List<String> getCustomCnameExcludeList() {
        return Collections.unmodifiableList(this.customCnameExcludeList);
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
}
