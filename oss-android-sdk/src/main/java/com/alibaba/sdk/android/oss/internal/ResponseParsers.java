package com.alibaba.sdk.android.oss.internal;

import android.text.TextUtils;
import android.util.Xml;

import com.alibaba.sdk.android.oss.ClientException;
import com.alibaba.sdk.android.oss.ServiceException;
import com.alibaba.sdk.android.oss.common.OSSHeaders;
import com.alibaba.sdk.android.oss.common.OSSLog;
import com.alibaba.sdk.android.oss.common.utils.CRC64;
import com.alibaba.sdk.android.oss.common.utils.DateUtil;
import com.alibaba.sdk.android.oss.common.utils.OSSUtils;
import com.alibaba.sdk.android.oss.model.AbortMultipartUploadResult;
import com.alibaba.sdk.android.oss.model.AppendObjectResult;
import com.alibaba.sdk.android.oss.model.CannedAccessControlList;
import com.alibaba.sdk.android.oss.model.CompleteMultipartUploadResult;
import com.alibaba.sdk.android.oss.model.CopyObjectResult;
import com.alibaba.sdk.android.oss.model.CreateBucketResult;
import com.alibaba.sdk.android.oss.model.DeleteBucketResult;
import com.alibaba.sdk.android.oss.model.DeleteMultipleObjectResult;
import com.alibaba.sdk.android.oss.model.DeleteObjectResult;
import com.alibaba.sdk.android.oss.model.GetBucketACLResult;
import com.alibaba.sdk.android.oss.model.GetBucketInfoResult;
import com.alibaba.sdk.android.oss.model.GetObjectACLResult;
import com.alibaba.sdk.android.oss.model.GetObjectResult;
import com.alibaba.sdk.android.oss.model.GetSymlinkResult;
import com.alibaba.sdk.android.oss.model.HeadObjectResult;
import com.alibaba.sdk.android.oss.model.ImagePersistResult;
import com.alibaba.sdk.android.oss.model.InitiateMultipartUploadResult;
import com.alibaba.sdk.android.oss.model.ListBucketsResult;
import com.alibaba.sdk.android.oss.model.ListMultipartUploadsResult;
import com.alibaba.sdk.android.oss.model.ListObjectsResult;
import com.alibaba.sdk.android.oss.model.ListPartsResult;
import com.alibaba.sdk.android.oss.model.OSSBucketSummary;
import com.alibaba.sdk.android.oss.model.OSSObjectSummary;
import com.alibaba.sdk.android.oss.model.ObjectMetadata;
import com.alibaba.sdk.android.oss.model.Owner;
import com.alibaba.sdk.android.oss.model.PartSummary;
import com.alibaba.sdk.android.oss.model.PutObjectResult;
import com.alibaba.sdk.android.oss.model.PutSymlinkResult;
import com.alibaba.sdk.android.oss.model.RestoreObjectResult;
import com.alibaba.sdk.android.oss.model.TriggerCallbackResult;
import com.alibaba.sdk.android.oss.model.UploadPartResult;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by zhouzhuo on 11/23/15.
 */
public final class ResponseParsers {

