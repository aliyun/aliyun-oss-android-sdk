package com.alibaba.sdk.android;

import android.os.Environment;
import android.test.AndroidTestCase;

import com.alibaba.sdk.android.oss.ClientConfiguration;
import com.alibaba.sdk.android.oss.ClientException;
import com.alibaba.sdk.android.oss.common.OSSLog;
import com.alibaba.sdk.android.oss.common.OSSLogToFileUtils;

import java.io.File;
import java.io.IOException;

/**
 * Created by jingdan on 2017/8/16.
 */
public class OSSWriteLogTest extends AndroidTestCase {
    private static final long MAX_LOG_SIZE = 5 * 1024 * 1024;

    @Override
    public void setUp() throws Exception {
        OSSLogToFileUtils.reset();
        OSSTestConfig.instance(getContext());
        OSSLog.enableLog();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        OSSLog.logDebug("tearDown", false);
        OSSLogToFileUtils.getInstance().setUseSdCard(true);
    }

    public void testWriteLogWithOutSdCard() throws Exception {
        OSSLogToFileUtils.reset();
        OSSLogToFileUtils.getInstance().setUseSdCard(false);
        ClientConfiguration defaultConf = ClientConfiguration.getDefaultConf();
        defaultConf.setMaxLogSize(MAX_LOG_SIZE);
        OSSLogToFileUtils.init(getContext(), defaultConf);
        Thread.sleep(1000);
        OSSLog.logDebug("testWriteLogWithOutSdCard", true);

        Thread.sleep(2000l);
        long fileSize = OSSLogToFileUtils.getLocalLogFileSize();
        assertEquals(true, fileSize > 0);
    }

    //测试日志的超过上限，重置case
    public void testWriteLogLogic() throws Exception {
        OSSLogToFileUtils.reset();
        OSSLogToFileUtils.getInstance().setUseSdCard(true);
        long maxsize = 2 * 1024;
        ClientConfiguration defaultConf = ClientConfiguration.getDefaultConf();
        defaultConf.setMaxLogSize(maxsize);
        OSSLogToFileUtils.init(getContext(), defaultConf);
        Thread.sleep(1000);
        OSSLog.logDebug("testWriteLogLogic--start-----", false);

        long fileSize = 0;

        while (fileSize < maxsize) {
            String log = "testWriteLogLogictestWriteLogLogictestWriteLogLogictestWriteLogLogic" +
                    "testWriteLogLogictestWriteLogLogictestWriteLogLogictestWriteLogLogictestWriteLogLogic" +
                    "testWriteLogLogictestWriteLogLogictestWriteLogLogictestWriteLogLogictestWriteLogLogic" +
                    "testWriteLogLogictestWriteLogLogictestWriteLogLogictestWriteLogLogictestWriteLogLogictest" +
                    "testWriteLogLogictestWriteLogLogictestWriteLogLogictestWriteLogLogictestWriteLogLogictestWr";
            OSSLog.logDebug(log);
            Thread.sleep(2000l);
            fileSize = OSSLogToFileUtils.getLocalLogFileSize();
        }

        assertEquals(true, fileSize > maxsize);

        OSSLog.logDebug("testWriteLogLogic--end-----", false);
    }

    //空文件下写日志case
    public void testWriteLogWithOutFile() throws Exception {
        ClientConfiguration defaultConf = ClientConfiguration.getDefaultConf();
        defaultConf.setMaxLogSize(MAX_LOG_SIZE);
        OSSLogToFileUtils.init(getContext(), defaultConf);
        Thread.sleep(1000);
        OSSLogToFileUtils.getInstance().deleteLogFile();

        OSSLogToFileUtils.reset();
        OSSLogToFileUtils.init(getContext(), defaultConf);
        Thread.sleep(1000);

        OSSLog.logDebug("testWriteLogWithOutFile", true);
        IOException ioException = new IOException();
        ClientException e = new ClientException(ioException);
        OSSLog.logThrowable2Local(e);

        Thread.sleep(2000l);

        long fileSize = OSSLogToFileUtils.getLocalLogFileSize();
        assertEquals(true, fileSize > 0);
    }

