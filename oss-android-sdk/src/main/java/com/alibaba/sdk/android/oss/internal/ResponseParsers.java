package com.alibaba.sdk.android.oss.internal;

import com.alibaba.sdk.android.oss.ServiceException;
import com.alibaba.sdk.android.oss.common.OSSHeaders;
import com.alibaba.sdk.android.oss.common.OSSLog;
import com.alibaba.sdk.android.oss.common.utils.DateUtil;
import com.alibaba.sdk.android.oss.model.AbortMultipartUploadResult;
import com.alibaba.sdk.android.oss.model.AppendObjectResult;
import com.alibaba.sdk.android.oss.model.CompleteMultipartUploadResult;
import com.alibaba.sdk.android.oss.model.CopyObjectResult;
import com.alibaba.sdk.android.oss.model.DeleteBucketResult;
import com.alibaba.sdk.android.oss.model.DeleteObjectResult;
import com.alibaba.sdk.android.oss.model.GetBucketACLResult;
import com.alibaba.sdk.android.oss.model.GetObjectResult;
import com.alibaba.sdk.android.oss.model.HeadObjectResult;
import com.alibaba.sdk.android.oss.model.InitiateMultipartUploadResult;
import com.alibaba.sdk.android.oss.model.ListObjectsResult;
import com.alibaba.sdk.android.oss.model.ListPartsResult;
import com.alibaba.sdk.android.oss.model.OSSObjectSummary;
import com.alibaba.sdk.android.oss.model.ObjectMetadata;
import com.alibaba.sdk.android.oss.model.PartSummary;
import com.alibaba.sdk.android.oss.model.CreateBucketResult;
import com.alibaba.sdk.android.oss.model.PutObjectResult;
import com.alibaba.sdk.android.oss.model.UploadPartResult;
import okhttp3.Headers;
import okhttp3.Response;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Created by zhouzhuo on 11/23/15.
 */
public final class ResponseParsers {

    public static final DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();

    public static final class PutObjectReponseParser implements ResponseParser<PutObjectResult> {

        @Override
        public PutObjectResult parse(Response response)
                throws IOException {
            try {
                PutObjectResult result = new PutObjectResult();
                result.setRequestId(response.header(OSSHeaders.OSS_HEADER_REQUEST_ID));
                result.setStatusCode(response.code());
                result.setResponseHeader(parseResponseHeader(response));

                result.setETag(trimQuotes(response.header(OSSHeaders.ETAG)));
                if (response.body().contentLength() > 0) {
                    result.setServerCallbackReturnBody(response.body().string());
                }
                return result;
            } finally {
                safeCloseResponse(response);
            }
        }
    }

    public static final class AppendObjectResponseParser implements ResponseParser<AppendObjectResult> {

        @Override
        public AppendObjectResult parse(Response response) throws IOException {
            try {
                AppendObjectResult result = new AppendObjectResult();
                result.setRequestId(response.header(OSSHeaders.OSS_HEADER_REQUEST_ID));
                result.setStatusCode(response.code());
                result.setResponseHeader(parseResponseHeader(response));

                String nextPosition = response.header(OSSHeaders.OSS_NEXT_APPEND_POSITION);
                if (nextPosition != null) {
                    result.setNextPosition(Long.valueOf(nextPosition));
                }
                result.setObjectCRC64(response.header(OSSHeaders.OSS_HASH_CRC64_ECMA));
                return result;
            } finally {
                safeCloseResponse(response);
            }
        }
    }

    public static final class HeadObjectResponseParser implements ResponseParser<HeadObjectResult> {

        @Override
        public HeadObjectResult parse(Response response) throws IOException {
            HeadObjectResult result = new HeadObjectResult();
            try {
                result.setRequestId(response.header(OSSHeaders.OSS_HEADER_REQUEST_ID));
                result.setStatusCode(response.code());
                result.setResponseHeader(parseResponseHeader(response));
                result.setMetadata(parseObjectMetadata(result.getResponseHeader()));
                return result;
            } finally {
                safeCloseResponse(response);
            }
        }
    }

