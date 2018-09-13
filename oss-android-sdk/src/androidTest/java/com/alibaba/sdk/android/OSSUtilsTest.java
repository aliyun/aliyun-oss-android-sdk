package com.alibaba.sdk.android;

import android.os.Environment;
import android.test.AndroidTestCase;
import android.text.TextUtils;

import com.alibaba.sdk.android.oss.common.LogThreadPoolManager;
import com.alibaba.sdk.android.oss.common.OSSLog;
import com.alibaba.sdk.android.oss.common.utils.BinaryUtil;
import com.alibaba.sdk.android.oss.common.utils.DateUtil;
import com.alibaba.sdk.android.oss.common.utils.HttpUtil;
import com.alibaba.sdk.android.oss.common.utils.IOUtils;
import com.alibaba.sdk.android.oss.common.utils.OSSUtils;
import com.alibaba.sdk.android.oss.common.utils.VersionInfoUtils;

import org.apache.commons.codec.binary.Base64;


import java.io.File;
import java.text.ParseException;
import java.util.Date;
import java.util.concurrent.CountDownLatch;

/**
 * Created by jingdan on 2017/8/25.
 */

public class OSSUtilsTest extends AndroidTestCase {


    @Override
    protected void setUp() throws Exception {
        super.setUp();
        OSSLog.enableLog();
        OSSTestConfig.initDemoFile("guihua.zip");
    }

    public void testVersionInfoUtils() {
        System.clearProperty("http.agent");
        String agent = VersionInfoUtils.getUserAgent(null);
        assertEquals(true, !TextUtils.isEmpty(agent));
    }

    public void testCheckParamRange() {
        long param = 500;
        long from = 1000;
        boolean leftInclusive = true;
        long to = 3000;
        boolean rightInclusive = true;

        boolean range = OSSUtils.checkParamRange(param, from, leftInclusive, to, rightInclusive);
        assertEquals(false, range);

        param = 2000;
        from = 1000;
        leftInclusive = true;
        to = 3000;
        rightInclusive = false;

        range = OSSUtils.checkParamRange(param, from, leftInclusive, to, rightInclusive);
        assertEquals(true, range);

        param = 3000;
        from = 1000;
        leftInclusive = true;
        to = 3000;
        rightInclusive = false;

        range = OSSUtils.checkParamRange(param, from, leftInclusive, to, rightInclusive);
        assertEquals(false, range);

        param = 2000;
        from = 1000;
        leftInclusive = false;
        to = 3000;
        rightInclusive = false;

        range = OSSUtils.checkParamRange(param, from, leftInclusive, to, rightInclusive);
        assertEquals(true, range);

        param = 1000;
        from = 1000;
        leftInclusive = false;
        to = 3000;
        rightInclusive = false;

        range = OSSUtils.checkParamRange(param, from, leftInclusive, to, rightInclusive);
        assertEquals(false, range);

        param = 2000;
        from = 1000;
        leftInclusive = false;
        to = 3000;
        rightInclusive = true;

        range = OSSUtils.checkParamRange(param, from, leftInclusive, to, rightInclusive);
        assertEquals(true, range);

        param = 1000;
        from = 1000;
        leftInclusive = false;
        to = 3000;
        rightInclusive = true;

        range = OSSUtils.checkParamRange(param, from, leftInclusive, to, rightInclusive);
        assertEquals(false, range);

    }

    public void testFormatAlternativeIso8601Date() {
        Date date = new Date();
        String s = DateUtil.formatAlternativeIso8601Date(date);
        assertNotNull(s);
    }

    public void testFormatIso8601Date() {
        new DateUtil();
        Date date = new Date();
        String s = DateUtil.formatIso8601Date(date);
        assertNotNull(s);
        Date date1 = null;
        try {
            date1 = DateUtil.parseIso8601Date("");
        } catch (ParseException e) {
            assertTrue(date1 == null);
        }
    }

    public void testIOUtils() throws Exception {
        new IOUtils();
        byte[] bytes = IOUtils.readStreamAsBytesArray(null);
        assertTrue(bytes.length == 0);

        String str = IOUtils.readStreamAsString(null, "UTF-8");
        assertTrue(TextUtils.isEmpty(str));
    }

    public void testUrlEncode() {
        new HttpUtil();
        String s = HttpUtil.urlEncode(null, "");
        assertTrue(TextUtils.isEmpty(s));
        String test = null;
        try {
            test = HttpUtil.urlEncode("http://xxxxx?x=2", "");
        } catch (Exception e) {
            e.printStackTrace();
        }
        assertTrue(test == null);
    }

