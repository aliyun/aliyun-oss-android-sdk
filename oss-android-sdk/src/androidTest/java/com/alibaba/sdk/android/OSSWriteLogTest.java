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

/**
 * Created by jingdan on 2017/8/16.
 */
public class OSSWriteLogTest extends AndroidTestCase {
    private OSS oss;
    private static final long MAX_LOG_SIZE = 5 * 1024 *10;

    @Override
    public void setUp() throws Exception {
        if (oss == null) {
            OSSLog.enableLog();
            ClientConfiguration defaultConf = ClientConfiguration.getDefaultConf();
            defaultConf.setMaxLogSize(MAX_LOG_SIZE);
            oss = new OSSClient(getContext(), OSSTestConfig.ENDPOINT, OSSTestConfig.credentialProvider, defaultConf);
        }
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        OSSLog.logD("tearDown",false);
        OSSLogToFileUtils.getInstance().resetLogFile();
    }

    //测试日志的超过上限，重置case
    public void testWriteLogLogic(){
        OSSLog.logD("testWriteLogLogic--start-----",false);

        long fileSize = OSSLogToFileUtils.getLocalLogFileSize();

        while (fileSize < MAX_LOG_SIZE){
            ClientException e = new ClientException();
            OSSLog.logThrowable2Local(e);
            fileSize = OSSLogToFileUtils.getLocalLogFileSize();
        }

        assertEquals(true,fileSize>MAX_LOG_SIZE);

        OSSLog.logD("testWriteLogLogic--end-----",false);
    }

    //空文件下写日志case
    public void testWriteLogWithOutFile(){
        OSSLogToFileUtils.getInstance().deleteLogFile();

        OSSLog.logD("testWriteLogWithOutFile",true);
    }


    public void testWriteLogWithOutSdCard(){
        String externalStorageState = Environment.getExternalStorageState();
        if(externalStorageState.equals(Environment.MEDIA_MOUNTED)){
            OSSLog.logD("testWriteLogWithOutSdCard--MOUNTED",true);
        }else{
            OSSLog.logD("testWriteLogWithOutSdCard--UNMOUNTED",false);
        }
    }

}
