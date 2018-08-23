package com.alibaba.sdk.android.oss.common.utils;

import com.alibaba.sdk.android.oss.common.OSSLog;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * @author: zhouzhuo
 * Jun 20, 2015
 */
public class HttpdnsMini {


    private static final String TAG = "HttpDnsMini";
    private static final String SERVER_IP = "203.107.1.1";
    private static final String ACCOUNT_ID = "181345";
    private static final int MAX_THREAD_NUM = 5;
    private static final int RESOLVE_TIMEOUT_IN_SEC = 10;
    private static final int MAX_HOLD_HOST_NUM = 100;
    private static final int EMPTY_RESULT_HOST_TTL = 30;
    private static HttpdnsMini instance;
    private ConcurrentMap<String, HostObject> hostManager = new ConcurrentHashMap<String, HostObject>();
    private ExecutorService pool = Executors.newFixedThreadPool(MAX_THREAD_NUM);

    public boolean isHttp2Test = false;

    private HttpdnsMini() {
    }

    public static HttpdnsMini getInstance() {
        if (instance == null) {
            synchronized (HttpdnsMini.class) {
                if (instance == null) {
                    instance = new HttpdnsMini();
                }
            }
        }
        return instance;
    }

    public String getIpByHostAsync(String hostName) {
        if (isHttp2Test){
            return  "118.178.62.19";
        }
        HostObject host = hostManager.get(hostName);
        if (host == null || host.isExpired()) {
            OSSLog.logDebug("[httpdnsmini] - refresh host: " + hostName);
            pool.submit(new QueryHostTask(hostName));
        }
        if (host != null) {
            return (host.isStillAvailable() ? host.getIp() : null);
        }
        return null;
    }

    class HostObject {

        private String hostName;
        private String ip;
        private long ttl;
        private long queryTime;

        @Override
        public String toString() {
            return "[hostName=" + getHostName() + ", ip=" + ip + ", ttl=" + getTtl() + ", queryTime="
                    + queryTime + "]";
        }

        public boolean isExpired() {
            return getQueryTime() + ttl < System.currentTimeMillis() / 1000;
        }

        // 一个域名解析结果过期后，异步接口仍然可以返回这个结果，但最多可以容忍过期10分钟
        public boolean isStillAvailable() {
            return getQueryTime() + ttl + 10 * 60 > System.currentTimeMillis() / 1000;
        }

        public String getIp() {
            return ip;
        }

        public void setIp(String ip) {
            this.ip = ip;
        }

        public String getHostName() {
            return hostName;
        }

        public void setHostName(String hostName) {
            this.hostName = hostName;
        }

        public long getTtl() {
            return ttl;
        }

        public void setTtl(long ttl) {
            this.ttl = ttl;
        }

        public long getQueryTime() {
            return queryTime;
        }

        public void setQueryTime(long queryTime) {
            this.queryTime = queryTime;
        }
    }

    class QueryHostTask implements Callable<String> {
        private String hostName;
        private boolean hasRetryed = false;

        public QueryHostTask(String hostToQuery) {
            this.hostName = hostToQuery;
        }

        @Override
        public String call() {
            String chooseServerAddress = SERVER_IP;
            String resolveUrl = "http://" + chooseServerAddress + "/" + ACCOUNT_ID + "/d?host=" + hostName;
            InputStream in = null;
            OSSLog.logDebug("[httpdnsmini] - buildUrl: " + resolveUrl);
            try {
                HttpURLConnection conn = (HttpURLConnection) new URL(resolveUrl).openConnection();
                conn.setConnectTimeout(RESOLVE_TIMEOUT_IN_SEC * 1000);
                conn.setReadTimeout(RESOLVE_TIMEOUT_IN_SEC * 1000);
                if (conn.getResponseCode() != 200) {
                    OSSLog.logError("[httpdnsmini] - responseCodeNot 200, but: " + conn.getResponseCode());
                } else {
                    in = conn.getInputStream();
                    BufferedReader streamReader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = streamReader.readLine()) != null) {
                        sb.append(line);
                    }
                    JSONObject json = new JSONObject(sb.toString());
                    String host = json.getString("host");
                    long ttl = json.getLong("ttl");
                    JSONArray ips = json.getJSONArray("ips");
                    OSSLog.logDebug("[httpdnsmini] - ips:" + ips.toString());
                    if (host != null && ips != null && ips.length() > 0) {
                        if (ttl == 0) {
                            // 如果有结果返回，但是ip列表为空，ttl也为空，那默认没有ip就是解析结果，并设置ttl为一个较长的时间
                            // 避免一直请求同一个ip冲击sever
                            ttl = EMPTY_RESULT_HOST_TTL;
                        }
                        HostObject hostObject = new HostObject();
                        String ip = (ips == null) ? null : ips.getString(0);
                        hostObject.setHostName(host);
                        hostObject.setTtl(ttl);
                        hostObject.setIp(ip);
                        hostObject.setQueryTime(System.currentTimeMillis() / 1000);
                        OSSLog.logDebug("[httpdnsmini] - resolve result:" + hostObject.toString());
                        if (hostManager.size() < MAX_HOLD_HOST_NUM) {
                            hostManager.put(hostName, hostObject);
                        }
                        return ip;
                    }
                }
            } catch (Exception e) {
                if (OSSLog.isEnableLog()) {
                    e.printStackTrace();
                    OSSLog.logThrowable2Local(e);
                }
            } finally {
                try {
                    if (in != null) {
                        in.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (!hasRetryed) {
                hasRetryed = true;
                return call();
            }
            return null;
        }
    }
}