    public void testBinaryUtil() throws Exception {
        byte[] testdatas = BinaryUtil.fromBase64String("testdata");
        assertTrue(testdatas.length > 0);

        String md5StrFromBytes = BinaryUtil.getMd5StrFromBytes(null);
        assertTrue(TextUtils.isEmpty(md5StrFromBytes));


        String filepath = Environment.getExternalStorageDirectory().getPath() + File.separator + "OSSLog/logs.csv";
        String s = BinaryUtil.calculateBase64Md5(filepath);
        assertTrue(!TextUtils.isEmpty(s));
    }

    public void testLogThreadPoolManager() {
        try {
            final CountDownLatch countDownLatch = new CountDownLatch(520);
            LogThreadPoolManager poolManager = LogThreadPoolManager.newInstance();
            for (int i = 0; i < 520; i++) {
                final int num = i;
                poolManager.addExecuteTask(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(10l);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        OSSLog.logDebug("run:" + num, false);
                        countDownLatch.countDown();
                    }
                });
            }
            countDownLatch.await();
            assertTrue(true);
        } catch (Exception e) {
            assertTrue(false);
        }
    }

    public void testIsValidateIPWithRightIPV6(){
        String ipv6address = "2401:b180::dc";
        try{
            Boolean isValidateIp = OSSUtils.isValidateIP(ipv6address);
            assertTrue(isValidateIp);
        }catch (Exception e){
            assertTrue(false);
        }
    }

    public void testIsValidateIPWithWrongIPV6(){
        String ipv6address = "2401:b180:error:dc";
        try{
            Boolean isValidateIp = OSSUtils.isValidateIP(ipv6address);
            assertFalse(isValidateIp);
        }catch (Exception e){
            assertFalse(true);
        }
    }

    public void testIsValidateIPWithRightIPV4(){
        String ipv6address = "192.168.0.1";
        try{
            Boolean isValidateIp = OSSUtils.isValidateIP(ipv6address);
            assertTrue(isValidateIp);
        }catch (Exception e){
            assertTrue(false);
        }
    }

    public void testIsValidateIPWithWrongIPV4(){
        String ipv6address = "256.168.0.1";
        try{
            Boolean isValidateIp = OSSUtils.isValidateIP(ipv6address);
            assertFalse(isValidateIp);
        }catch (Exception e){
            assertFalse(true);
        }
    }

    public void testIsValidateIPWithHost(){
        String ipv6address = "www.aliyun.com";
        try{
            Boolean isValidateIp = OSSUtils.isValidateIP(ipv6address);
            assertFalse(isValidateIp);
        }catch (Exception e){
            assertFalse(true);
        }
    }

    public void testValidateHost(){
        String host = "*.aliyun-inc.com";
        try{
            Boolean isTrue = OSSUtils.isOssOriginHost(host);
            assertTrue(isTrue);
        }catch (Exception e){
            assertTrue(false);
        }
    }

    public void testCnameHost(){
        String host = "*.abc.com";
        try{
            Boolean isFalse = OSSUtils.isOssOriginHost(host);
            assertFalse(isFalse);
        }catch (Exception e){
            assertTrue(false);
        }
    }

    public void testInternalHost(){
        String host = "oss-beijing-internal.aliyuncs.com";
        try{
            Boolean isTrue = OSSUtils.isOssOriginHost(host);
            assertTrue(isTrue);
        }catch (Exception e){
            assertTrue(false);
        }
    }

    public void testIpHost(){
        String host = "10.0.0.2";
        try{
            Boolean isFalse = OSSUtils.isOssOriginHost(host);
            assertFalse(isFalse);
        }catch (Exception e){
            assertTrue(false);
        }
    }

    public void testBase64() throws Exception{
        String srcFileBase64Md5 = BinaryUtil.toBase64String(BinaryUtil.calculateMd5(OSSTestConfig.FILE_DIR + "guihua.zip"));
        byte[] data = BinaryUtil.fromBase64String(srcFileBase64Md5);
        String base64String = toBase64String(data);
        OSSLog.logDebug("srcFileBase64Md5 : " + srcFileBase64Md5 + " length : " +  srcFileBase64Md5.length() + " base64String : " + base64String +  " length : " +  base64String.length()+ " is equals " + srcFileBase64Md5.trim().equals(base64String.trim()));
        assertEquals(srcFileBase64Md5,base64String);


        data = fromBase64String(srcFileBase64Md5);
        base64String = BinaryUtil.toBase64String(data);
        assertEquals(srcFileBase64Md5,base64String);
    }


    public static String toBase64String(byte[] binaryData){
        return new String(Base64.encodeBase64(binaryData));
    }

    public static byte[] fromBase64String(String base64String){
        return Base64.decodeBase64(base64String.getBytes());
    }
}
