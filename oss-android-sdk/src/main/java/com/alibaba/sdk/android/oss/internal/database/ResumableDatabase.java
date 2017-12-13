package com.alibaba.sdk.android.oss.internal.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.alibaba.sdk.android.oss.common.OSSLog;
import com.alibaba.sdk.android.oss.common.OSSSQLiteHelper;
import com.alibaba.sdk.android.oss.model.PartETag;

/**
 * Created by jingdan on 2017/12/5.
 */

public class ResumableDatabase {

    private SQLiteDatabase mDatabase;

    public ResumableDatabase(Context context) {
        OSSSQLiteHelper dataHelper = new OSSSQLiteHelper(context);
        mDatabase = dataHelper.getWritableDatabase();
    }

    public long addPart(PartETag partETag, String uploadId) {
        ContentValues values = new ContentValues();
        values.put("upload_id", uploadId);
        values.put("num", partETag.getPartNumber());
        values.put("size", partETag.getPartSize());
        values.put("etag", partETag.getETag());
        values.put("crc64", partETag.getCrc64());
        return mDatabase.insert(OSSSQLiteHelper.TABLE_NAME_PART_INFO, null, values);
    }

    public long deletePartInfoData(String uploadId) {
        String[] args = {uploadId};
        return mDatabase.delete(OSSSQLiteHelper.TABLE_NAME_PART_INFO, "upload_id = ?", args);
    }

    public long getPartCRC64(int partNum, String uploadId) {
        String[] args = {String.valueOf(partNum), uploadId};
        Cursor query = mDatabase.query(OSSSQLiteHelper.TABLE_NAME_PART_INFO,
                new String[]{"crc64"}, "num=? and upload_id=?",
                args, null, null, null, null);
        int count = query.getCount();
        long crc = 0;
        if (count >0) {
            query.moveToNext();
            crc = query.getLong(0);
        }
        OSSLog.logDebug("part_crc: " + crc + " num: "+ partNum + " uploadId: "+ uploadId);
        query.close();
        return crc;
    }

    public void close() {
        mDatabase.close();
        mDatabase = null;
    }
}
