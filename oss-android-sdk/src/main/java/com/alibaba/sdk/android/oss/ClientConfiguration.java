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
 * 访问阿里云服务的客户端配置。
 */
public class ClientConfiguration {

    private static final String DEFAULT_USER_AGENT = VersionInfoUtils.getDefaultUserAgent();
    private static final int DEFAULT_MAX_RETRIES = 2;

    private int maxConcurrentRequest = 5;
    private int socketTimeout = 15 * 1000;
    private int connectionTimeout = 15 * 1000;
    private int maxErrorRetry = DEFAULT_MAX_RETRIES;
    private List<String> customCnameExcludeList = new ArrayList<String>();
    private String proxyHost;
    private int proxyPort;

    /**
     * 构造新实例。
     */
    public ClientConfiguration(){
    }

    /**
     * 获取一个默认实例
     */
    public static ClientConfiguration getDefaultConf() {
        return new ClientConfiguration();
    }

    /**
     * 返回最大的并发HTTP请求数
     * @return
     */
    public int getMaxConcurrentRequest() {
        return maxConcurrentRequest;
    }

    /**
     * 设置允许并发的最大HTTP请求数
     * @param maxConcurrentRequest
     *          最大HTTP并发请求数
     */
    public void setMaxConcurrentRequest(int maxConcurrentRequest) {
        this.maxConcurrentRequest = maxConcurrentRequest;
    }

    /**
     * 返回通过打开的连接传输数据的超时时间（单位：毫秒）。
     * 0表示无限等待（但不推荐使用）。
     * @return 通过打开的连接传输数据的超时时间（单位：毫秒）。
     */
    public int getSocketTimeout() {
        return socketTimeout;
    }

    /**
     * 设置通过打开的连接传输数据的超时时间（单位：毫秒）。
     * 0表示无限等待（但不推荐使用）。
     * @param socketTimeout
     *          通过打开的连接传输数据的超时时间（单位：毫秒）。
     */
    public void setSocketTimeout(int socketTimeout) {
        this.socketTimeout = socketTimeout;
    }

    /**
     * 返回建立连接的超时时间（单位：毫秒）。
     * @return 建立连接的超时时间（单位：毫秒）。
     */
    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    /**
     * 设置建立连接的超时时间（单位：毫秒）。
     * @param connectionTimeout
     *          建立连接的超时时间（单位：毫秒）。
     */
    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    /**
     * 返回一个值表示当可重试的请求失败后最大的重试次数。（默认值为2）
     * @return 当可重试的请求失败后最大的重试次数。
     */
    public int getMaxErrorRetry() {
        return maxErrorRetry;
    }

    /**
     * 设置一个值表示当可重试的请求失败后最大的重试次数。（默认值为2）
     * @param maxErrorRetry
     *          当可重试的请求失败后最大的重试次数。
     */
    public void setMaxErrorRetry(int maxErrorRetry) {
        this.maxErrorRetry = maxErrorRetry;
    }

    /**
     * 设置CNAME排除列表。
     * @param customCnameExcludeList CNAME排除列表。
     */
    public void setCustomCnameExcludeList(List<String> customCnameExcludeList) {
        if (customCnameExcludeList == null) {
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
     * 获取CNAME排除列表（不可修改），以列表元素作为后缀的域名将不进行CNAME解析。
     * @return CNAME排除列表。
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
