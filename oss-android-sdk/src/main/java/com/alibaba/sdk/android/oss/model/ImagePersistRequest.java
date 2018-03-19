package com.alibaba.sdk.android.oss.model;

/**
 * Created by huaixu on 2018/1/29.
 */

public class ImagePersistRequest extends OSSRequest {

    public String mFromBucket;

    public String mFromObjectkey;

    public String mToBucketName;

    public String mToObjectKey;

    public String mAction;


    public ImagePersistRequest(String fromBucket, String fromObjectKey, String toBucketName, String mToObjectKey, String action) {
        this.mFromBucket = fromBucket;
        this.mFromObjectkey = fromObjectKey;
        this.mToBucketName = toBucketName;
        this.mToObjectKey = mToObjectKey;
        this.mAction = action;
    }

}
