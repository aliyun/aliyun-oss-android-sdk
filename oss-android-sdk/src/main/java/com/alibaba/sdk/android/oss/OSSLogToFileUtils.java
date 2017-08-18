package com.alibaba.sdk.android.oss;

import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.util.Log;

import com.alibaba.sdk.android.oss.common.OSSLog;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by jingdan on 2017/8/11.
 * 日志写入本地文件
 */

public class OSSLogToFileUtils {

    private static ExecutorService logService = Executors.newSingleThreadExecutor();
    /**
     * 上下文对象
     */
    private static Context sContext;
    /**
     * FileLogUtils类的实例
     */
    private static OSSLogToFileUtils instance;
    /**
     * 用于保存日志的文件
     */
    private static File sLogFile;
    /**
     * 日志中的时间显示格式
     */
    private static SimpleDateFormat sLogSDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    /**
     * 日志的最大占用空间 - 单位：字节
     * <p>
     * 注意：为了性能，没有每次写入日志时判断，故日志在写入第二次初始化之前，不会受此变量限制，所以，请注意日志工具类的初始化时间
     * <p>
     * 为了衔接上文，日志超出设定大小后不会被直接删除，而是存储一个副本，所以实际占用空间是两份日志大小
     * <p>
     * 除了第一次超出大小后存为副本外，第二次及以后再次超出大小，则会覆盖副本文件，所以日志文件最多也只有两份
     * <p>
     * 默认10M
     */
    private static long LOG_MAX_SIZE = 5 * 1024 *1024; //5mb
    /**
     * 以调用者的类名作为TAG
     */
    private static String mTag;

    private static final String LOG_TAG = "OSS-Android-SDK";
    private static final String LOG_DIR_NAME = "OSSLog";
    private boolean useSdCard = true;

    private OSSLogToFileUtils(){}

    /**
     * 初始化日志库
     *
     * @param context
     */
    public static void init(Context context, ClientConfiguration cfg) {
        log(Log.INFO,"init ...");
        if (null == sContext || null == instance || null == sLogFile || !sLogFile.exists()) {
            if(cfg!=null) {
                LOG_MAX_SIZE = cfg.getMaxLogSize();
            }
            sContext = context.getApplicationContext();
            instance = getInstance();
            sLogFile = instance.getLogFile();
            if(sLogFile !=null) {
                log(Log.INFO, "LogFilePath is: " + sLogFile.getPath());
                // 获取当前日志文件大小
                long logFileSize = getFileSize(sLogFile);
                log(Log.DEBUG, "Log max size is: " + Formatter.formatFileSize(context, LOG_MAX_SIZE));
                log(Log.INFO, "Log now size is: " + Formatter.formatFileSize(context, logFileSize));
                // 若日志文件超出了预设大小，则重置日志文件
                if (LOG_MAX_SIZE < logFileSize) {
                    log(Log.INFO, "init reset log file");
                    instance.resetLogFile();
                }
            }
        } else {
            log(Log.INFO,"LogToFileUtils has been init ...");
        }
    }

    public static OSSLogToFileUtils getInstance(){
        if(instance == null){
            synchronized (OSSLogToFileUtils.class){
                if(instance == null){
                    instance = new OSSLogToFileUtils();
                }
            }
        }
        return instance;
    }

    /**
     * 读取外部存储空间大小  大小单位 kb
     * @return
     */
    private long readSDCard() {
        long sdCardSize = 0;
        String state = Environment.getExternalStorageState();
        if(Environment.MEDIA_MOUNTED.equals(state)) {
            File sdcardDir = Environment.getExternalStorageDirectory();
            StatFs sf = new StatFs(sdcardDir.getPath());
            long blockSize = sf.getBlockSize();
            long availCount = sf.getAvailableBlocks();
            sdCardSize = availCount*blockSize;
        }
        return sdCardSize;
    }

    /**
     * 读取内部存储空间大小  大小单位 kb
     * @return
     */
    private long readSystem() {
        File root = Environment.getRootDirectory();
        StatFs sf = new StatFs(root.getPath());
        long blockSize = sf.getBlockSize();
        long availCount = sf.getAvailableBlocks();
        return availCount*blockSize/1024;
    }