    private static CopyObjectResult parseCopyObjectResponseXML(InputStream in, CopyObjectResult result)
            throws Exception {

        XmlPullParser parser = Xml.newPullParser();
        parser.setInput(in, "utf-8");
        int eventType = parser.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT) {
            switch (eventType) {
                case XmlPullParser.START_TAG:
                    String name = parser.getName();
                    if ("LastModified".equals(name)) {
                        result.setLastModified(DateUtil.parseIso8601Date(parser.nextText()));
                    } else if ("ETag".equals(name)) {
                        result.setEtag(parser.nextText());
                    }
                    break;
            }

            eventType = parser.next();
            if (eventType == XmlPullParser.TEXT) {
                eventType = parser.next();
            }
        }
        return result;
    }

    private static ListPartsResult parseListPartsResponseXML(InputStream in, ListPartsResult result)
            throws Exception {

        List<PartSummary> partEtagList = new ArrayList<PartSummary>();
        PartSummary partSummary = null;
        XmlPullParser parser = Xml.newPullParser();
        parser.setInput(in, "utf-8");
        int eventType = parser.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT) {
            switch (eventType) {
                case XmlPullParser.START_TAG:
                    String name = parser.getName();
                    if ("Bucket".equals(name)) {
                        result.setBucketName(parser.nextText());
                    } else if ("Key".equals(name)) {
                        result.setKey(parser.nextText());
                    } else if ("UploadId".equals(name)) {
                        result.setUploadId(parser.nextText());
                    } else if ("PartNumberMarker".equals(name)) {
                        String partNumberMarker = parser.nextText();
                        if (!OSSUtils.isEmptyString(partNumberMarker)) {
                            result.setPartNumberMarker(Integer.parseInt(partNumberMarker));
                        }
                    } else if ("NextPartNumberMarker".equals(name)) {
                        String nextPartNumberMarker = parser.nextText();
                        if (!OSSUtils.isEmptyString(nextPartNumberMarker)) {
                            result.setNextPartNumberMarker(Integer.parseInt(nextPartNumberMarker));
                        }
                    } else if ("MaxParts".equals(name)) {
                        String maxParts = parser.nextText();
                        if (!OSSUtils.isEmptyString(maxParts)) {
                            result.setMaxParts(Integer.parseInt(maxParts));
                        }
                    } else if ("IsTruncated".equals(name)) {
                        String isTruncated = parser.nextText();
                        if (!OSSUtils.isEmptyString(isTruncated)) {
                            result.setTruncated(Boolean.valueOf(isTruncated));
                        }
                    } else if ("StorageClass".equals(name)) {
                        result.setStorageClass(parser.nextText());
                    } else if ("Part".equals(name)) {
                        partSummary = new PartSummary();
                    } else if ("PartNumber".equals(name)) {
                        String partNum = parser.nextText();
                        if (!OSSUtils.isEmptyString(partNum)) {
                            partSummary.setPartNumber(Integer.valueOf(partNum));
                        }
                    } else if ("LastModified".equals(name)) {
                        partSummary.setLastModified(DateUtil.parseIso8601Date(parser.nextText()));
                    } else if ("ETag".equals(name)) {
                        partSummary.setETag(parser.nextText());
                    } else if ("Size".equals(name)) {
                        String size = parser.nextText();
                        if (!OSSUtils.isEmptyString(size)) {
                            partSummary.setSize(Long.valueOf(size));
                        }
                    }
                    break;
                case XmlPullParser.END_TAG:
                    if ("Part".equals(parser.getName())) {
                        partEtagList.add(partSummary);
                    }
                    break;
            }
            eventType = parser.next();
            if (eventType == XmlPullParser.TEXT) {
                eventType = parser.next();
            }
        }

        if (partEtagList.size() > 0) {
            result.setParts(partEtagList);
        }

        return result;
    }

    private static CompleteMultipartUploadResult parseCompleteMultipartUploadResponseXML(InputStream in, CompleteMultipartUploadResult result)
            throws Exception {
        XmlPullParser parser = Xml.newPullParser();
        parser.setInput(in, "utf-8");
        int eventType = parser.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT) {
            switch (eventType) {
                case XmlPullParser.START_TAG:
                    String name = parser.getName();
                    if ("Location".equals(name)) {
                        result.setLocation(parser.nextText());
                    } else if ("Bucket".equals(name)) {
                        result.setBucketName(parser.nextText());
                    } else if ("Key".equals(name)) {
                        result.setObjectKey(parser.nextText());
                    } else if ("ETag".equals(name)) {
                        result.setETag(parser.nextText());
                    }
                    break;
            }

            eventType = parser.next();
            if (eventType == XmlPullParser.TEXT) {
                eventType = parser.next();
            }
        }

        return result;
    }

    private static InitiateMultipartUploadResult parseInitMultipartResponseXML(InputStream in, InitiateMultipartUploadResult result)
            throws Exception {
        XmlPullParser parser = Xml.newPullParser();
        parser.setInput(in, "utf-8");
        int eventType = parser.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT) {
            switch (eventType) {
                case XmlPullParser.START_TAG:
                    String name = parser.getName();
                    if ("Bucket".equals(name)) {
                        result.setBucketName(parser.nextText());
                    } else if ("Key".equals(name)) {
                        result.setObjectKey(parser.nextText());
                    } else if ("UploadId".equals(name)) {
                        result.setUploadId(parser.nextText());
                    }
                    break;
            }

            eventType = parser.next();
            if (eventType == XmlPullParser.TEXT) {
                eventType = parser.next();
            }
        }
        return result;
    }

    /**
     * Parse the response of GetObjectACL
     *
     * @param in
     * @param result
     * @return
     */
    private static GetObjectACLResult parseGetObjectACLResponse(InputStream in, GetObjectACLResult result)
            throws Exception {
        XmlPullParser parser = Xml.newPullParser();
        parser.setInput(in, "utf-8");
        int eventType = parser.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT) {
            switch (eventType) {
                case XmlPullParser.START_TAG:
                    String name = parser.getName();
                    if ("Grant".equals(name)) {
                        result.setObjectACL(parser.nextText());
                    } else if ("ID".equals(name)) {
                        result.setObjectOwnerID(parser.nextText());
                    } else if ("DisplayName".equals(name)) {
                        result.setObjectOwner(parser.nextText());
                    }
                    break;
            }

            eventType = parser.next();
            if (eventType == XmlPullParser.TEXT) {
                eventType = parser.next();
            }
        }
        return result;
    }

    /**
     * Parse the response of GetBucketInfo
     *
     * @param in
     * @return
     * @throws Exception
     */
    private static GetBucketInfoResult parseGetBucketInfoResponse(InputStream in, GetBucketInfoResult result)
            throws Exception {
        XmlPullParser parser = Xml.newPullParser();
        parser.setInput(in, "utf-8");
        int eventType = parser.getEventType();
        OSSBucketSummary bucket = null;
        Owner owner = null;
        while (eventType != XmlPullParser.END_DOCUMENT) {
            switch (eventType) {
                case XmlPullParser.START_TAG:
                    String name = parser.getName();
                    if (name == null) {
                        break;
                    }

                    if ("Owner".equals(name)) {
                        owner = new Owner();
                    } else if ("ID".equals(name)) {
                        if (owner != null) {
                            owner.setId(parser.nextText());
                        }
                    } else if ("DisplayName".equals(name)) {
                        if (owner != null) {
                            owner.setDisplayName(parser.nextText());
                        }
                    } else if ("Bucket".equals(name)) {
                        bucket = new OSSBucketSummary();
                    } else if ("CreationDate".equals(name)) {
                        if (bucket != null) {
                            bucket.createDate = DateUtil.parseIso8601Date(parser.nextText());
                        }
                    } else if ("ExtranetEndpoint".equals(name)) {
                        if (bucket != null) {
                            bucket.extranetEndpoint = parser.nextText();
                        }
                    } else if ("IntranetEndpoint".equals(name)) {
                        if (bucket != null) {
                            bucket.intranetEndpoint = parser.nextText();
                        }
                    } else if ("Location".equals(name)) {
                        if (bucket != null) {
                            bucket.location = parser.nextText();
                        }
                    } else if ("Name".equals(name)) {
                        if (bucket != null) {
                            bucket.name = parser.nextText();
                        }
                    } else if ("StorageClass".equals(name)) {
                        if (bucket != null) {
                            bucket.storageClass = parser.nextText();
                        }
                    } else if ("Grant".equals(name)) {
                        if (bucket != null) {
                            bucket.setAcl(parser.nextText());
                        }
                    }
                    break;
                case XmlPullParser.END_TAG:
                    String endTagName = parser.getName();
                    if (endTagName == null) {
                        break;
                    }

                    if ("Bucket".equals(endTagName)) {
                        if (bucket != null) {
                            result.setBucket(bucket);
                        }
                    } else if ("Owner".equals(endTagName)) {
                        if (bucket != null) {
                            bucket.owner = owner;
                        }
                    }

                    break;
            }
            eventType = parser.next();
            if (eventType == XmlPullParser.TEXT) {
                eventType = parser.next();
            }
        }
        return result;
    }

    /**
     * Parse the response of GetBucketACL
     *
     * @param in
     * @return
     * @throws Exception
     */
    private static GetBucketACLResult parseGetBucketACLResponse(InputStream in, GetBucketACLResult result)
            throws Exception {
        XmlPullParser parser = Xml.newPullParser();
        parser.setInput(in, "utf-8");
        int eventType = parser.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT) {
            switch (eventType) {
                case XmlPullParser.START_TAG:
                    String name = parser.getName();
                    if ("Grant".equals(name)) {
                        result.setBucketACL(parser.nextText());
                    } else if ("ID".equals(name)) {
                        result.setBucketOwnerID(parser.nextText());
                    } else if ("DisplayName".equals(name)) {
                        result.setBucketOwner(parser.nextText());
                    }
                    break;
            }

            eventType = parser.next();
            if (eventType == XmlPullParser.TEXT) {
                eventType = parser.next();
            }
        }
        return result;
    }

    private static DeleteMultipleObjectResult parseDeleteMultipleObjectResponse(InputStream in, DeleteMultipleObjectResult result)
            throws Exception {
        XmlPullParser parser = Xml.newPullParser();
        parser.setInput(in, "utf-8");
        int eventType = parser.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT) {
            switch (eventType) {
                case XmlPullParser.START_TAG:
                    String name = parser.getName();
                    if ("Key".equals(name)) {
                        result.addDeletedObject(parser.nextText());
                    }
                    break;
            }

            eventType = parser.next();
            if (eventType == XmlPullParser.TEXT) {
                eventType = parser.next();
            }
        }
        return result;
    }

    /**
     * Parse the response of listBucketInService
     *
     * @param in
     * @param result
     * @return
     * @throws IOException
     */
    private static ListBucketsResult parseBucketListResponse(InputStream in, ListBucketsResult result)
            throws Exception {
        result.clearBucketList();
        XmlPullParser parser = Xml.newPullParser();
        parser.setInput(in, "utf-8");
        int eventType = parser.getEventType();
        OSSBucketSummary bucket = null;
        while (eventType != XmlPullParser.END_DOCUMENT) {
            switch (eventType) {
                case XmlPullParser.START_TAG:
                    String name = parser.getName();
                    if (name == null) {
                        break;
                    }
                    if ("Prefix".equals(name)) {
                        result.setPrefix(parser.nextText());
                    } else if ("Marker".equals(name)) {
                        result.setMarker(parser.nextText());
                    } else if ("MaxKeys".equals(name)) {
                        String maxKeys = parser.nextText();
                        if (maxKeys != null) {
                            result.setMaxKeys(Integer.valueOf(maxKeys));
                        }
                    } else if ("IsTruncated".equals(name)) {
                        String isTruncated = parser.nextText();
                        if (isTruncated != null) {
                            result.setTruncated(Boolean.valueOf(isTruncated));
                        }
                    } else if ("NextMarker".equals(name)) {
                        result.setNextMarker(parser.nextText());
                    } else if ("ID".equals(name)) {
                        result.setOwnerId(parser.nextText());
                    } else if ("DisplayName".equals(name)) {
                        result.setOwnerDisplayName(parser.nextText());
                    } else if ("Bucket".equals(name)) {
                        bucket = new OSSBucketSummary();
                    } else if ("CreationDate".equals(name)) {
                        if (bucket != null) {
                            bucket.createDate = DateUtil.parseIso8601Date(parser.nextText());
                        }
                    } else if ("ExtranetEndpoint".equals(name)) {
                        if (bucket != null) {
                            bucket.extranetEndpoint = parser.nextText();
                        }
                    } else if ("IntranetEndpoint".equals(name)) {
                        if (bucket != null) {
                            bucket.intranetEndpoint = parser.nextText();
                        }
                    } else if ("Location".equals(name)) {
                        if (bucket != null) {
                            bucket.location = parser.nextText();
                        }
                    } else if ("Name".equals(name)) {
                        if (bucket != null) {
                            bucket.name = parser.nextText();
                        }
                    } else if ("StorageClass".equals(name)) {
                        if (bucket != null) {
                            bucket.storageClass = parser.nextText();
                        }
                    }
                    break;
                case XmlPullParser.END_TAG:
                    String endTagName = parser.getName();
                    if ("Bucket".equals(endTagName)) {
                        if (bucket != null) {
                            result.addBucket(bucket);
                        }
                    }
                    break;
            }
            eventType = parser.next();
            if (eventType == XmlPullParser.TEXT) {
                eventType = parser.next();
            }
        }
        return result;
    }

    /**
     * Parse the response of listObjectInBucket
     *
     * @param in
     * @return
     * @throws Exception
     */
    private static ListObjectsResult parseObjectListResponse(InputStream in, ListObjectsResult result)
            throws Exception {
        result.clearCommonPrefixes();
        result.clearObjectSummaries();
        XmlPullParser parser = Xml.newPullParser();
        parser.setInput(in, "utf-8");
        int eventType = parser.getEventType();
        OSSObjectSummary object = null;
        Owner owner = null;
        boolean isCommonPrefixes = false;
        while (eventType != XmlPullParser.END_DOCUMENT) {
            switch (eventType) {
                case XmlPullParser.START_TAG:
                    String name = parser.getName();
                    if ("Name".equals(name)) {
                        result.setBucketName(parser.nextText());
                    } else if ("Prefix".equals(name)) {
                        if (isCommonPrefixes) {
                            String commonPrefix = parser.nextText();
                            if (!OSSUtils.isEmptyString(commonPrefix)) {
                                result.addCommonPrefix(commonPrefix);
                            }
                        } else {
                            result.setPrefix(parser.nextText());
                        }

                    } else if ("Marker".equals(name)) {
                        result.setMarker(parser.nextText());
                    } else if ("Delimiter".equals(name)) {
                        result.setDelimiter(parser.nextText());
                    } else if ("EncodingType".equals(name)) {
                        result.setEncodingType(parser.nextText());
                    } else if ("MaxKeys".equals(name)) {
                        String maxKeys = parser.nextText();
                        if (!OSSUtils.isEmptyString(maxKeys)) {
                            result.setMaxKeys(Integer.valueOf(maxKeys));
                        }
                    } else if ("NextMarker".equals(name)) {
                        result.setNextMarker(parser.nextText());
                    } else if ("IsTruncated".equals(name)) {
                        String isTruncated = parser.nextText();
                        if (!OSSUtils.isEmptyString(isTruncated)) {
                            result.setTruncated(Boolean.valueOf(isTruncated));
                        }
                    } else if ("Contents".equals(name)) {
                        object = new OSSObjectSummary();
                    } else if ("Key".equals(name)) {
                        object.setKey(parser.nextText());
                    } else if ("LastModified".equals(name)) {
                        object.setLastModified(DateUtil.parseIso8601Date(parser.nextText()));
                    } else if ("Size".equals(name)) {
                        String size = parser.nextText();
                        if (!OSSUtils.isEmptyString(size)) {
                            object.setSize(Long.valueOf(size));
                        }
                    } else if ("ETag".equals(name)) {
                        object.setETag(parser.nextText());
                    } else if ("Type".equals(name)) {
                        object.setType(parser.nextText());
                    } else if ("StorageClass".equals(name)) {
                        object.setStorageClass(parser.nextText());
                    } else if ("Owner".equals(name)) {
                        owner = new Owner();
                    } else if ("ID".equals(name)) {
                        owner.setId(parser.nextText());
                    } else if ("DisplayName".equals(name)) {
                        owner.setDisplayName(parser.nextText());
                    } else if ("CommonPrefixes".equals(name)) {
                        isCommonPrefixes = true;
                    }
                    break;
                case XmlPullParser.END_TAG:
                    String endTagName = parser.getName();
                    if ("Owner".equals(parser.getName())) {
                        if (owner != null) {
                            object.setOwner(owner);
                        }
                    } else if ("Contents".equals(endTagName)) {
                        if (object != null) {
                            object.setBucketName(result.getBucketName());
                            result.addObjectSummary(object);
                        }
                    } else if ("CommonPrefixes".equals(endTagName)) {
                        isCommonPrefixes = false;
                    }
                    break;
            }

            eventType = parser.next();
            if (eventType == XmlPullParser.TEXT) {
                eventType = parser.next();
            }
        }

        return result;
    }

    public static String trimQuotes(String s) {
        if (s == null) return null;

        s = s.trim();
        if (s.startsWith("\"")) s = s.substring(1);
        if (s.endsWith("\"")) s = s.substring(0, s.length() - 1);

        return s;
    }

    /**
     * Unmarshall object metadata from response headers.
     */
    public static ObjectMetadata parseObjectMetadata(Map<String, String> headers)
            throws Exception {

        try {
            ObjectMetadata objectMetadata = new ObjectMetadata();

            for (Iterator<String> it = headers.keySet().iterator(); it.hasNext(); ) {
                String key = it.next();

                if (key.indexOf(OSSHeaders.OSS_USER_METADATA_PREFIX) >= 0) {
                    objectMetadata.addUserMetadata(key, headers.get(key));
                } else if (key.equalsIgnoreCase(OSSHeaders.LAST_MODIFIED) || key.equalsIgnoreCase(OSSHeaders.DATE)) {
                    try {
                        objectMetadata.setHeader(key, DateUtil.parseRfc822Date(headers.get(key)));
                    } catch (ParseException pe) {
                        throw new IOException(pe.getMessage(), pe);
                    }
                } else if (key.equalsIgnoreCase(OSSHeaders.CONTENT_LENGTH)) {
                    Long value = Long.valueOf(headers.get(key));
                    objectMetadata.setHeader(key, value);
                } else if (key.equalsIgnoreCase(OSSHeaders.ETAG)) {
                    objectMetadata.setHeader(key, trimQuotes(headers.get(key)));
                } else {
                    objectMetadata.setHeader(key, headers.get(key));
                }
            }

            return objectMetadata;
        } catch (Exception e) {
            throw new IOException(e.getMessage(), e);
        }
    }

    public static ServiceException parseResponseErrorXML(ResponseMessage response, boolean isHeadRequest)
            throws ClientException {

        int statusCode = response.getStatusCode();
        String requestId = response.getResponse().header(OSSHeaders.OSS_HEADER_REQUEST_ID);
        String code = null;
        String message = null;
        String hostId = null;
        String partNumber = null;
        String partEtag = null;
        String errorMessage = null;
        if (!isHeadRequest) {
            try {
                errorMessage = response.getResponse().body().string();
                OSSLog.logDebug("errorMessage  ： " + " \n " +  errorMessage);
                InputStream inputStream = new ByteArrayInputStream(errorMessage.getBytes());
                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(inputStream, "utf-8");
                int eventType = parser.getEventType();
                while (eventType != XmlPullParser.END_DOCUMENT) {
                    switch (eventType) {
                        case XmlPullParser.START_TAG:
                            if ("Code".equals(parser.getName())) {
                                code = parser.nextText();
                            } else if ("Message".equals(parser.getName())) {
                                message = parser.nextText();
                            } else if ("RequestId".equals(parser.getName())) {
                                requestId = parser.nextText();
                            } else if ("HostId".equals(parser.getName())) {
                                hostId = parser.nextText();
                            } else if ("PartNumber".equals(parser.getName())) {
                                partNumber = parser.nextText();
                            } else if ("PartEtag".equals(parser.getName())) {
                                partEtag = parser.nextText();
                            }
                            break;
                    }
                    eventType = parser.next();
                    if (eventType == XmlPullParser.TEXT) {
                        eventType = parser.next();
                    }
                }

            } catch (IOException e) {
                throw new ClientException(e);
            } catch (XmlPullParserException e) {
                throw new ClientException(e);
            }
        }

        ServiceException serviceException = new ServiceException(statusCode, message, code, requestId, hostId, errorMessage);
        if (!TextUtils.isEmpty(partEtag)) {
            serviceException.setPartEtag(partEtag);
        }

        if (!TextUtils.isEmpty(partNumber)) {
            serviceException.setPartNumber(partNumber);
        }


        return serviceException;
    }

    public static final class PutObjectResponseParser extends AbstractResponseParser<PutObjectResult> {

        @Override
        public PutObjectResult parseData(ResponseMessage response, PutObjectResult result)
                throws IOException {
            result.setETag(trimQuotes(response.getHeaders().get(OSSHeaders.ETAG)));
            String body = response.getResponse().body().string();
            if (!TextUtils.isEmpty(body)) {
                result.setServerCallbackReturnBody(body);
            }
            return result;
        }
    }

    public static final class AppendObjectResponseParser extends AbstractResponseParser<AppendObjectResult> {

        @Override
        public AppendObjectResult parseData(ResponseMessage response, AppendObjectResult result) throws IOException {
            String nextPosition = response.getHeaders().get(OSSHeaders.OSS_NEXT_APPEND_POSITION);
            if (nextPosition != null) {
                result.setNextPosition(Long.valueOf(nextPosition));
            }
            result.setObjectCRC64(response.getHeaders().get(OSSHeaders.OSS_HASH_CRC64_ECMA));
            return result;
        }
    }

    public static final class HeadObjectResponseParser extends AbstractResponseParser<HeadObjectResult> {

        @Override
        public HeadObjectResult parseData(ResponseMessage response, HeadObjectResult result) throws Exception {
            result.setMetadata(parseObjectMetadata(result.getResponseHeader()));
            return result;
        }
    }

    public static final class GetObjectResponseParser extends AbstractResponseParser<GetObjectResult> {

        @Override
        public GetObjectResult parseData(ResponseMessage response, GetObjectResult result) throws Exception {
            result.setMetadata(parseObjectMetadata(result.getResponseHeader()));
            result.setContentLength(response.getContentLength());
            if (response.getRequest().isCheckCRC64()) {
                result.setObjectContent(new CheckCRC64DownloadInputStream(response.getContent()
                        , new CRC64(), response.getContentLength()
                        , result.getServerCRC(), result.getRequestId()));
            } else {
                result.setObjectContent(response.getContent());
            }
            return result;
        }

        @Override
        public boolean needCloseResponse() {
            // keep body stream open for reading content
            return false;
        }
    }

    public static final class GetObjectACLResponseParser extends AbstractResponseParser<GetObjectACLResult> {

        @Override
        GetObjectACLResult parseData(ResponseMessage response, GetObjectACLResult result) throws Exception {
            result = parseGetObjectACLResponse(response.getContent(), result);
            return result;
        }
    }

    public static final class CopyObjectResponseParser extends AbstractResponseParser<CopyObjectResult> {

        @Override
        public CopyObjectResult parseData(ResponseMessage response, CopyObjectResult result) throws Exception {
            result = parseCopyObjectResponseXML(response.getContent(), result);
            return result;
        }
    }

    public static final class CreateBucketResponseParser extends AbstractResponseParser<CreateBucketResult> {

        @Override
        public CreateBucketResult parseData(ResponseMessage response, CreateBucketResult result) throws Exception {
            if (result.getResponseHeader().containsKey("Location")) {
                result.bucketLocation = result.getResponseHeader().get("Location");
            }
            return result;
        }
    }

    public static final class DeleteBucketResponseParser extends AbstractResponseParser<DeleteBucketResult> {

        @Override
        public DeleteBucketResult parseData(ResponseMessage response, DeleteBucketResult result) throws Exception {
            return result;
        }
    }

    public static final class GetBucketInfoResponseParser extends AbstractResponseParser<GetBucketInfoResult> {

        @Override
        public GetBucketInfoResult parseData(ResponseMessage response, GetBucketInfoResult result) throws Exception {
            result = parseGetBucketInfoResponse(response.getContent(), result);
            return result;
        }
    }

    public static final class GetBucketACLResponseParser extends AbstractResponseParser<GetBucketACLResult> {

        @Override
        public GetBucketACLResult parseData(ResponseMessage response, GetBucketACLResult result) throws Exception {
            result = parseGetBucketACLResponse(response.getContent(), result);
            return result;
        }
    }

    public static final class DeleteObjectResponseParser extends AbstractResponseParser<DeleteObjectResult> {

        @Override
        public DeleteObjectResult parseData(ResponseMessage response, DeleteObjectResult result) throws Exception {
            return result;
        }
    }

    public static final class DeleteMultipleObjectResponseParser extends AbstractResponseParser<DeleteMultipleObjectResult> {

        @Override
        DeleteMultipleObjectResult parseData(ResponseMessage response, DeleteMultipleObjectResult result) throws Exception {
            result = parseDeleteMultipleObjectResponse(response.getContent(), result);
            return result;
        }
    }

    public static final class ListObjectsResponseParser extends AbstractResponseParser<ListObjectsResult> {

        @Override
        public ListObjectsResult parseData(ResponseMessage response, ListObjectsResult result) throws Exception {
            result = parseObjectListResponse(response.getContent(), result);
            return result;
        }
    }

    public static final class ListBucketResponseParser extends AbstractResponseParser<ListBucketsResult> {

        @Override
        ListBucketsResult parseData(ResponseMessage response, ListBucketsResult result) throws Exception {
            result = parseBucketListResponse(response.getContent(), result);
            return result;
        }
    }

    public static final class InitMultipartResponseParser extends AbstractResponseParser<InitiateMultipartUploadResult> {

        @Override
        public InitiateMultipartUploadResult parseData(ResponseMessage response, InitiateMultipartUploadResult result) throws Exception {
            return parseInitMultipartResponseXML(response.getContent(), result);
        }
    }

    public static final class UploadPartResponseParser extends AbstractResponseParser<UploadPartResult> {

        @Override
        public UploadPartResult parseData(ResponseMessage response, UploadPartResult result) throws Exception {
            result.setETag(trimQuotes(response.getHeaders().get(OSSHeaders.ETAG)));
            return result;
        }
    }

    public static final class AbortMultipartUploadResponseParser extends AbstractResponseParser<AbortMultipartUploadResult> {

        @Override
        public AbortMultipartUploadResult parseData(ResponseMessage response, AbortMultipartUploadResult result) throws Exception {
            return result;
        }
    }

    public static final class CompleteMultipartUploadResponseParser extends AbstractResponseParser<CompleteMultipartUploadResult> {

        @Override
        public CompleteMultipartUploadResult parseData(ResponseMessage response, CompleteMultipartUploadResult result) throws Exception {
            if (response.getHeaders().get(OSSHeaders.CONTENT_TYPE).equals("application/xml")) {
                result = parseCompleteMultipartUploadResponseXML(response.getContent(), result);
            } else {
                String body = response.getResponse().body().string();
                if (!TextUtils.isEmpty(body)) {
                    result.setServerCallbackReturnBody(body);
                }
            }
            return result;
        }
    }

    public static final class ListPartsResponseParser extends AbstractResponseParser<ListPartsResult> {

        @Override
        public ListPartsResult parseData(ResponseMessage response, ListPartsResult result) throws Exception {
            result = parseListPartsResponseXML(response.getContent(), result);
            return result;
        }
    }

    public static final class ListMultipartUploadsResponseParser extends AbstractResponseParser<ListMultipartUploadsResult> {

        @Override
        public ListMultipartUploadsResult parseData(ResponseMessage response, ListMultipartUploadsResult result) throws Exception {
            return result.parseData(response);
        }
    }

    public static final class TriggerCallbackResponseParser extends AbstractResponseParser<TriggerCallbackResult> {

        @Override
        public TriggerCallbackResult parseData(ResponseMessage response, TriggerCallbackResult result) throws Exception {
            String body = response.getResponse().body().string();
            if (!TextUtils.isEmpty(body)) {
                result.setServerCallbackReturnBody(body);
            }
            return result;
        }
    }

    public static final class ImagePersistResponseParser extends AbstractResponseParser<ImagePersistResult> {

        @Override
        public ImagePersistResult parseData(ResponseMessage response, ImagePersistResult result) throws Exception {
            return result;
        }
    }

    public static final class PutSymlinkResponseParser extends AbstractResponseParser<PutSymlinkResult> {

        @Override
        PutSymlinkResult parseData(ResponseMessage response, PutSymlinkResult result) throws Exception {
            return result;
        }
    }

    public static final class GetSymlinkResponseParser extends AbstractResponseParser<GetSymlinkResult> {

        @Override
        GetSymlinkResult parseData(ResponseMessage response, GetSymlinkResult result) throws Exception {
            result.setTargetObjectName(response.getHeaders().get(OSSHeaders.OSS_HEADER_SYMLINK_TARGET));
            return result;
        }
    }

    public static final class RestoreObjectResponseParser extends AbstractResponseParser<RestoreObjectResult> {

        @Override
        RestoreObjectResult parseData(ResponseMessage response, RestoreObjectResult result) throws Exception {
            return result;
        }
    }
}