    public static final class GetObjectResponseParser implements ResponseParser<GetObjectResult> {

        @Override
        public GetObjectResult parse(Response response) throws IOException {
            GetObjectResult result = new GetObjectResult();

            result.setRequestId(response.header(OSSHeaders.OSS_HEADER_REQUEST_ID));
            result.setStatusCode(response.code());
            result.setResponseHeader(parseResponseHeader(response));
            result.setMetadata(parseObjectMetadata(result.getResponseHeader()));
            result.setContentLength(response.body().contentLength());
            result.setObjectContent(response.body().byteStream());

            // keep body stream open for reading content
            return result;
        }
    }

    public static final class CopyObjectResponseParser implements ResponseParser<CopyObjectResult> {

        @Override
        public CopyObjectResult parse(Response response) throws IOException {
            try {
                CopyObjectResult result = parseCopyObjectResponseXML(response.body().byteStream());
                result.setRequestId(response.header(OSSHeaders.OSS_HEADER_REQUEST_ID));
                result.setStatusCode(response.code());
                result.setResponseHeader(parseResponseHeader(response));
                return result;
            } catch (Exception e) {
                e.printStackTrace();
                throw new IOException(e.getMessage(), e);
            } finally {
                safeCloseResponse(response);
            }
        }
    }

    public static final class CreateBucketResponseParser implements ResponseParser<CreateBucketResult> {

        @Override
        public CreateBucketResult parse(Response response) throws IOException {
            try {
                CreateBucketResult result = new CreateBucketResult();
                result.setRequestId(response.header(OSSHeaders.OSS_HEADER_REQUEST_ID));
                result.setStatusCode(response.code());
                result.setResponseHeader(parseResponseHeader(response));
                return result;
            } catch (Exception e) {
                throw new IOException(e.getMessage(), e);
            } finally {
                safeCloseResponse(response);
            }
        }
    }

    public static final class DeleteBucketResponseParser implements ResponseParser<DeleteBucketResult> {

        @Override
        public DeleteBucketResult parse(Response response) throws IOException {
            try {
                DeleteBucketResult result = new DeleteBucketResult();
                result.setRequestId(response.header(OSSHeaders.OSS_HEADER_REQUEST_ID));
                result.setStatusCode(response.code());
                result.setResponseHeader(parseResponseHeader(response));
                return result;
            } finally {
                safeCloseResponse(response);
            }
        }
    }

    public static final class GetBucketACLResponseParser implements ResponseParser<GetBucketACLResult> {

        @Override
        public GetBucketACLResult parse(Response response) throws IOException {
            try {
                GetBucketACLResult result = parseGetBucketACLResponse(response.body().byteStream());
                result.setRequestId(response.header(OSSHeaders.OSS_HEADER_REQUEST_ID));
                result.setStatusCode(response.code());
                result.setResponseHeader(parseResponseHeader(response));
                return result;
            } catch (Exception e) {
                throw new IOException(e.getMessage(), e);
            } finally {
                safeCloseResponse(response);
            }
        }
    }


    public static final class DeleteObjectResponseParser implements ResponseParser<DeleteObjectResult> {

        @Override
        public DeleteObjectResult parse(Response response) throws IOException {
            DeleteObjectResult result = new DeleteObjectResult();
            try {
                result.setRequestId(response.header(OSSHeaders.OSS_HEADER_REQUEST_ID));
                result.setStatusCode(response.code());
                result.setResponseHeader(parseResponseHeader(response));
                return result;
            } finally {
                safeCloseResponse(response);
            }
        }
    }

    public static final class ListObjectsResponseParser implements ResponseParser<ListObjectsResult> {

