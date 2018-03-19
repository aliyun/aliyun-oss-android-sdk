package com.alibaba.sdk.android.oss.model;

import com.alibaba.sdk.android.oss.common.HttpMethod;

import java.util.HashMap;
import java.util.Map;

/**
 * This class wraps all the information needed to generate a presigned URl.
 * And it's not the real "request" class.
 */
public class GeneratePresignedUrlRequest {

    /**
     * The HTTP method (GET, PUT, DELETE, HEAD) to be used in this request and when the pre-signed URL is used
     */
    private HttpMethod method;

    /**
     * The name of the bucket involved in this request
     */
    private String bucketName;

    /**
     * The key of the object involved in this request
     */
    private String key;

    /**
     * process
     */
    private String process;

    /**
     * An optional expiration date at which point the generated pre-signed URL
     * will no longer be accepted by OSS. If not specified, a default
     * value will be supplied.
     */
    private long expiration;

    /**
     * Content-Type to url sign
     */
    private String contentType;

    /**
     * Content-MD5
     */
    private String contentMD5;

    private Map<String, String> queryParam = new HashMap<String, String>();

    /**
     * Constructor with GET as the httpMethod
     *
     * @param bucketName Bucket name.
     * @param key        Object key.
     */
    public GeneratePresignedUrlRequest(String bucketName, String key) {
        this(bucketName, key, 60 * 60);
    }

    /**
     * Constructor.
     *
     * @param bucketName Bucket name.
     * @param key        Object key.
     * @param expiration
     */
    public GeneratePresignedUrlRequest(String bucketName, String key, long expiration) {
        this(bucketName, key, 60 * 60, HttpMethod.GET);
    }

    /**
     * Constructor.
     *
     * @param bucketName Bucket name.
     * @param key        Object key.
     * @param expiration
     * @param method     {@link HttpMethod#GET}。
     */
    public GeneratePresignedUrlRequest(String bucketName, String key, long expiration, HttpMethod method) {
        this.bucketName = bucketName;
        this.key = key;
        this.expiration = expiration;
        this.method = method;
    }

    /**
     * Gets the content type header.
     *
     * @return Content-Type Header
     */
    public String getContentType() {
        return this.contentType;
    }

    /**
     * Sets the content-type header which indicates the file's type.
     *
     * @param contentType The file's content type.
     */
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    /**
     * Gets the file's MD5 value.
     *
     * @return Content-MD5
     */
    public String getContentMD5() {
        return this.contentMD5;
    }

    /**
     * Sets the file's MD5 value.
     *
     * @param contentMD5 The target file's MD5 value.
     */
    public void setContentMD5(String contentMD5) {
        this.contentMD5 = contentMD5;
    }

    /**
     * Gets Http method.
     *
     * @return HTTP method.
     */
    public HttpMethod getMethod() {
        return method;
    }

    /**
     * Sets Http method.
     *
     * @param method HTTP method.
     */
    public void setMethod(HttpMethod method) {
        if (method != HttpMethod.GET && method != HttpMethod.PUT)
            throw new IllegalArgumentException("Only GET or PUT is supported!");

        this.method = method;
    }

    /**
     * @return Bucket name
     */
    public String getBucketName() {
        return bucketName;
    }

    /**
     * @param bucketName
     */
    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    /**
     * @return Object key.
     */
    public String getKey() {
        return key;
    }

    /**
     * @param key
     */
    public void setKey(String key) {
        this.key = key;
    }

    /**
     * Gets the expiration time of the Url
     *
     * @return The expiration time of the Url.
     */
    public long getExpiration() {
        return expiration;
    }

    /**
     * Sets the expiration time of the Url
     *
     * @param expiration The expiration time of the Url.
     */
    public void setExpiration(long expiration) {
        this.expiration = expiration;
    }


    /**
     * Gets the query parameters.
     *
     * @return Query parameters.
     */
    public Map<String, String> getQueryParameter() {
        return this.queryParam;
    }

    /**
     * Sets the query parameters.
     *
     * @param queryParam Query parameters.
     */
    public void setQueryParameter(Map<String, String> queryParam) {
        if (queryParam == null) {
            throw new NullPointerException("The argument 'queryParameter' is null.");
        }
        if (this.queryParam != null && this.queryParam.size() > 0) {
            this.queryParam.clear();
        }
        this.queryParam.putAll(queryParam);
    }

    /**
     * @param key
     * @param value
     */
    public void addQueryParameter(String key, String value) {
        this.queryParam.put(key, value);
    }

    /**
     * Gets the process header.
     *
     * @return The process header.
     */
    public String getProcess() {
        return process;
    }

    /**
     * Sets the process header.
     *
     * @param process The process header.
     */
    public void setProcess(String process) {
        this.process = process;
    }
}