    //空目录下写日志case
    public void testWriteLogWithOutFileDir() throws Exception {
        ClientConfiguration defaultConf = ClientConfiguration.getDefaultConf();
        defaultConf.setMaxLogSize(MAX_LOG_SIZE);
        OSSLogToFileUtils.init(getContext(), defaultConf);
        Thread.sleep(1000);
        OSSLogToFileUtils.getInstance().deleteLogFileDir();

        OSSLogToFileUtils.reset();
        OSSLogToFileUtils.init(getContext(), defaultConf);
        Thread.sleep(1000);
        OSSLog.logWarn("testWriteLogWithOutFileDir");
        ClientException e = new ClientException("xxx");
        OSSLog.logThrowable2Local(e);

        Thread.sleep(2000l);
        long fileSize = OSSLogToFileUtils.getLocalLogFileSize();
        assertEquals(true, fileSize > 0);
    }

    //测试超过日志上限继续写日志
    public void testWriteLogWithOutFileMinStore() throws Exception {
        ClientConfiguration defaultConf = ClientConfiguration.getDefaultConf();
        defaultConf.setMaxLogSize(5);
        OSSLogToFileUtils.init(getContext(), defaultConf);
        Thread.sleep(1000);
        OSSLogToFileUtils.getInstance().deleteLogFileDir();

        OSSLogToFileUtils.reset();
        OSSLogToFileUtils.init(getContext(), defaultConf);
        Thread.sleep(1000);
        OSSLog.logDebug("testWriteLogWithOutFileMinStore", true);

        Thread.sleep(2000l);
        long fileSize = OSSLogToFileUtils.getLocalLogFileSize();
        assertEquals(true, fileSize > 0);
    }

    public void testCreateNewFileWithNoFileError() throws Exception {
        ClientConfiguration defaultConf = ClientConfiguration.getDefaultConf();
        defaultConf.setMaxLogSize(MAX_LOG_SIZE);
        OSSLogToFileUtils.init(getContext(), defaultConf);
        Thread.sleep(1000);
        OSSLogToFileUtils.getInstance().deleteLogFile();
        OSSLogToFileUtils.getInstance().resetLogFile();

        OSSLogToFileUtils.reset();
        OSSLogToFileUtils.init(getContext(), defaultConf);
        Thread.sleep(1000);
        OSSLog.logDebug("testCreateNewFileWithNoFileError", true);
        Thread.sleep(2000l);

        long fileSize = OSSLogToFileUtils.getLocalLogFileSize();
        assertEquals(true, fileSize > 0);
    }

    public void testCreateNewFileWithNoFileDirError() throws Exception {
        OSSLogToFileUtils.reset();
        ClientConfiguration defaultConf = ClientConfiguration.getDefaultConf();
        defaultConf.setMaxLogSize(MAX_LOG_SIZE);
        OSSLogToFileUtils.init(getContext(), defaultConf);
        Thread.sleep(1000);
        OSSLogToFileUtils.getInstance().deleteLogFileDir();
        OSSLogToFileUtils.getInstance().resetLogFile();

        OSSLog.logDebug("testCreateNewFileWithNoFileDirError", true);

        Thread.sleep(2000l);
        long fileSize = OSSLogToFileUtils.getLocalLogFileSize();
        assertEquals(true, fileSize > 0);
    }

    public void testDisableLog() throws Exception {
        OSSLog.disableLog();

        ClientConfiguration defaultConf = ClientConfiguration.getDefaultConf();
        defaultConf.setMaxLogSize(MAX_LOG_SIZE);
        OSSLogToFileUtils.init(getContext(), defaultConf);
        Thread.sleep(1000);
        OSSLogToFileUtils.getInstance().resetLogFile();

        OSSLog.logDebug("testDisableLog");


        long fileSize = OSSLogToFileUtils.getLocalLogFileSize();
        assertEquals(true, fileSize == 0);

        OSSLog.enableLog();
    }

    public void testCreateFileError() throws Exception {
        File file = new File(Environment.getExternalStorageDirectory().getPath() + File.separator
                + "OSSLog" + File.separator + "logs.csv");
        ClientConfiguration defaultConf = ClientConfiguration.getDefaultConf();
        defaultConf.setMaxLogSize(MAX_LOG_SIZE);
        OSSLogToFileUtils.init(getContext(), defaultConf);
        Thread.sleep(1000);
        OSSLogToFileUtils.getInstance().deleteLogFileDir();
        OSSLogToFileUtils.getInstance().createNewFile(file);
        assertEquals(true, !file.exists());
    }

}