        @Override
        public ListObjectsResult parse(Response response) throws IOException {
            try {
                ListObjectsResult result = parseObjectListResponse(response.body().byteStream());

                result.setRequestId(response.header(OSSHeaders.OSS_HEADER_REQUEST_ID));
                result.setStatusCode(response.code());
                result.setResponseHeader(parseResponseHeader(response));

                return result;
            } catch (Exception e) {
                throw new IOException(e.getMessage(), e);
            } finally {
                safeCloseResponse(response);
            }
        }
    }

    public static final class InitMultipartResponseParser implements ResponseParser<InitiateMultipartUploadResult> {

        @Override
        public InitiateMultipartUploadResult parse(Response response) throws IOException {
            try {
                InitiateMultipartUploadResult result = parseInitMultipartResponseXML(response.body().byteStream());

                result.setRequestId(response.header(OSSHeaders.OSS_HEADER_REQUEST_ID));
                result.setStatusCode(response.code());
                result.setResponseHeader(parseResponseHeader(response));

                return result;
            } catch (Exception e) {
                throw new IOException(e.getMessage(), e);
            } finally {
                safeCloseResponse(response);
            }
        }
    }

    public static final class UploadPartResponseParser implements ResponseParser<UploadPartResult> {

        @Override
        public UploadPartResult parse(Response response) throws IOException {
            try {
                UploadPartResult result = new UploadPartResult();

                result.setRequestId(response.header(OSSHeaders.OSS_HEADER_REQUEST_ID));
                result.setStatusCode(response.code());
                result.setResponseHeader(parseResponseHeader(response));
                result.setETag(trimQuotes(response.header(OSSHeaders.ETAG)));

                return result;
            } finally {
                safeCloseResponse(response);
            }
        }
    }

    public static final class AbortMultipartUploadResponseParser implements ResponseParser<AbortMultipartUploadResult> {

        @Override
        public AbortMultipartUploadResult parse(Response response) throws IOException {
            try {
                AbortMultipartUploadResult result = new AbortMultipartUploadResult();

                result.setRequestId(response.header(OSSHeaders.OSS_HEADER_REQUEST_ID));
                result.setStatusCode(response.code());
                result.setResponseHeader(parseResponseHeader(response));

                return result;
            } finally {
                safeCloseResponse(response);
            }
        }
    }

    public static final class CompleteMultipartUploadResponseParser implements ResponseParser<CompleteMultipartUploadResult> {

        @Override
        public CompleteMultipartUploadResult parse(Response response) throws IOException {
            try {
                CompleteMultipartUploadResult result = new CompleteMultipartUploadResult();
                if (response.header(OSSHeaders.CONTENT_TYPE).equals("application/xml")) {
                    result = parseCompleteMultipartUploadResponseXML(response.body().byteStream());
                } else if (response.body() != null) {
                    result.setServerCallbackReturnBody(response.body().string());
                }
                result.setRequestId(response.header(OSSHeaders.OSS_HEADER_REQUEST_ID));
                result.setStatusCode(response.code());
                result.setResponseHeader(parseResponseHeader(response));
                return result;
            } catch (Exception e) {
                throw new IOException(e.getMessage(), e);
            } finally {
                safeCloseResponse(response);
            }
        }
    }

    public static final class ListPartsResponseParser implements ResponseParser<ListPartsResult> {

        @Override
        public ListPartsResult parse(Response response) throws IOException {
            try {
                ListPartsResult result = parseListPartsResponseXML(response.body().byteStream());

                result.setRequestId(response.header(OSSHeaders.OSS_HEADER_REQUEST_ID));
                result.setStatusCode(response.code());
                result.setResponseHeader(parseResponseHeader(response));

                return result;
            } catch (Exception e) {
                throw new IOException(e.getMessage(), e);
            } finally {
                safeCloseResponse(response);
            }
        }
    }

