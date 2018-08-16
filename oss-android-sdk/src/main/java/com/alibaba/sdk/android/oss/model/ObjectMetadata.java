package com.alibaba.sdk.android.oss.model;

import com.alibaba.sdk.android.oss.common.OSSConstants;
import com.alibaba.sdk.android.oss.common.OSSHeaders;
import com.alibaba.sdk.android.oss.common.utils.CaseInsensitiveHashMap;
import com.alibaba.sdk.android.oss.common.utils.DateUtil;

import java.text.ParseException;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * OSS object metadata class definition.
 * It includes user's custom metadata as well as standard HTTP headers (such as Content-Length, ETag, etc)
 */
public class ObjectMetadata {

    public static final String AES_256_SERVER_SIDE_ENCRYPTION = "AES256";
    // User's custom metadata dictionary. All keys  will be prefixed with x-oss-meta-in the HTTP headers.
    // But the keys in this dictionary does not include x-oss-meta-in.
    private Map<String, String> userMetadata = new CaseInsensitiveHashMap<String, String>();
    // Standard metadata
    private Map<String, Object> metadata = new CaseInsensitiveHashMap<String, Object>();

    /**
     * <p>
     * Gets the user's custom metadata.
     * </p>
     * <p>
     * OSS SDK will append x-oss-meta- as the header prefix for all custom metadata.
     * But users do not need to specify this prefix through any API of this class.
     * Meanwhile the metadata's key is case insensitive and all metadata keys returned from OSS is
     * in lowercase.
     * </p>
     *
     * @return User's custom metadata.
     */
    public Map<String, String> getUserMetadata() {
        return userMetadata;
    }

    /**
     * Sets user's custom metadata.
     *
     * @param userMetadata User's custom metadata
     */
    public void setUserMetadata(Map<String, String> userMetadata) {
        this.userMetadata.clear();
        if (userMetadata != null && !userMetadata.isEmpty()) {
            this.userMetadata.putAll(userMetadata);
        }
    }

    /**
     * Sets header (SDK internal usage only).
     *
     * @param key   Request Key.
     * @param value Request Value.
     */
    public void setHeader(String key, Object value) {
        metadata.put(key, value);
    }

    /**
     * Adds a custom metadata.
     *
     * @param key   metadata key
     *              This key should not include the prefix "x-oss-meta-" as the OSS SDK will add it automatically.
     * @param value metadata value
     */
    public void addUserMetadata(String key, String value) {
        this.userMetadata.put(key, value);
    }

    /**
     * Gets the Last-Modified value, which is the time of the object's last update.
     *
     * @return The object's last modified time.
     */
    public Date getLastModified() {
        return (Date) metadata.get(OSSHeaders.LAST_MODIFIED);
    }

    /**
     * Sets the Last-Modified value, which is the time of the object's last update(SDK internal only).
     *
     * @param lastModified The object's last modified time.
     */
    public void setLastModified(Date lastModified) {
        metadata.put(OSSHeaders.LAST_MODIFIED, lastModified);
    }

    /**
     * Gets Expires header value in Rfc822 format (EEE, dd MMM yyyy HH:mm:ss 'GMT'")
     * If the 'expires' header was not assigned with value, returns null.
     *
     * @return Expires header value in Rfc822 format.
     * @throws ParseException unable to parse the Expires value into Rfc822 format
     */
    public Date getExpirationTime() throws ParseException {
        return DateUtil.parseRfc822Date((String) metadata.get(OSSHeaders.EXPIRES));
    }

    /**
     * Sets Expires header value
     *
     * @param expirationTime Expires time
     */
    public void setExpirationTime(Date expirationTime) {
        metadata.put(OSSHeaders.EXPIRES, DateUtil.formatRfc822Date(expirationTime));
    }

    /**
     * Gets the raw expires header value without parsing it.
     * If the 'expires' header was not assigned with value, returns null.
     *
     * @return The raw expires header value
     */
    public String getRawExpiresValue() {
        return (String) metadata.get(OSSHeaders.EXPIRES);
    }

    /**
     * Gets Content-Length header value which means the object content's size.
     *
     * @return The value of Content-Length header.
     */
    public long getContentLength() {
        Long contentLength = (Long) metadata.get(OSSHeaders.CONTENT_LENGTH);

        if (contentLength == null) return 0;
        return contentLength.longValue();
    }

    /**
     * Sets Content-Length header value which means the object content's size.
     * The Content-Length header must be specified correctly when uploading an object.
     *
     * @param contentLength Object content length
     * @throws IllegalArgumentException Object content length is more than 5GB or less than 0.
     */
    public void setContentLength(long contentLength) {
        if (contentLength > OSSConstants.DEFAULT_FILE_SIZE_LIMIT) {
            throw new IllegalArgumentException("The content length could not be more than 5GB.");
        }

        metadata.put(OSSHeaders.CONTENT_LENGTH, contentLength);
    }

