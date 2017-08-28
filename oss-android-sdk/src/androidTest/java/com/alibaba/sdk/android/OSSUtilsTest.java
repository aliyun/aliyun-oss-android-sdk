package com.alibaba.sdk.android;

import android.os.Environment;
import android.test.AndroidTestCase;
import android.text.TextUtils;

import com.alibaba.sdk.android.oss.common.utils.BinaryUtil;
import com.alibaba.sdk.android.oss.common.utils.DateUtil;
import com.alibaba.sdk.android.oss.common.utils.HttpUtil;
import com.alibaba.sdk.android.oss.common.utils.IOUtils;
import com.alibaba.sdk.android.oss.common.utils.OSSUtils;
import com.alibaba.sdk.android.oss.common.utils.VersionInfoUtils;

import java.io.File;
import java.util.Date;

/**
 * Created by jingdan on 2017/8/25.
 */

public class OSSUtilsTest extends AndroidTestCase {

    public void testVersionInfoUtils(){
        System.clearProperty("http.agent");
        String agent = VersionInfoUtils.getDefaultUserAgent();
        assertEquals(true, !TextUtils.isEmpty(agent));
    }

    public void testCheckParamRange(){
        long param =500;
        long from =1000 ;
        boolean leftInclusive = true;
        long to = 3000;
        boolean rightInclusive = true;

        boolean range = OSSUtils.checkParamRange(param, from, leftInclusive, to, rightInclusive);
        assertEquals(false,range);

        param =2000;
        from =1000 ;
        leftInclusive = true;
        to = 3000;
        rightInclusive = false;

        range = OSSUtils.checkParamRange(param, from, leftInclusive, to, rightInclusive);
        assertEquals(true,range);

        param =3000;
        from =1000 ;
        leftInclusive = true;
        to = 3000;
        rightInclusive = false;

        range = OSSUtils.checkParamRange(param, from, leftInclusive, to, rightInclusive);
        assertEquals(false,range);

        param =2000;
        from =1000 ;
        leftInclusive = false;
        to = 3000;
        rightInclusive = false;

        range = OSSUtils.checkParamRange(param, from, leftInclusive, to, rightInclusive);
        assertEquals(true,range);

        param =1000;
        from =1000 ;
        leftInclusive = false;
        to = 3000;
        rightInclusive = false;

        range = OSSUtils.checkParamRange(param, from, leftInclusive, to, rightInclusive);
        assertEquals(false,range);

        param =2000;
        from =1000 ;
        leftInclusive = false;
        to = 3000;
        rightInclusive = true;

        range = OSSUtils.checkParamRange(param, from, leftInclusive, to, rightInclusive);
        assertEquals(true,range);

        param =1000;
        from =1000 ;
        leftInclusive = false;
        to = 3000;
        rightInclusive = true;

        range = OSSUtils.checkParamRange(param, from, leftInclusive, to, rightInclusive);
        assertEquals(false,range);

    }

    public void testFormatAlternativeIso8601Date(){
        Date date = new Date();
        String s = DateUtil.formatAlternativeIso8601Date(date);
        assertNotNull(s);
    }

    public void testFormatIso8601Date(){
        Date date = new Date();
        String s = DateUtil.formatIso8601Date(date);
        assertNotNull(s);
        Date date1= null;
        try {
            date1 = DateUtil.parseIso8601Date("wrong format date");
        }catch (Exception e){
            assertTrue(date1 == null);
        }
    }

    public void testIOUtils() throws Exception{
        byte[] bytes = IOUtils.readStreamAsBytesArray(null);
        assertTrue(bytes.length==0);

        String str = IOUtils.readStreamAsString(null,"UTF-8");
        assertTrue(TextUtils.isEmpty(str));
    }

    public void testUrlEncode(){
        String s = HttpUtil.urlEncode(null, "");
        assertTrue(TextUtils.isEmpty(s));
        String test = null;
        try {
            test = HttpUtil.urlEncode("xxxxxx", "wrong encode");
        }catch (Exception e){
        }
        assertTrue(test == null);
    }

    public void testBinaryUtil() throws Exception{
        byte[] testdatas = BinaryUtil.fromBase64String("testdata");
        assertTrue(testdatas.length>0);

        String md5StrFromBytes = BinaryUtil.getMd5StrFromBytes(null);
        assertTrue(TextUtils.isEmpty(md5StrFromBytes));


        String filepath = Environment.getExternalStorageDirectory().getPath()+ File.separator+"OSSLog/logs.csv";
        String s = BinaryUtil.calculateBase64Md5(filepath);
        assertTrue(!TextUtils.isEmpty(s));
    }
}