    private static CopyObjectResult parseCopyObjectResponseXML(InputStream in)
            throws ParseException, ParserConfigurationException, IOException, SAXException {

        CopyObjectResult result = new CopyObjectResult();
        DocumentBuilder builder = domFactory.newDocumentBuilder();
        Document dom = builder.parse(in);
        Element element = dom.getDocumentElement();
        OSSLog.logD("[item] - " + element.getNodeName());

        NodeList list = element.getChildNodes();
        for (int i = 0; i < list.getLength(); i++) {
            Node item = list.item(i);
            String name = item.getNodeName();
            if (name == null) {
                continue;
            } else if (name.equals("LastModified")) {
                result.setLastModified(DateUtil.parseIso8601Date(checkChildNotNullAndGetValue(item)));
            } else if (name.equals("ETag")) {
                result.setEtag(checkChildNotNullAndGetValue(item));
            }
        }
        return result;
    }

    private static ListPartsResult parseListPartsResponseXML(InputStream in)
            throws ParserConfigurationException, IOException, SAXException, ParseException {

        ListPartsResult result = new ListPartsResult();
        DocumentBuilder builder = domFactory.newDocumentBuilder();
        Document dom = builder.parse(in);
        Element element = dom.getDocumentElement();
        OSSLog.logD("[parseObjectListResponse] - " + element.getNodeName());

        List<PartSummary> partEtagList = new ArrayList<PartSummary>();
        NodeList list = element.getChildNodes();
        for (int i = 0; i < list.getLength(); i++) {
            Node item = list.item(i);
            String name = item.getNodeName();
            if (name == null) {
                continue;
            } else if (name.equals("Bucket")) {
                result.setBucketName(checkChildNotNullAndGetValue(item));
            } else if (name.equals("Key")) {
                result.setKey(checkChildNotNullAndGetValue(item));
            } else if (name.equals("UploadId")) {
                result.setUploadId(checkChildNotNullAndGetValue(item));
            } else if (name.equals("PartNumberMarker")) {
                String partNumberMarker = checkChildNotNullAndGetValue(item);
                if (partNumberMarker != null) {
                    result.setPartNumberMarker(Integer.valueOf(partNumberMarker));
                }
            } else if (name.equals("NextPartNumberMarker")) {
                String nextPartNumberMarker = checkChildNotNullAndGetValue(item);
                if (nextPartNumberMarker != null) {
                    result.setNextPartNumberMarker(Integer.valueOf(nextPartNumberMarker));
                }
            } else if (name.equals("MaxParts")) {
                String maxParts = checkChildNotNullAndGetValue(item);
                if (maxParts != null) {
                    result.setMaxParts(Integer.valueOf(maxParts));
                }
            } else if (name.equals("IsTruncated")) {
                String isTruncated = checkChildNotNullAndGetValue(item);
                if (isTruncated != null) {
                    result.setTruncated(Boolean.valueOf(isTruncated));
                }
            } else if (name.equals("Part")) {
                NodeList partNodeList = item.getChildNodes();
                PartSummary partSummary = new PartSummary();
                for (int k = 0; k < partNodeList.getLength(); k++) {
                    Node partItem = partNodeList.item(k);
                    String partItemName = partItem.getNodeName();
                    if (partItemName == null) {
                        continue;
                    } else if (partItemName.equals("PartNumber")) {
                        String partNumber = checkChildNotNullAndGetValue(partItem);
                        if (partNumber != null) {
                            partSummary.setPartNumber(Integer.valueOf(partNumber));
                        }
                    } else if (partItemName.equals("LastModified")) {
                        partSummary.setLastModified(DateUtil.parseIso8601Date(checkChildNotNullAndGetValue(partItem)));
                    } else if (partItemName.equals("ETag")) {
                        partSummary.setETag(checkChildNotNullAndGetValue(partItem));
                    } else if(partItemName.equals("Size")) {
                        String size = checkChildNotNullAndGetValue(partItem);
                        if (size != null) {
                            partSummary.setSize(Integer.valueOf(size));
                        }
                    }
                }
                partEtagList.add(partSummary);
            }
        }
        result.setParts(partEtagList);
        return result;
    }

