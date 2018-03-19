package com.alibaba.sdk.android.oss.common;

import android.content.Context;
import android.os.Environment;
import android.os.StatFs;

import com.alibaba.sdk.android.oss.ClientConfiguration;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by jingdan on 2017/8/11.
 */

public class OSSLogToFileUtils {

    private static final String LOG_DIR_NAME = "OSSLog";
    private static LogThreadPoolManager logService = LogThreadPoolManager.newInstance();
    /**
     * Context Object
     */
    private static Context sContext;
    /**
     * FileLogUtils instance
     */
    private static OSSLogToFileUtils instance;
    /**
     * file for log
     */
    private static File sLogFile;
    /**
     * time format
     */
    private static SimpleDateFormat sLogSDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    /**
     * default 5M
     */
    private static long LOG_MAX_SIZE = 5 * 1024 * 1024; //5mb
    private boolean useSdCard = true;

    private OSSLogToFileUtils() {
    }

    /**
     * init
     *
     * @param context
     */
    public static void init(Context context, ClientConfiguration cfg) {
        OSSLog.logDebug("init ...", false);
        if (cfg != null) {
            LOG_MAX_SIZE = cfg.getMaxLogSize();
        }
        if (null == sContext || null == instance || null == sLogFile || !sLogFile.exists()) {
            sContext = context.getApplicationContext();
            instance = getInstance();
            logService.addExecuteTask(new Runnable() {
                @Override
                public void run() {
                    sLogFile = instance.getLogFile();
                    if (sLogFile != null) {
                        OSSLog.logInfo("LogFilePath is: " + sLogFile.getPath(), false);
                        // 获取当前日志文件大小
                        long logFileSize = getLogFileSize(sLogFile);
                        // 若日志文件超出了预设大小，则重置日志文件
                        if (LOG_MAX_SIZE < logFileSize) {
                            OSSLog.logInfo("init reset log file", false);
                            instance.resetLogFile();
                        }
                    }
                }
            });
        } else {
            OSSLog.logDebug("LogToFileUtils has been init ...", false);
        }
    }

    public static OSSLogToFileUtils getInstance() {
        if (instance == null) {
            synchronized (OSSLogToFileUtils.class) {
                if (instance == null) {
                    instance = new OSSLogToFileUtils();
                }
            }
        }
        return instance;
    }

    public static void reset() {
        sContext = null;
        instance = null;
        sLogFile = null;
    }

    /**
     * file length
     *
     * @param file 文件
     * @return
     */
    public static long getLogFileSize(File file) {
        long size = 0;
        if (file != null && file.exists()) {
            size = file.length();
        }
        return size;
    }

    /**
     * log size
     *
     * @return
     */
    public static long getLocalLogFileSize() {
        return getLogFileSize(sLogFile);
    }

