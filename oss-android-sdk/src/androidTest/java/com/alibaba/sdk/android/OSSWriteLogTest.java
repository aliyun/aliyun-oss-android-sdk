package com.alibaba.sdk.android;

import android.content.Context;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.test.AndroidTestCase;
import android.util.Log;

import com.alibaba.sdk.android.oss.ClientConfiguration;
import com.alibaba.sdk.android.oss.ClientException;
import com.alibaba.sdk.android.oss.OSS;
import com.alibaba.sdk.android.oss.OSSClient;
import com.alibaba.sdk.android.oss.OSSLogToFileUtils;
import com.alibaba.sdk.android.oss.common.OSSLog;

import java.io.File;

/**
 * Created by jingdan on 2017/8/16.
 */
public class OSSWriteLogTest extends AndroidTestCase {
    private static final long MAX_LOG_SIZE = 10 * 1024;

    @Override
    public void setUp() throws Exception {
        OSSLog.enableLog();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        OSSLog.logD("tearDown",false);
        OSSLogToFileUtils.getInstance().resetLogFile();
        OSSLogToFileUtils.getInstance().setUseSdCard(true);
    }

    public void testWriteLogWithOutSdCard(){
        OSSLogToFileUtils.reset();
        OSSLogToFileUtils.getInstance().setUseSdCard(false);
        ClientConfiguration defaultConf = ClientConfiguration.getDefaultConf();
        defaultConf.setMaxLogSize(MAX_LOG_SIZE);
        OSSLogToFileUtils.init(getContext(),defaultConf);
        OSSLog.logD("testWriteLogWithOutSdCard",true);
        assertTrue(true);
    }

    //测试日志的超过上限，重置case
    public void testWriteLogLogic(){
        OSSLogToFileUtils.reset();
        OSSLogToFileUtils.getInstance().setUseSdCard(true);
        long maxsize = 2*1024;
        ClientConfiguration defaultConf = ClientConfiguration.getDefaultConf();
        defaultConf.setMaxLogSize(maxsize);
        OSSLogToFileUtils.init(getContext(),defaultConf);
        OSSLog.logD("testWriteLogLogic--start-----",false);

        long fileSize = OSSLogToFileUtils.getLocalLogFileSize();

        while (fileSize < maxsize){
            ClientException e = new ClientException();
            OSSLog.logThrowable2Local(e);
            fileSize = OSSLogToFileUtils.getLocalLogFileSize();
        }

        assertEquals(true,fileSize>maxsize);

        OSSLog.logD("testWriteLogLogic--end-----",false);
    }

    //空文件下写日志case
    public void testWriteLogWithOutFile(){
        OSSLogToFileUtils.reset();
        ClientConfiguration defaultConf = ClientConfiguration.getDefaultConf();
        defaultConf.setMaxLogSize(MAX_LOG_SIZE);
        OSSLogToFileUtils.init(getContext(),defaultConf);
        OSSLogToFileUtils.getInstance().deleteLogFile();

        OSSLog.logD("testWriteLogWithOutFile",true);
        assertTrue(true);
    }

    //空目录下写日志case
    public void testWriteLogWithOutFileDir(){
        OSSLogToFileUtils.reset();
        ClientConfiguration defaultConf = ClientConfiguration.getDefaultConf();
        defaultConf.setMaxLogSize(MAX_LOG_SIZE);
        OSSLogToFileUtils.init(getContext(),defaultConf);
        OSSLogToFileUtils.getInstance().deleteLogFileDir();

        OSSLog.logD("testWriteLogWithOutFile",true);
        assertTrue(true);
    }

    //测试存储空间不足时
    public void testWriteLogWithOutFileMinStore(){
        OSSLogToFileUtils.reset();
        ClientConfiguration defaultConf = ClientConfiguration.getDefaultConf();
        defaultConf.setMaxLogSize(5);
        OSSLogToFileUtils.init(getContext(),defaultConf);
        OSSLogToFileUtils.getInstance().deleteLogFileDir();

        OSSLog.logD("testWriteLogWithOutFile",true);
        assertTrue(true);
    }

    public void testCreateNewFileWithNoFileError(){
        OSSLogToFileUtils.reset();
        ClientConfiguration defaultConf = ClientConfiguration.getDefaultConf();
        defaultConf.setMaxLogSize(MAX_LOG_SIZE);
        OSSLogToFileUtils.init(getContext(),defaultConf);
        OSSLogToFileUtils.getInstance().deleteLogFile();
        OSSLogToFileUtils.getInstance().resetLogFile();
        OSSLog.logD("testCreateNewFileWithNoFileError");
        assertTrue(true);
    }

    public void testCreateNewFileWithNoFileDirError(){
        OSSLogToFileUtils.reset();
        ClientConfiguration defaultConf = ClientConfiguration.getDefaultConf();
        defaultConf.setMaxLogSize(MAX_LOG_SIZE);
        OSSLogToFileUtils.init(getContext(),defaultConf);
        OSSLogToFileUtils.getInstance().deleteLogFileDir();
        OSSLogToFileUtils.getInstance().resetLogFile();
        OSSLog.logD("testCreateNewFileWithNoFileDirError");
        assertTrue(true);
    }


}