    private static CompleteMultipartUploadResult parseCompleteMultipartUploadResponseXML(InputStream in) throws ParserConfigurationException, IOException, SAXException {
        CompleteMultipartUploadResult result = new CompleteMultipartUploadResult();
        DocumentBuilder builder = domFactory.newDocumentBuilder();
        Document dom = builder.parse(in);
        Element element = dom.getDocumentElement();
        OSSLog.logD("[item] - " + element.getNodeName());

        NodeList list = element.getChildNodes();
        for (int i = 0; i < list.getLength(); i++) {
            Node item = list.item(i);
            String name = item.getNodeName();
            if (name == null) {
                continue;
            } else if (name.equalsIgnoreCase("Location")) {
                result.setLocation(checkChildNotNullAndGetValue(item));
            } else if (name.equalsIgnoreCase("Bucket")) {
                result.setBucketName(checkChildNotNullAndGetValue(item));
            } else if (name.equalsIgnoreCase("Key")) {
                result.setObjectKey(checkChildNotNullAndGetValue(item));
            } else if (name.equalsIgnoreCase("ETag")) {
                result.setETag(checkChildNotNullAndGetValue(item));
            }
        }

        return result;
    }

    private static InitiateMultipartUploadResult parseInitMultipartResponseXML(InputStream in)
            throws IOException, SAXException, ParserConfigurationException {

        InitiateMultipartUploadResult result = new InitiateMultipartUploadResult();

        DocumentBuilder builder = domFactory.newDocumentBuilder();
        Document dom = builder.parse(in);
        Element element = dom.getDocumentElement();
        OSSLog.logD("[item] - " + element.getNodeName());

        NodeList list = element.getChildNodes();
        for (int i = 0; i < list.getLength(); i++) {
            Node item = list.item(i);
            String name = item.getNodeName();
            if (name == null) {
                continue;
            } else if (name.equalsIgnoreCase("UploadId")) {
                result.setUploadId(checkChildNotNullAndGetValue(item));
            } else if (name.equalsIgnoreCase("Bucket")) {
                result.setBucketName(checkChildNotNullAndGetValue(item));
            } else if (name.equalsIgnoreCase("Key")) {
                result.setObjectKey(checkChildNotNullAndGetValue(item));
            }
        }
        return result;
    }

    /**
     * 解析XML中的Contents
     *
     * @param list
     * @return
     */
    private static OSSObjectSummary parseObjectSummaryXML(NodeList list) throws ParseException {
        OSSObjectSummary object = new OSSObjectSummary();
        for (int i = 0; i < list.getLength(); i++) {
            Node item = list.item(i);
            String name = item.getNodeName();

            if (name == null) {
                continue;
            } else if (name.equals("Key")) {
                object.setKey(checkChildNotNullAndGetValue(item));
            } else if (name.equals("LastModified")) {
                object.setLastModified(DateUtil.parseIso8601Date(checkChildNotNullAndGetValue(item)));
            } else if (name.equals("Size")) {
                String size = checkChildNotNullAndGetValue(item);
                if (size != null) {
                    object.setSize(Integer.valueOf(size));
                }
            } else if (name.equals("ETag")) {
                object.setETag(checkChildNotNullAndGetValue(item));
            } else if (name.equals("Type")) {
                object.setType(checkChildNotNullAndGetValue(item));
            } else if (name.equals("StorageClass")) {
                object.setStorageClass(checkChildNotNullAndGetValue(item));
            }
        }
        return object;
    }

    private static String parseCommonPrefixXML(NodeList list) {
        for (int i = 0; i < list.getLength(); i++) {
            Node item = list.item(i);
            String name = item.getNodeName();
            if (name == null) {
                continue;
            } else if (name.equals("Prefix")) {
                return checkChildNotNullAndGetValue(item);
            }
        }
        return "";
    }