    private long readSDCardSpace() {
        long sdCardSize = 0;
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            File sdcardDir = Environment.getExternalStorageDirectory();
            StatFs sf = new StatFs(sdcardDir.getPath());
            long blockSize = sf.getBlockSize();
            long availCount = sf.getAvailableBlocks();
            sdCardSize = availCount * blockSize;
        }
        OSSLog.logDebug("sd卡存储空间:" + String.valueOf(sdCardSize) + "kb", false);
        return sdCardSize;
    }

    private long readSystemSpace() {
        File root = Environment.getRootDirectory();
        StatFs sf = new StatFs(root.getPath());
        long blockSize = sf.getBlockSize();
        long availCount = sf.getAvailableBlocks();
        long systemSpaceSize = availCount * blockSize / 1024;
        OSSLog.logDebug("内部存储空间:" + String.valueOf(systemSpaceSize) + "kb", false);
        return systemSpaceSize;
    }

    public void setUseSdCard(boolean useSdCard) {
        this.useSdCard = useSdCard;
    }

    /**
     * set log file
     */
    public void resetLogFile() {
        OSSLog.logDebug("Reset Log File ... ", false);

        // 创建log.csv，若存在则删除
        if (!sLogFile.getParentFile().exists()) {
            OSSLog.logDebug("Reset Log make File dir ... ", false);
            sLogFile.getParentFile().mkdir();
        }
        File logFile = new File(sLogFile.getParent() + "/logs.csv");
        if (logFile.exists()) {
            logFile.delete();
        }
        // 新建日志文件
        createNewFile(logFile);
    }

    public void deleteLogFile() {
        // 创建log.csv，若存在则删除
        File logFile = new File(sLogFile.getParent() + "/logs.csv");
        if (logFile.exists()) {
            OSSLog.logDebug("delete Log File ... ", false);
            logFile.delete();
        }
    }

    public void deleteLogFileDir() {
        // 创建log.csv，若存在则删除
        deleteLogFile();
        File dir = new File(Environment.getExternalStorageDirectory().getPath() + File.separator + LOG_DIR_NAME);
        if (dir.exists()) {
            OSSLog.logDebug("delete Log FileDir ... ", false);
            dir.delete();
        }
    }

    /**
     * get log file
     *
     * @return APP日志文件
     */
    private File getLogFile() {
        File file;
        boolean canStorage;
        // 判断是否有SD卡或者外部存储器
        if (useSdCard && Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            canStorage = readSDCardSpace() > LOG_MAX_SIZE / 1024;
            // 有SD卡则使用SD - PS:没SD卡但是有外部存储器，会使用外部存储器
            // SD\OSSLog\logs.csv
            file = new File(Environment.getExternalStorageDirectory().getPath() + File.separator + LOG_DIR_NAME);
        } else {
            // 没有SD卡或者外部存储器，使用内部存储器
            // \data\data\包名\files\OSSLog\logs.csv
            canStorage = readSystemSpace() > LOG_MAX_SIZE / 1024;
            file = new File(sContext.getFilesDir().getPath() + File.separator + LOG_DIR_NAME);
        }
        File logFile = null;
        // 若目录不存在则创建目录
        if (canStorage) {
            if (!file.exists()) {
                file.mkdirs();
            }
            logFile = new File(file.getPath() + "/logs.csv");
            if (!logFile.exists()) {
                createNewFile(logFile);
            }
        }
        return logFile;
    }

    public void createNewFile(File logFile) {
        try {
            logFile.createNewFile();
        } catch (Exception e) {
            OSSLog.logError("Create log file failure !!! " + e.toString(), false);
        }
    }


    private String getFunctionInfo(StackTraceElement[] ste) {
        String msg = null;
        if (ste == null) {
            msg = "[" + sLogSDF.format(new java.util.Date()) + "]";
        }
        return msg;
    }

    public synchronized void write(Object str) {
        if (OSSLog.isEnableLog()) {
            // 判断是否初始化或者初始化是否成功
            if (null == sContext || null == instance || null == sLogFile) {
                return;
            }
            if (!sLogFile.exists()) {
                resetLogFile();
            }
            WriteCall writeCall = new WriteCall(str);
            logService.addExecuteTask(writeCall);
        }
    }

    private static class WriteCall implements Runnable {

        private Object mStr;

        public WriteCall(Object mStr) {
            this.mStr = mStr;
        }

        @Override
        public void run() {
            if (sLogFile != null) {
                long logFileSize = getInstance().getLogFileSize(sLogFile);
                // 若日志文件超出了预设大小，则重置日志文件
                if (logFileSize > LOG_MAX_SIZE) {
                    getInstance().resetLogFile();
                }
                //输出流操作 输出日志信息至本地存储空间内
                PrintWriter pw;
                try {
                    pw = new PrintWriter(new FileWriter(sLogFile, true), true);
                    if (pw != null) {
                        if (mStr instanceof Throwable) {
                            //写入异常信息
                            printEx(pw);
                        } else {
                            //写入普通log
                            pw.println(getInstance().getFunctionInfo(null) + " - " + mStr.toString());
                        }
                        pw.println("------>end of log");
                        pw.println();
                        pw.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private PrintWriter printEx(PrintWriter pw) {
            pw.println("crash_time：" + sLogSDF.format(new Date()));
            ((Throwable) mStr).printStackTrace(pw);
            return pw;
        }
    }
}
