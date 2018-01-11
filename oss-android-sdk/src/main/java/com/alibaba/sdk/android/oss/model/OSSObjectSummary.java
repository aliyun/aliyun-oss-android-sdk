package com.alibaba.sdk.android.oss.model;

import java.util.Date;

/**
 * OSSObject summary class definition.
 */
public class OSSObjectSummary {

    /**
     * The name of the bucket in which this object is stored
     */
    private String bucketName;

    /**
     * The key under which this object is stored
     */
    private String key;

    private String type;

    private String eTag;

    private long size;

    private Date lastModified;

    private String storageClass;

    /**
     * {@link Owner}
     */
    private Owner owner;

    /**
     * Creates a new {@link OSSObjectSummary}
     */
    public OSSObjectSummary() {
    }

    /**
     * Gets the bucket name.
     *
     * @return The bucket name
     */
    public String getBucketName() {
        return bucketName;
    }

    /**
     * Sets the bucket name
     *
     * @param bucketName The bucket name
     */
    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    /**
     * Gets Object key
     *
     * @return Object key
     */
    public String getKey() {
        return key;
    }

    /**
     * Sets Object key
     *
     * @param key Object Key.
     */
    public void setKey(String key) {
        this.key = key;
    }

    /**
     * Gets the ETag which is the object's 128 bit MD5 digest in hex encoding.
     *
     * @return The 128 bit MD5 digest in hex encoding.
     */
    public String getETag() {
        return eTag;
    }

    /**
     * Sets the ETag which is the object's 128 bit MD5 digest in hex encoding.
     *
     * @param eTag The 128 bit MD5 digest in hex encoding.
     */
    public void setETag(String eTag) {
        this.eTag = eTag;
    }

    /**
     * Gets Object size in byte
     *
     * @return Object size in byte
     */
    public long getSize() {
        return size;
    }

    /**
     * Sets Object size in byte
     *
     * @param size Object size in byte
     */
    public void setSize(long size) {
        this.size = size;
    }

    /**
     * Gets the last modified time of the object.
     *
     * @return The object's last modified time
     */
    public Date getLastModified() {
        return lastModified;
    }

    /**
     * Sets the last modified time of the object.
     *
     * @param lastModified The object's last modified time
     */
    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    /**
     * Gets the object's storage class (Standard, IA, Archive)
     *
     * @return The object's storage class
     */
    public String getStorageClass() {
        return storageClass;
    }

    /**
     * Sets the object's storage class
     *
     * @param storageClass Object storage class
     */
    public void setStorageClass(String storageClass) {
        this.storageClass = storageClass;
    }

    /**
     * Gets Object type
     *
     * @return Object type
     */
    public String getType() {
        return type;
    }

    /**
     * Sets object type
     *
     * @param type Object type
     */
    public void setType(String type) {
        this.type = type;
    }

    public Owner getOwner() {
        return owner;
    }

    public void setOwner(Owner owner) {
        this.owner = owner;
    }
}