    /**
     * Gets Content-Type header value in MIME types, which means the object's type.
     *
     * @return The object Content-Type value in MIME types.
     */
    public String getContentType() {
        return (String) metadata.get(OSSHeaders.CONTENT_TYPE);
    }

    /**
     * Sets Content-Type header value in MIME types, which means the object's type.
     *
     * @param contentType The object Content-Type value in MIME types.
     */
    public void setContentType(String contentType) {
        metadata.put(OSSHeaders.CONTENT_TYPE, contentType);
    }

    public String getContentMD5() {
        return (String) metadata.get(OSSHeaders.CONTENT_MD5);
    }

    public void setContentMD5(String contentMD5) {
        metadata.put(OSSHeaders.CONTENT_MD5, contentMD5);
    }

    public String getSHA1() {
        return (String) metadata.get(OSSHeaders.OSS_HASH_SHA1);
    }

    public void setSHA1(String value) {
        metadata.put(OSSHeaders.OSS_HASH_SHA1, value);
    }

    /**
     * Gets Content-Encoding header value which means the object content's encoding method.
     *
     * @return The object content's encoding
     */
    public String getContentEncoding() {
        return (String) metadata.get(OSSHeaders.CONTENT_ENCODING);
    }

    /**
     * Gets Content-Encoding header value which means the object content's encoding method.
     *
     * @param encoding The object content's encoding.
     */
    public void setContentEncoding(String encoding) {
        metadata.put(OSSHeaders.CONTENT_ENCODING, encoding);
    }

    /**
     * Gets Cache-Control header value, which specifies the cache behavior of accessing the object.
     *
     * @return Cache-Control header value
     */
    public String getCacheControl() {
        return (String) metadata.get(OSSHeaders.CACHE_CONTROL);
    }

    /**
     * Sets Cache-Control header value, which specifies the cache behavior of accessing the object.
     *
     * @param cacheControl Cache-Control header value
     */
    public void setCacheControl(String cacheControl) {
        metadata.put(OSSHeaders.CACHE_CONTROL, cacheControl);
    }

    /**
     * Gets Content-Disposition header value, which specifies how MIME agent is going to handle
     * attachments.
     *
     * @return Content-Disposition header value
     */
    public String getContentDisposition() {
        return (String) metadata.get(OSSHeaders.CONTENT_DISPOSITION);
    }

    /**
     * Gets Content-Disposition header value, which specifies how MIME agent is going to handle
     * attachments.
     *
     * @param disposition Content-Disposition header value
     */
    public void setContentDisposition(String disposition) {
        metadata.put(OSSHeaders.CONTENT_DISPOSITION, disposition);
    }

    /**
     * Gets the ETag value which is the 128bit MD5 digest in HEX encoding.
     *
     * @return The ETag value.
     */
    public String getETag() {
        return (String) metadata.get(OSSHeaders.ETAG);
    }

    /**
     * Gets the server side encryption algorithm.
     *
     * @return The server side encryption algorithm. No encryption if it returns null.
     */
    public String getServerSideEncryption() {
        return (String) metadata.get(OSSHeaders.OSS_SERVER_SIDE_ENCRYPTION);
    }

    /**
     * Sets the server side encryption algorithm.
     */
    public void setServerSideEncryption(String serverSideEncryption) {
        metadata.put(OSSHeaders.OSS_SERVER_SIDE_ENCRYPTION, serverSideEncryption);
    }

    /**
     * Gets Object type---Normal or Appendable
     *
     * @return Object type
     */
    public String getObjectType() {
        return (String) metadata.get(OSSHeaders.OSS_OBJECT_TYPE);
    }

    /**
     * Gets the raw metadata dictionary (SDK internal only)
     *
     * @return The raw metadata (SDK internal only)
     */
    public Map<String, Object> getRawMetadata() {
        return Collections.unmodifiableMap(metadata);
    }

    @Override
    public String toString() {
        String s;
        String expirationTimeStr = "";
        try {
            Date expirationTime = getExpirationTime();
            expirationTimeStr = expirationTime.toString();
        } catch (Exception e) {
        }
        s = OSSHeaders.LAST_MODIFIED + ":" + getLastModified() + "\n"
                + OSSHeaders.EXPIRES + ":" + expirationTimeStr + "\n"
                + "rawExpires" + ":" + getRawExpiresValue() + "\n"
                + OSSHeaders.CONTENT_MD5 + ":" + getContentMD5() + "\n"
                + OSSHeaders.OSS_OBJECT_TYPE + ":" + getObjectType() + "\n"
                + OSSHeaders.OSS_SERVER_SIDE_ENCRYPTION + ":" + getServerSideEncryption() + "\n"
                + OSSHeaders.CONTENT_DISPOSITION + ":" + getContentDisposition() + "\n"
                + OSSHeaders.CONTENT_ENCODING + ":" + getContentEncoding() + "\n"
                + OSSHeaders.CACHE_CONTROL + ":" + getCacheControl() + "\n"
                + OSSHeaders.ETAG + ":" + getETag() + "\n";

        return s;
    }
}