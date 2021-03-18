package com.alibaba.sdk.android;

import android.net.Uri;
import android.support.test.runner.AndroidJUnit4;

import com.alibaba.sdk.android.oss.internal.RequestMessage;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertTrue;
import static junit.framework.TestCase.assertEquals;

@RunWith(AndroidJUnit4.class)
public class RequestMessageTest {

    private static String SCHEME = "https://";
    private static String ENDPOINT = "oss-cn-hangzhou.aliyuncs.com";
    private static String CNAME_ENDPOINT = "oss.custom.com";
    private static String IP_ENDPOINT = "192.168.1.1:8080";

    private static String CUSTOMPATH = "/path";
    private static String CUSTOMPATH_ENDPOINT = ENDPOINT + "/path";
    private static String BUCKET_NAME = "BucketName";
    private static String OBJECT_KEY = "ObjectKey";

    public URI asUri(String uriString) {
        uriString = SCHEME + uriString;
        try {
            return new URI(uriString);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Test
    public void testBuildUrlWithCname() throws Exception {
        List<String> cnameExcludeList = new ArrayList<String>();
        cnameExcludeList.add("111");

        RequestMessage requestMessage = new RequestMessage();
        requestMessage.setBucketName(BUCKET_NAME);
        requestMessage.setObjectKey(OBJECT_KEY);
        requestMessage.setEndpoint(asUri(CNAME_ENDPOINT));
        requestMessage.setSupportCnameEnable(true);
        requestMessage.setCustomCnameExcludeList(cnameExcludeList);

        String url = requestMessage.buildCanonicalURL();
        String canonicalUrl = SCHEME + CNAME_ENDPOINT + "/" + OBJECT_KEY;
        assertEquals(url, canonicalUrl);
    }

    @Test
    public void testBuildUrlWithoutCname() throws Exception {
        List<String> cnameExcludeList = new ArrayList<String>();
        cnameExcludeList.add(CNAME_ENDPOINT);

        RequestMessage requestMessage = new RequestMessage();
        requestMessage.setBucketName(BUCKET_NAME);
        requestMessage.setObjectKey(OBJECT_KEY);
        requestMessage.setEndpoint(asUri(CNAME_ENDPOINT));
        requestMessage.setSupportCnameEnable(true);
        requestMessage.setCustomCnameExcludeList(cnameExcludeList);

        String url = requestMessage.buildCanonicalURL();
        String canonicalUrl = SCHEME + BUCKET_NAME + "." + CNAME_ENDPOINT + "/" + OBJECT_KEY;
        assertEquals(url, canonicalUrl);
    }

    @Test
    public void testBuildUrlWithCnameAndPathStyleAccessEnable() throws Exception {
        List<String> cnameExcludeList = new ArrayList<String>();
        cnameExcludeList.add(CNAME_ENDPOINT);

        RequestMessage requestMessage = new RequestMessage();
        requestMessage.setBucketName(BUCKET_NAME);
        requestMessage.setObjectKey(OBJECT_KEY);
        requestMessage.setEndpoint(asUri(CNAME_ENDPOINT));
        requestMessage.setSupportCnameEnable(true);
        requestMessage.setCustomCnameExcludeList(cnameExcludeList);
        requestMessage.setPathStyleAccessEnable(true);

        String url = requestMessage.buildCanonicalURL();
        String canonicalUrl = SCHEME + CNAME_ENDPOINT + "/" + BUCKET_NAME + "/" + OBJECT_KEY;
        assertEquals(url, canonicalUrl);
    }

    @Test
    public void testBuildUrlWithPathStyleAccessEnable() throws Exception {
        RequestMessage requestMessage = new RequestMessage();
        requestMessage.setBucketName(BUCKET_NAME);
        requestMessage.setObjectKey(OBJECT_KEY);
        requestMessage.setEndpoint(asUri(ENDPOINT));
        requestMessage.setPathStyleAccessEnable(true);

        String url = requestMessage.buildCanonicalURL();
        String canonicalUrl = SCHEME + ENDPOINT + "/" + BUCKET_NAME + "/" + OBJECT_KEY;
        assertEquals(url, canonicalUrl);
    }

    @Test
    public void testBuildUrlWithoutPathStyleAccessEnable() throws Exception {
        RequestMessage requestMessage = new RequestMessage();
        requestMessage.setBucketName(BUCKET_NAME);
        requestMessage.setObjectKey(OBJECT_KEY);
        requestMessage.setEndpoint(asUri(ENDPOINT));
        requestMessage.setPathStyleAccessEnable(false);

        String url = requestMessage.buildCanonicalURL();
        String canonicalUrl = SCHEME + BUCKET_NAME + "." + ENDPOINT + "/" + OBJECT_KEY;
        assertEquals(url, canonicalUrl);
    }

    @Test
    public void testBuildUrlWithCustomPathPrefixEnable() throws Exception {
        RequestMessage requestMessage = new RequestMessage();
        requestMessage.setBucketName(BUCKET_NAME);
        requestMessage.setObjectKey(OBJECT_KEY);
        requestMessage.setEndpoint(asUri(CUSTOMPATH_ENDPOINT));
        requestMessage.setCustomPathPrefixEnable(true);

        String url = requestMessage.buildCanonicalURL();
        String canonicalUrl = SCHEME + BUCKET_NAME + "." + CUSTOMPATH_ENDPOINT + "/" + OBJECT_KEY;
        assertEquals(url, canonicalUrl);
    }

    @Test
    public void testBuildUrlWithCustomPathPrefixEnableAndPathStyleAccessEnable() throws Exception {
        RequestMessage requestMessage = new RequestMessage();
        requestMessage.setBucketName(BUCKET_NAME);
        requestMessage.setObjectKey(OBJECT_KEY);
        requestMessage.setEndpoint(asUri(CUSTOMPATH_ENDPOINT));
        requestMessage.setCustomPathPrefixEnable(true);
        requestMessage.setPathStyleAccessEnable(true);

        String url = requestMessage.buildCanonicalURL();
        String canonicalUrl = SCHEME + CUSTOMPATH_ENDPOINT + "/" + BUCKET_NAME + "/" + OBJECT_KEY;
        assertEquals(url, canonicalUrl);
    }

    @Test
    public void testBuildUrlWithIp() throws Exception {
        RequestMessage requestMessage = new RequestMessage();
        requestMessage.setBucketName(BUCKET_NAME);
        requestMessage.setObjectKey(OBJECT_KEY);
        requestMessage.setEndpoint(asUri(IP_ENDPOINT));
        requestMessage.setPathStyleAccessEnable(true);

        String url = requestMessage.buildCanonicalURL();
        String canonicalUrl = SCHEME + IP_ENDPOINT + "/" + BUCKET_NAME + "/" + OBJECT_KEY;
        assertEquals(url, canonicalUrl);
    }

    @Test
    public void testBuildUrlWithNullBucketNameOrObjectKey() throws Exception {
        RequestMessage requestMessage = new RequestMessage();
        requestMessage.setBucketName(BUCKET_NAME);
        requestMessage.setEndpoint(asUri(ENDPOINT));

        String url = requestMessage.buildCanonicalURL();
        String canonicalUrl = SCHEME + BUCKET_NAME + "." + ENDPOINT;
        assertEquals(url, canonicalUrl);

        requestMessage = new RequestMessage();
        requestMessage.setEndpoint(asUri(ENDPOINT));

        url = requestMessage.buildCanonicalURL();
        canonicalUrl = SCHEME + ENDPOINT;
        assertEquals(url, canonicalUrl);

        requestMessage = new RequestMessage();
        requestMessage.setObjectKey(OBJECT_KEY);
        requestMessage.setEndpoint(asUri(ENDPOINT));
        requestMessage.setPathStyleAccessEnable(true);

        url = requestMessage.buildCanonicalURL();
        canonicalUrl = SCHEME + ENDPOINT;
        assertEquals(url, canonicalUrl);
    }

    @Test
    public void testBuildUrlWithNullEndpoint() {
        Exception exception = null;
        try {
            URI endpoint = new URI(ENDPOINT);
            RequestMessage requestMessage = new RequestMessage();
            requestMessage.setBucketName(BUCKET_NAME);
            requestMessage.setEndpoint(endpoint);

            String url = requestMessage.buildCanonicalURL();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (Exception e) {
            exception = e;
        }

        assertTrue(exception.getMessage().contains("Host name can not be null."));
    }
}
