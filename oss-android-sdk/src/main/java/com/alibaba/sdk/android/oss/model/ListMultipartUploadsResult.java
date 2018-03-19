package com.alibaba.sdk.android.oss.model;

import android.util.Xml;

import com.alibaba.sdk.android.oss.common.utils.DateUtil;
import com.alibaba.sdk.android.oss.common.utils.OSSUtils;
import com.alibaba.sdk.android.oss.internal.ResponseMessage;

import org.xmlpull.v1.XmlPullParser;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jingdan on 2018/2/13.
 */

public class ListMultipartUploadsResult extends OSSResult {

    private String bucketName;

    private String keyMarker;

    private String delimiter;

    private String prefix;

    private String uploadIdMarker;

    private int maxUploads;

    private boolean isTruncated;

    private String nextKeyMarker;

    private String nextUploadIdMarker;

    private List<MultipartUpload> multipartUploads = new ArrayList<MultipartUpload>();

    private List<String> commonPrefixes = new ArrayList<String>();

    public String getBucketName() {
        return bucketName;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    public String getKeyMarker() {
        return keyMarker;
    }

    public void setKeyMarker(String keyMarker) {
        this.keyMarker = keyMarker;
    }

    public String getUploadIdMarker() {
        return uploadIdMarker;
    }

    public void setUploadIdMarker(String uploadIdMarker) {
        this.uploadIdMarker = uploadIdMarker;
    }

    public String getNextKeyMarker() {
        return nextKeyMarker;
    }

    public void setNextKeyMarker(String nextKeyMarker) {
        this.nextKeyMarker = nextKeyMarker;
    }

    public String getNextUploadIdMarker() {
        return nextUploadIdMarker;
    }

    public void setNextUploadIdMarker(String nextUploadIdMarker) {
        this.nextUploadIdMarker = nextUploadIdMarker;
    }

    public int getMaxUploads() {
        return maxUploads;
    }

    public void setMaxUploads(int maxUploads) {
        this.maxUploads = maxUploads;
    }

    public boolean isTruncated() {
        return isTruncated;
    }

    public void setTruncated(boolean isTruncated) {
        this.isTruncated = isTruncated;
    }

    public List<MultipartUpload> getMultipartUploads() {
        return multipartUploads;
    }

    public void setMultipartUploads(List<MultipartUpload> multipartUploads) {
        this.multipartUploads.clear();
        if (multipartUploads != null && !multipartUploads.isEmpty()) {
            this.multipartUploads.addAll(multipartUploads);
        }
    }

    public void addMultipartUpload(MultipartUpload multipartUpload) {
        this.multipartUploads.add(multipartUpload);
    }

    public String getDelimiter() {
        return delimiter;
    }

    public void setDelimiter(String delimiter) {
        this.delimiter = delimiter;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public List<String> getCommonPrefixes() {
        return commonPrefixes;
    }

    public void setCommonPrefixes(List<String> commonPrefixes) {
        this.commonPrefixes.clear();
        if (commonPrefixes != null && !commonPrefixes.isEmpty()) {
            this.commonPrefixes.addAll(commonPrefixes);
        }
    }

    public void addCommonPrefix(String commonPrefix) {
        this.commonPrefixes.add(commonPrefix);
    }

    public ListMultipartUploadsResult parseData(ResponseMessage responseMessage) throws Exception {
        List<MultipartUpload> uploadList = new ArrayList<MultipartUpload>();
        MultipartUpload upload = null;
        boolean isCommonPrefixes = false;
        XmlPullParser parser = Xml.newPullParser();
        parser.setInput(responseMessage.getContent(), "utf-8");
        int eventType = parser.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT) {
            switch (eventType) {
                case XmlPullParser.START_TAG:
                    String name = parser.getName();
                    if ("Bucket".equals(name)) {
                        setBucketName(parser.nextText());
                    } else if ("Delimiter".equals(name)) {
                        setDelimiter(parser.nextText());
                    } else if ("Prefix".equals(name)) {
                        if (isCommonPrefixes) {
                            String commonPrefix = parser.nextText();
                            if (!OSSUtils.isEmptyString(commonPrefix)) {
                                addCommonPrefix(commonPrefix);
                            }
                        } else {
                            setPrefix(parser.nextText());
                        }
                    } else if ("MaxUploads".equals(name)) {
                        String maxUploads = parser.nextText();
                        if (!OSSUtils.isEmptyString(maxUploads)) {
                            setMaxUploads(Integer.valueOf(maxUploads));
                        }
                    } else if ("IsTruncated".equals(name)) {
                        String isTruncated = parser.nextText();
                        if (!OSSUtils.isEmptyString(isTruncated)) {
                            setTruncated(Boolean.valueOf(isTruncated));
                        }
                    } else if ("KeyMarker".equals(name)) {
                        setKeyMarker(parser.nextText());
                    } else if ("UploadIdMarker".equals(name)) {
                        setUploadIdMarker(parser.nextText());
                    } else if ("NextKeyMarker".equals(name)) {
                        setNextKeyMarker(parser.nextText());
                    } else if ("NextUploadIdMarker".equals(name)) {
                        setNextUploadIdMarker(parser.nextText());
                    } else if ("Upload".equals(name)) {
                        upload = new MultipartUpload();
                    } else if ("Key".equals(name)) {
                        upload.setKey(parser.nextText());
                    } else if ("UploadId".equals(name)) {
                        upload.setUploadId(parser.nextText());
                    } else if ("Initiated".equals(name)) {
                        upload.setInitiated(DateUtil.parseIso8601Date(parser.nextText()));
                    } else if ("StorageClass".equals(name)) {
                        upload.setStorageClass(parser.nextText());
                    } else if ("CommonPrefixes".equals(name)) {
                        isCommonPrefixes = true;
                    }
                    break;
                case XmlPullParser.END_TAG:
                    if ("Upload".equals(parser.getName())) {
                        uploadList.add(upload);
                    } else if ("CommonPrefixes".equals(parser.getName())) {
                        isCommonPrefixes = false;
                    }
                    break;
            }

            eventType = parser.next();
            if (eventType == XmlPullParser.TEXT) {
                eventType = parser.next();
            }
        }

        if (uploadList.size() > 0) {
            setMultipartUploads(uploadList);
        }

        return this;
    }
}
