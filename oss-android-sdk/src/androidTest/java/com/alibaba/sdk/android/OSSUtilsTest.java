package com.alibaba.sdk.android;

import android.os.Environment;
import android.support.test.runner.AndroidJUnit4;
import android.text.TextUtils;
import android.util.Base64;

import com.alibaba.sdk.android.oss.common.LogThreadPoolManager;
import com.alibaba.sdk.android.oss.common.OSSHeaders;
import com.alibaba.sdk.android.oss.common.OSSLog;
import com.alibaba.sdk.android.oss.common.utils.BinaryUtil;
import com.alibaba.sdk.android.oss.common.utils.DateUtil;
import com.alibaba.sdk.android.oss.common.utils.HttpUtil;
import com.alibaba.sdk.android.oss.common.utils.HttpdnsMini;
import com.alibaba.sdk.android.oss.common.utils.IOUtils;
import com.alibaba.sdk.android.oss.common.utils.OSSUtils;
import com.alibaba.sdk.android.oss.common.utils.VersionInfoUtils;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;


import java.io.File;
import java.net.URL;
import java.text.ParseException;
import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.net.URI;

import static org.junit.Assert.*;

/**
 * Created by jingdan on 2017/8/25.
 */

@RunWith(AndroidJUnit4.class)
public class OSSUtilsTest {

    @Before
    public void setUp() throws Exception {
        OSSLog.enableLog();
        OSSTestConfig.initDemoFile("guihua.zip");
    }

    @Test
    public void testVersionInfoUtils() {
        System.clearProperty("http.agent");
        String agent = VersionInfoUtils.getUserAgent(null);
        assertEquals(true, !TextUtils.isEmpty(agent));
    }

    @Test
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

    @Test
    public void testFormatAlternativeIso8601Date() {
        Date date = new Date();
        String s = DateUtil.formatAlternativeIso8601Date(date);
        assertNotNull(s);
    }

    @Test
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

    @Test
    public void testIOUtils() throws Exception {
        new IOUtils();
        byte[] bytes = IOUtils.readStreamAsBytesArray(null);
        assertTrue(bytes.length == 0);

        String str = IOUtils.readStreamAsString(null, "UTF-8");
        assertTrue(TextUtils.isEmpty(str));
    }

    @Test
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

    @Test
    public void testBinaryUtil() throws Exception {
        byte[] testdatas = BinaryUtil.fromBase64String("testdata");
        assertTrue(testdatas.length > 0);

        String md5StrFromBytes = BinaryUtil.getMd5StrFromBytes(null);
        assertTrue(TextUtils.isEmpty(md5StrFromBytes));


        String filepath = OSSTestConfig.FILE_DIR + "guihua.zip";
        String s = BinaryUtil.calculateBase64Md5(filepath);
        assertTrue(!TextUtils.isEmpty(s));
    }

    @Test
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

    @Test
    public void testIsValidateIPWithRightIPV6(){
        String ipv6address = "2401:b180::dc";
        try{
            Boolean isValidateIp = OSSUtils.isValidateIP(ipv6address);
            assertTrue(isValidateIp);
        }catch (Exception e){
            assertTrue(false);
        }
    }

    @Test
    public void testIsValidateIPWithWrongIPV6(){
        String ipv6address = "2401:b180:error:dc";
        try{
            Boolean isValidateIp = OSSUtils.isValidateIP(ipv6address);
            assertFalse(isValidateIp);
        }catch (Exception e){
            assertFalse(true);
        }
    }

    @Test
    public void testIsValidateIPWithRightIPV4(){
        String ipv6address = "192.168.0.1";
        try{
            Boolean isValidateIp = OSSUtils.isValidateIP(ipv6address);
            assertTrue(isValidateIp);
        }catch (Exception e){
            assertTrue(false);
        }
    }

    @Test
    public void testIsValidateIPWithWrongIPV4(){
        String ipv6address = "256.168.0.1";
        try{
            Boolean isValidateIp = OSSUtils.isValidateIP(ipv6address);
            assertFalse(isValidateIp);
        }catch (Exception e){
            assertFalse(true);
        }
    }

    @Test
    public void testIsValidateIPWithHost(){
        String ipv6address = "www.aliyun.com";
        try{
            Boolean isValidateIp = OSSUtils.isValidateIP(ipv6address);
            assertFalse(isValidateIp);
        }catch (Exception e){
            assertFalse(true);
        }
    }

    @Test
    public void testValidateHost(){
        String host = "*.aliyun-inc.com";
        try{
            Boolean isTrue = OSSUtils.isOssOriginHost(host);
            assertTrue(isTrue);
        }catch (Exception e){
            assertTrue(false);
        }
    }

