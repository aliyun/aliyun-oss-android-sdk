package com.alibaba.sdk.android;

import android.os.Build;
import android.os.Environment;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.alibaba.sdk.android.oss.ClientConfiguration;
import com.alibaba.sdk.android.oss.ClientException;
import com.alibaba.sdk.android.oss.common.OSSLog;
import com.alibaba.sdk.android.oss.common.OSSLogToFileUtils;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.*;

/**
 * Created by jingdan on 2017/8/16.
 */

@RunWith(AndroidJUnit4.class)
public class OSSWriteLogTest {
    private static final long MAX_LOG_SIZE = 5 * 1024 * 1024;

    @Before
    public void setUp() throws Exception {
        OSSLogToFileUtils.reset();
        OSSLog.enableLog();
    }

    @After
    public void tearDown() throws Exception {
        OSSLog.logDebug("tearDown", false);
        OSSLogToFileUtils.getInstance().setUseSdCard(true);
    }

    @Test
    public void testWriteLogWithOutSdCard() throws Exception {
        OSSLogToFileUtils.reset();
        OSSLogToFileUtils.getInstance().setUseSdCard(false);
        ClientConfiguration defaultConf = ClientConfiguration.getDefaultConf();
        defaultConf.setMaxLogSize(MAX_LOG_SIZE);
        OSSLogToFileUtils.init(InstrumentationRegistry.getTargetContext(), defaultConf);
        Thread.sleep(1000);
        OSSLog.logDebug("testWriteLogWithOutSdCard", true);

        Thread.sleep(2000l);
        long fileSize = OSSLogToFileUtils.getLocalLogFileSize();
        assertEquals(true, fileSize > 0);
    }