    /**
     * 解析GetBucketACL请求的响应体
     * @param in
     * @return
     * @throws Exception
     */
    private static GetBucketACLResult parseGetBucketACLResponse(InputStream in)
            throws ParserConfigurationException, IOException, SAXException, ParseException {
        GetBucketACLResult result = new GetBucketACLResult();
        DocumentBuilder builder = domFactory.newDocumentBuilder();
        Document dom = builder.parse(in);
        Element element = dom.getDocumentElement();
        OSSLog.logD("[parseGetBucketACLResponse - " + element.getNodeName());
        NodeList list = element.getChildNodes();
        for (int i = 0; i < list.getLength(); i++) {
            Node item = list.item(i);
            String name = item.getNodeName();
            if (name == null) {
                continue;
            } else if (name.equals("Owner")) {
                NodeList ownerList = item.getChildNodes();
                for (int j = 0; j < ownerList.getLength(); j++) {
                    Node ownerItem = ownerList.item(j);
                    String ownerName = ownerItem.getNodeName();
                    if (ownerName == null) {
                        continue;
                    } else if (ownerName.equals("ID")) {
                        result.setBucketOwnerID(checkChildNotNullAndGetValue(ownerItem));
                    } else if (ownerName.equals("DisplayName")) {
                        result.setBucketOwner(checkChildNotNullAndGetValue(ownerItem));
                    }
                }
            } else if (name.equals("AccessControlList")) {
                NodeList aclList = item.getChildNodes();
                for (int k = 0; k < aclList.getLength(); k++) {
                    Node aclItem = aclList.item(k);
                    String aclName = aclItem.getNodeName();
                    if (aclName == null) {
                        continue;
                    } else if (aclName.equals("Grant")) {
                        result.setBucketACL(checkChildNotNullAndGetValue(aclItem));
                    }
                }
            }
        }
        return result;
    }

