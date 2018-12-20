package com.alibaba.sdk.android.oss.model;

public class BucketLifecycleRule {
    private String mIdentifier;
    private String mPrefix;
    private boolean mStatus;
    private String mDays;
    private String mExpireDate;
    private String mMultipartDays;
    private String mMultipartExpireDate;
    private String mIADays;
    private String mIAExpireDate;
    private String mArchiveDays;
    private String mArchiveExpireDate;

    public String getIdentifier() {
        return mIdentifier;
    }

    public void setIdentifier(String identifier) {
        this.mIdentifier = identifier;
    }

    public String getPrefix() {
        return mPrefix;
    }

    public void setPrefix(String prefix) {
        this.mPrefix = prefix;
    }

    public boolean getStatus() {
        return mStatus;
    }

    public void setStatus(boolean status) {
        this.mStatus = status;
    }

    public String getDays() {
        return mDays;
    }

    public void setDays(String days) {
        this.mDays = days;
    }

    public String getExpireDate() {
        return mExpireDate;
    }

    public void setExpireDate(String expireDate) {
        this.mExpireDate = expireDate;
    }

    public String getMultipartDays() {
        return mMultipartDays;
    }

    public void setMultipartDays(String multipartDays) {
        this.mMultipartDays = multipartDays;
    }

    public String getMultipartExpireDate() {
        return mMultipartExpireDate;
    }

    public void setMultipartExpireDate(String multipartExpireDate) {
        this.mMultipartExpireDate = multipartExpireDate;
    }

    public String getIADays() {
        return mIADays;
    }

    public void setIADays(String iaDays) {
        this.mIADays = iaDays;
    }

    public String getIAExpireDate() {
        return mIAExpireDate;
    }

    public void setIAExpireDate(String iaExpireDate) {
        this.mIAExpireDate = iaExpireDate;
    }

    public String getArchiveDays() {
        return mArchiveDays;
    }

    public void setArchiveDays(String archiveDays) {
        this.mArchiveDays = archiveDays;
    }

    public String getArchiveExpireDate() {
        return mArchiveExpireDate;
    }

    public void setArchiveExpireDate(String archiveExpireDate) {
        this.mArchiveExpireDate = archiveExpireDate;
    }
}
