package com.alibaba.sdk.android.oss.common;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by jingdan on 2017/12/5.
 */

public class OSSSQLiteHelper extends SQLiteOpenHelper {

    public final static String TABLE_NAME_PART_INFO = "part_info";
    private final static String CREATE_TABLE_PART_INFO =
            "create table if not exists " + TABLE_NAME_PART_INFO + "("
                    + "id INTEGER primary key,"
                    + "upload_id VARCHAR(255),"
                    + "num INTEGER,"
                    + "crc64 INTEGER,"
                    + "size INTEGER,"
                    + "etag VARCHAR(255))";


    public OSSSQLiteHelper(Context context) {
        this(context, "oss_android_sdk.db", null, 1);
    }

    public OSSSQLiteHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_PART_INFO);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}