    /**
     * 解析listObjectInBucket请求的响应体
     *
     * @param in
     * @return
     * @throws Exception
     */
    private static ListObjectsResult parseObjectListResponse(InputStream in)
            throws ParserConfigurationException, IOException, SAXException, ParseException {
        ListObjectsResult result = new ListObjectsResult();
        DocumentBuilder builder = domFactory.newDocumentBuilder();
        Document dom = builder.parse(in);
        Element element = dom.getDocumentElement();
        OSSLog.logD("[parseObjectListResponse] - " + element.getNodeName());

        NodeList list = element.getChildNodes();
        for (int i = 0; i < list.getLength(); i++) {
            Node item = list.item(i);
            String name = item.getNodeName();
            if (name == null) {
                continue;
            } else if (name.equals("Name")) {
                result.setBucketName(checkChildNotNullAndGetValue(item));
            } else if (name.equals("Prefix")) {
                result.setPrefix(checkChildNotNullAndGetValue(item));
            } else if (name.equals("Marker")) {
                result.setMarker(checkChildNotNullAndGetValue(item));
            } else if (name.equals("Delimiter")) {
                result.setDelimiter(checkChildNotNullAndGetValue(item));
            } else if (name.equals("EncodingType")) {
                result.setEncodingType(checkChildNotNullAndGetValue(item));
            } else if (name.equals("MaxKeys")) {
                String maxKeys = checkChildNotNullAndGetValue(item);
                if (maxKeys != null) {
                    result.setMaxKeys(Integer.valueOf(maxKeys));
                }
            } else if (name.equals("NextMarker")) {
                result.setNextMarker(checkChildNotNullAndGetValue(item));
            } else if (name.equals("IsTruncated")) {
                String isTruncated = checkChildNotNullAndGetValue(item);
                if (isTruncated != null) {
                    result.setTruncated(Boolean.valueOf(isTruncated));
                }
            } else if (name.equals("Contents")) {
                if (item.getChildNodes() == null) {
                    continue;
                }
                result.getObjectSummaries().add(parseObjectSummaryXML(item.getChildNodes()));
            } else if (name.equals("CommonPrefixes")) {
                if (item.getChildNodes() == null) {
                    continue;
                }
                String prefix = parseCommonPrefixXML(item.getChildNodes());
                if (prefix != null) {
                    result.getCommonPrefixes().add(prefix);
                }
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
            throws IOException {

        try {
            ObjectMetadata objectMetadata = new ObjectMetadata();

            for (Iterator<String> it = headers.keySet().iterator(); it.hasNext();) {
                String key = it.next();

                if (key.indexOf(OSSHeaders.OSS_USER_METADATA_PREFIX) >= 0) {
                    objectMetadata.addUserMetadata(key, headers.get(key));
                } else if (key.equals(OSSHeaders.LAST_MODIFIED) || key.equals(OSSHeaders.DATE)) {
                    try {
                        objectMetadata.setHeader(key, DateUtil.parseRfc822Date(headers.get(key)));
                    } catch (ParseException pe) {
                        throw new IOException(pe.getMessage(), pe);
                    }
                } else if (key.equals(OSSHeaders.CONTENT_LENGTH)) {
                    Long value = Long.valueOf(headers.get(key));
                    objectMetadata.setHeader(key, value);
                } else if (key.equals(OSSHeaders.ETAG)) {
                    objectMetadata.setHeader(key, trimQuotes(headers.get(key)));
                } else {
                    objectMetadata.setHeader(key, headers.get(key) );
                }
            }

            return objectMetadata;
        } catch (Exception e) {
            throw new IOException(e.getMessage(), e);
        }
    }

    public static Map<String, String> parseResponseHeader(Response response) {
        Map<String, String> result = new HashMap<String, String>();
        Headers headers = response.headers();
        for (int i = 0; i < headers.size(); i++) {
            result.put(headers.name(i), headers.value(i));
        }
        return result;
    }

    public static ServiceException parseResponseErrorXML(Response response, boolean isHeadRequest)
            throws IOException {

        int statusCode = response.code();
        String requestId = response.header(OSSHeaders.OSS_HEADER_REQUEST_ID);
        String code = null;
        String message = null;
        String hostId = null;
        String errorMessage = null;

        if (!isHeadRequest) {
            try {
                errorMessage = response.body().string();
                DocumentBuilder builder = domFactory.newDocumentBuilder();
                InputSource is = new InputSource(new StringReader(errorMessage));
                Document dom = builder.parse(is);
                Element element = dom.getDocumentElement();

                NodeList list = element.getChildNodes();
                for (int i = 0; i < list.getLength(); i++) {
                    Node item = list.item(i);
                    String name = item.getNodeName();
                    if (name == null) continue;

                    if (name.equals("Code")) {
                        code = checkChildNotNullAndGetValue(item);
                    }
                    if (name.equals("Message")) {
                        message = checkChildNotNullAndGetValue(item);
                    }
                    if (name.equals("RequestId")) {
                        requestId = checkChildNotNullAndGetValue(item);
                    }
                    if (name.equals("HostId")) {
                        hostId = checkChildNotNullAndGetValue(item);
                    }
                }
                response.body().close();
            } catch (SAXException e) {
                e.printStackTrace();
            } catch (ParserConfigurationException e) {
                e.printStackTrace();
            }
        }
        return new ServiceException(statusCode, message, code, requestId, hostId, errorMessage);
    }

    /**
     * 检查xml单节点有子节点并取值
     * @param item
     */
    public static String checkChildNotNullAndGetValue(Node item) {
        if (item.getFirstChild() != null) {
            return item.getFirstChild().getNodeValue();
        }
        return null;
    }

    public static void safeCloseResponse(Response response) {
        try {
            response.body().close();
        } catch(Exception e) {
        }
    }
}