    //测试日志的超过上限，重置case
    @Test
    public void testWriteLogLogic() throws Exception {
        OSSLogToFileUtils.reset();
        OSSLogToFileUtils.getInstance().setUseSdCard(true);
        long maxsize = 2 * 1024;
        ClientConfiguration defaultConf = ClientConfiguration.getDefaultConf();
        defaultConf.setMaxLogSize(maxsize);
        OSSLogToFileUtils.init(InstrumentationRegistry.getTargetContext(), defaultConf);
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
    @Test
    public void testWriteLogWithOutFile() throws Exception {
        ClientConfiguration defaultConf = ClientConfiguration.getDefaultConf();
        defaultConf.setMaxLogSize(MAX_LOG_SIZE);
        OSSLogToFileUtils.init(InstrumentationRegistry.getTargetContext(), defaultConf);
        Thread.sleep(1000);
        OSSLogToFileUtils.getInstance().deleteLogFile();

        OSSLogToFileUtils.reset();
        OSSLogToFileUtils.init(InstrumentationRegistry.getTargetContext(), defaultConf);
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
    @Test
    public void testWriteLogWithOutFileDir() throws Exception {
        ClientConfiguration defaultConf = ClientConfiguration.getDefaultConf();
        defaultConf.setMaxLogSize(MAX_LOG_SIZE);
        OSSLogToFileUtils.init(InstrumentationRegistry.getTargetContext(), defaultConf);
        Thread.sleep(1000);
        OSSLogToFileUtils.getInstance().deleteLogFileDir();

        OSSLogToFileUtils.reset();
        OSSLogToFileUtils.init(InstrumentationRegistry.getTargetContext(), defaultConf);
        Thread.sleep(1000);
        OSSLog.logWarn("testWriteLogWithOutFileDir");
        ClientException e = new ClientException("xxx");
        OSSLog.logThrowable2Local(e);

        Thread.sleep(2000l);
        long fileSize = OSSLogToFileUtils.getLocalLogFileSize();
        assertEquals(true, fileSize > 0);
    }

    //测试超过日志上限继续写日志
    @Test
    public void testWriteLogWithOutFileMinStore() throws Exception {
        ClientConfiguration defaultConf = ClientConfiguration.getDefaultConf();
        defaultConf.setMaxLogSize(5);
        OSSLogToFileUtils.init(InstrumentationRegistry.getTargetContext(), defaultConf);
        Thread.sleep(1000);
        OSSLogToFileUtils.getInstance().deleteLogFileDir();

        OSSLogToFileUtils.reset();
        OSSLogToFileUtils.init(InstrumentationRegistry.getTargetContext(), defaultConf);
        Thread.sleep(1000);
        OSSLog.logDebug("testWriteLogWithOutFileMinStore", true);

        Thread.sleep(2000l);
        long fileSize = OSSLogToFileUtils.getLocalLogFileSize();
        assertEquals(true, fileSize > 0);
    }

    @Test
    public void testCreateNewFileWithNoFileError() throws Exception {
        ClientConfiguration defaultConf = ClientConfiguration.getDefaultConf();
        defaultConf.setMaxLogSize(MAX_LOG_SIZE);
        OSSLogToFileUtils.init(InstrumentationRegistry.getTargetContext(), defaultConf);
        Thread.sleep(1000);
        OSSLogToFileUtils.getInstance().deleteLogFile();
        OSSLogToFileUtils.getInstance().resetLogFile();

        OSSLogToFileUtils.reset();
        OSSLogToFileUtils.init(InstrumentationRegistry.getTargetContext(), defaultConf);
        Thread.sleep(1000);
        OSSLog.logDebug("testCreateNewFileWithNoFileError", true);
        Thread.sleep(2000l);

        long fileSize = OSSLogToFileUtils.getLocalLogFileSize();
        assertEquals(true, fileSize > 0);
    }

    @Test
    public void testCreateNewFileWithNoFileDirError() throws Exception {
        OSSLogToFileUtils.reset();
        ClientConfiguration defaultConf = ClientConfiguration.getDefaultConf();
        defaultConf.setMaxLogSize(MAX_LOG_SIZE);
        OSSLogToFileUtils.init(InstrumentationRegistry.getTargetContext(), defaultConf);
        Thread.sleep(1000);
        OSSLogToFileUtils.getInstance().deleteLogFileDir();
        OSSLogToFileUtils.getInstance().resetLogFile();

        OSSLog.logDebug("testCreateNewFileWithNoFileDirError", true);

        Thread.sleep(2000l);
        long fileSize = OSSLogToFileUtils.getLocalLogFileSize();
        assertEquals(true, fileSize > 0);
    }

    @Test
    public void testDisableLog() throws Exception {
        OSSLog.disableLog();

        ClientConfiguration defaultConf = ClientConfiguration.getDefaultConf();
        defaultConf.setMaxLogSize(MAX_LOG_SIZE);
        OSSLogToFileUtils.init(InstrumentationRegistry.getTargetContext(), defaultConf);
        Thread.sleep(1000);
        OSSLogToFileUtils.getInstance().resetLogFile();

        OSSLog.logDebug("testDisableLog");


        long fileSize = OSSLogToFileUtils.getLocalLogFileSize();
        assertEquals(true, fileSize == 0);

        OSSLog.enableLog();
    }

    @Test
    public void testCreateFileError() throws Exception {
        String logDir = Build.VERSION.SDK_INT < Build.VERSION_CODES.Q ? Environment.getExternalStorageDirectory().getPath() : InstrumentationRegistry.getContext().getFilesDir().getAbsolutePath();
        File file = new File( logDir + File.separator
                + "OSSLog" + File.separator + "logs.csv");
        ClientConfiguration defaultConf = ClientConfiguration.getDefaultConf();
        defaultConf.setMaxLogSize(MAX_LOG_SIZE);
        OSSLogToFileUtils.init(InstrumentationRegistry.getTargetContext(), defaultConf);
        Thread.sleep(1000);
        OSSLogToFileUtils.getInstance().deleteLogFileDir();
        OSSLogToFileUtils.getInstance().createNewFile(file);
        assertEquals(true, !file.exists());
    }

}