    @Test
    public void testCnameHost(){
        String host = "*.abc.com";
        try{
            Boolean isFalse = OSSUtils.isOssOriginHost(host);
            assertFalse(isFalse);
        }catch (Exception e){
            assertTrue(false);
        }
    }

    @Test
    public void testInternalHost(){
        String host = "oss-beijing-internal.aliyuncs.com";
        try{
            Boolean isTrue = OSSUtils.isOssOriginHost(host);
            assertTrue(isTrue);
        }catch (Exception e){
            assertTrue(false);
        }
    }

    @Test
    public void testIpHost(){
        String host = "10.0.0.2";
        try{
            Boolean isFalse = OSSUtils.isOssOriginHost(host);
            assertFalse(isFalse);
        }catch (Exception e){
            assertTrue(false);
        }
    }

    @Test
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

    @Test
    public void testBucketName() {
        ///^[a-z0-9][a-z0-9\\-]{1,61}[a-z0-9]$"
        Boolean result1 = OSSUtils.validateBucketName("123-456abc");
        assertTrue(result1);

        Boolean result2 = OSSUtils.validateBucketName("123-456abc-");
        assertFalse(result2);

        Boolean result3 = OSSUtils.validateBucketName("-123-456abc");
        assertFalse(result3);

        Boolean result4 = OSSUtils.validateBucketName("123\\456abc");
        assertFalse(result4);

        Boolean result5 = OSSUtils.validateBucketName("abc123");
        assertTrue(result5);

        Boolean result6 = OSSUtils.validateBucketName("abc_123");
        assertFalse(result6);

        Boolean result7 = OSSUtils.validateBucketName("a");
        assertFalse(result7);

        Boolean result8 = OSSUtils.validateBucketName("abcdefghig-abcdefghig-abcdefghig-abcdefghig-abcdefghig-abcdefghig");
        assertFalse(result8);

    }

    @Test
    public void testEndPoint() throws Exception{
        String bucketName = "test-image";

        String result1 = getResultEndPoint( "http://123.test:8989/path?ooob",bucketName);
        assertEquals(result1 , "http://123.test:8989");

        String result2 = getResultEndPoint( "http://192.168.0.1:8081",bucketName);
        assertEquals(result2 , "http://192.168.0.1:8081/"+bucketName);

        String result3 = getResultEndPoint( "http://192.168.0.1",bucketName);
        assertEquals(result3 , "http://192.168.0.1/"+bucketName);

        String result4 = getResultEndPoint( "http://oss-cn-region.aliyuncs.com",bucketName);
        assertEquals(result4 , "http://"+bucketName+".oss-cn-region.aliyuncs.com");
    }

    private String getResultEndPoint(String urlString,String bucketName) throws  Exception{

        URI endpoint = new URI( urlString);

        String scheme = endpoint.getScheme();
        String originHost = endpoint.getHost();
        String portString = null;

        int port = endpoint.getPort();
        if (port != -1) {
            portString = String.valueOf(port);
        }

        if (TextUtils.isEmpty(originHost)){
            String url = endpoint.toString();
            OSSLog.logDebug("endpoint url : " + url);
        }

        OSSLog.logDebug(" scheme : " + scheme);
        OSSLog.logDebug(" originHost : " + originHost);
        OSSLog.logDebug(" port : " + portString);

        String baseURL = scheme + "://" + originHost;
        if(!TextUtils.isEmpty(portString)){
            baseURL += (":" + portString);
        }

        if (!TextUtils.isEmpty(bucketName)) {
            if (OSSUtils.isOssOriginHost(originHost)) {
                // official endpoint
                originHost = bucketName + "." + originHost;
                String urlHost = null;

                if (scheme == "http") {
                    urlHost = HttpdnsMini.getInstance().getIpByHostAsync(originHost);
                } else {
                    OSSLog.logDebug("[buildCannonicalURL], disable httpdns");
                }

                if (!TextUtils.isEmpty(urlHost)) {
                    baseURL = scheme + "://" + urlHost;
                } else {
                    baseURL = scheme + "://" + originHost;
                }
            }else if (OSSUtils.isValidateIP(originHost)) {
                // ip address
                baseURL += ("/" + bucketName);
            }
        }
        return baseURL;
    }


    public static String toBase64String(byte[] binaryData){
        return Base64.encodeToString(binaryData, Base64.DEFAULT).trim();
    }

    public static byte[] fromBase64String(String base64String){
        return Base64.decode(base64String.getBytes(), Base64.DEFAULT);
    }
}