    public void setUseSdCard(boolean useSdCard) {
        this.useSdCard = useSdCard;
    }

    /**
     * 重置日志文件
     * <p>
     * 若日志文件超过一定大小，则把日志改名为lastLog.txt，然后新日志继续写入日志文件
     * <p>
     * 每次仅保存一个上一份日志，日志文件最多有两份
     * <p/>
     */
    public void resetLogFile() {
        log(Log.INFO,"Reset Log File ... ");

        // 创建log.txt，若存在则删除
        if(!sLogFile.getParentFile().exists()){
            log(Log.INFO,"Reset Log make File dir ... ");
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
        // 创建log.txt，若存在则删除
        File logFile = new File(sLogFile.getParent() + "/logs.csv");
        if (logFile.exists()) {
            log(Log.INFO,"delete Log File ... ");
            logFile.delete();
        }
    }

    public void deleteLogFileDir() {
        // 创建log.txt，若存在则删除
        File logFile = new File(sLogFile.getParent() + "/logs.csv");
        if (logFile.exists()) {
            logFile.delete();
        }
        File dir = new File(Environment.getExternalStorageDirectory().getPath()+"/"+LOG_DIR_NAME);
        if(dir.exists()) {
            log(Log.INFO,"delete Log FileDir ... ");
            dir.delete();
        }
    }

    public static void reset(){
        sContext = null;
        instance = null;
        sLogFile = null;
    }

    /**
     * 获取文件大小
     *
     * @param file 文件
     * @return
     */
    public static long getFileSize(File file) {
        long size = 0;
        if (file!=null && file.exists()) {
            try {
                FileInputStream fis = new FileInputStream(file);
                size = fis.available();
            } catch (Exception e) {
                OSSLog.logE(e.toString());
            }
        }
        return size;
    }

    /**
     * 获取本地日志文件大小
     * @return
     */
    public static long getLocalLogFileSize() {
        return getFileSize(sLogFile);
    }

    /**
     * 获取APP日志文件
     *
     * @return APP日志文件
     */
    private File getLogFile() {
        File file;
        boolean canStorage;
        // 判断是否有SD卡或者外部存储器
        if (useSdCard && Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            canStorage = readSDCard() > LOG_MAX_SIZE/1024;
            // 有SD卡则使用SD - PS:没SD卡但是有外部存储器，会使用外部存储器
            // SD\OSSLog\logs.txt
            file = new File(Environment.getExternalStorageDirectory().getPath()+"/"+LOG_DIR_NAME);
        } else {
            // 没有SD卡或者外部存储器，使用内部存储器
            // \data\data\包名\files\OSSLog\logs.txt
            canStorage = readSystem() > LOG_MAX_SIZE/1024;
            file = new File(sContext.getFilesDir().getPath() +"/"+ LOG_DIR_NAME);
        }
        File logFile = null;
        // 若目录不存在则创建目录
        if(canStorage) {
            if (!file.exists()) {
                file.mkdir();
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
            OSSLog.logE("Create log file failure !!! " + e.toString());
        }
    }

    /**
     * 获取当前函数的信息
     *
     * @return 当前函数的信息
     */
    private String getFunctionInfo(StackTraceElement[] sts) {
        String msg = null;
        if (sts == null) {
            msg = "[" + sLogSDF.format(new java.util.Date()) + "]";
        }
//        for (StackTraceElement st : sts) {
//            if (st.isNativeMethod()) {
//                continue;
//            }
//            if (st.getClassName().equals(Thread.class.getName())) {
//                continue;
//            }
//            if (st.getClassName().equals(instance.getClass().getName())) {
//                continue;
//            }
//            if (st.getClassName().equals(OSSLog.class.getName())){
//                continue;
//            }
//            mTag = st.getFileName();
//            return "[" + sLogSDF.format(new java.util.Date()) + " " + st.getClassName() + " " + st
//                    .getMethodName() + " Line:" + st.getLineNumber() + "]";
//        }
        return msg;
    }

    /**
     * 写入日志文件的数据
     *
     * @param str 需要写入的数据
     */
    public synchronized void write(Object str) {
        // 判断是否初始化或者初始化是否成功
        if (null == sContext || null == instance || null == sLogFile || !sLogFile.exists()) {
            return;
        }
        WriteCall writeCall = new WriteCall(str);
        logService.submit(writeCall);
    }

    private static class WriteCall implements Callable<Object>{

        private Object mStr;

        public WriteCall(Object mStr) {
            this.mStr = mStr;
        }

        @Override
        public Object call() throws Exception{
            long logFileSize = getInstance().getFileSize(sLogFile);
            log(Log.DEBUG, "Log max size is: " + Formatter.formatFileSize(sContext, LOG_MAX_SIZE));
            log(Log.INFO, "Log now size is: " + Formatter.formatFileSize(sContext, logFileSize));
            // 若日志文件超出了预设大小，则重置日志文件
            if (logFileSize > LOG_MAX_SIZE) {
                getInstance().resetLogFile();
            }
            //输出流操作 输出日志信息至本地存储空间内
            PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(sLogFile,true)));
            if(pw!=null) {
                log(Log.DEBUG,"文件存在:"+sLogFile.exists());
                log(Log.DEBUG,"write data");
                setBaseInfo(pw);
                if (mStr instanceof Throwable) {
                    //写入异常信息
                    printEx(pw);
                } else {
                    //写入普通log
                    pw.println(getInstance().getFunctionInfo(null) + " - " + mStr.toString());
                }
                pw.println("-----------------------------------------------------------------------");
                pw.flush();

                pw.close();
            }
            return mStr;
        }

        private PrintWriter setBaseInfo(PrintWriter pw){
            //导出手机信息和异常信息
            pw.println("android版本号：" + Build.VERSION.RELEASE);
            pw.println("手机型号：" + Build.MODEL);
            // 获取手机所有连接管理对象（包括对wi-fi,net等连接的管理）
            ConnectivityManager connectivityManager = (ConnectivityManager) sContext.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            String networkState = "unconnected";
            if (activeNetworkInfo.getState() == NetworkInfo.State.CONNECTED){
                networkState = "connected";
            }
            if(!TextUtils.isEmpty(getOperatorName())) {
                pw.println("运营商：" + getOperatorName());
            }
            pw.println("network_state：" + networkState);//网络状况
            pw.println("network_type：" + activeNetworkInfo.getTypeName());//当前网络类型 如 wifi 2g 3g 4g

            return pw;
        }

        private PrintWriter printEx(PrintWriter pw){
            pw.println("发生异常时间：" + sLogSDF.format(new java.util.Date()));
            StringWriter exsw = new StringWriter();
            PrintWriter expw = new PrintWriter(exsw);
            ((Throwable)mStr).printStackTrace(expw);
            String exStr = exsw.toString().replaceAll("<br>","\r\n");
            pw.print(exStr);
            return pw;
        }

        /**
         * 获取运营商名字,需要sim卡
         */
        private String getOperatorName() {
            TelephonyManager telephonyManager = (TelephonyManager) sContext.getSystemService(Context.TELEPHONY_SERVICE);
            String operator = telephonyManager.getSimOperator();
            String operatorName = "";
            if (operator != null) {
                if (operator.equals("46000") || operator.equals("46002")) {
                     operatorName="中国移动";
                } else if (operator.equals("46001")) {
                     operatorName="中国联通";
                } else if (operator.equals("46003")) {
                     operatorName="中国电信";
                }
            }
            return operatorName;
        }
    }

    private static void log(int level,String msg){
        if(OSSLog.isEnableLog()){
            if(level == Log.INFO){
                Log.i(LOG_TAG,msg);
            }else if(level == Log.WARN){
//                Log.w(LOG_TAG,msg);
            }else if(level == Log.ERROR){
                Log.e(LOG_TAG,msg);
            }else if(level == Log.DEBUG){
                Log.d(LOG_TAG,msg);
            }
        }
    }

}
