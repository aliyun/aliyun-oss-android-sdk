package com.alibaba.sdk.android;

import android.test.AndroidTestCase;

import com.alibaba.sdk.android.oss.common.OSSLog;
import com.alibaba.sdk.android.oss.internal.database.ResumableDatabase;
import com.alibaba.sdk.android.oss.model.PartETag;

/**
 * Created by jingdan on 2017/12/5.
 */

public class ResumableDatabaseTest extends AndroidTestCase{

    private ResumableDatabase database;

    @Override
    protected void setUp() throws Exception {
        database = new ResumableDatabase(getContext());
    }

    @Override
    protected void tearDown() throws Exception {
        long l1 = database.deletePartInfoData("0004B9894A22E5B1888A1E29F8236E2D");
        assertTrue(l1>0);
        database.close();
    }

    public void testAddPartDataAndQuery() {
        long l = addPart();
        assertTrue(l != -1);

        long partCRC64 = database.getPartCRC64(1, "0004B9894A22E5B1888A1E29F8236E2D");
        assertTrue(partCRC64 > 0);
    }

    private long addPart(){
        PartETag eTag = new PartETag(1,"sdssasda");
        eTag.setCrc64(12121212l);
        eTag.setPartSize(1024l);
        return database.addPart(eTag, "0004B9894A22E5B1888A1E29F8236E2D");
    }
}
