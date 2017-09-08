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

    public static final class PutObjectResponseParser extends AbstractResponseParser<PutObjectResult> {

        @Override
        public PutObjectResult parseData(Response response,PutObjectResult result)
                throws IOException {
            result.setETag(trimQuotes(response.header(OSSHeaders.ETAG)));
            if (response.body().contentLength() > 0) {
                result.setServerCallbackReturnBody(response.body().string());
            }
            return result;
        }
    }

    public static final class AppendObjectResponseParser extends AbstractResponseParser<AppendObjectResult> {

        @Override
        public AppendObjectResult parseData(Response response,AppendObjectResult result) throws IOException {
            String nextPosition = response.header(OSSHeaders.OSS_NEXT_APPEND_POSITION);
            if (nextPosition != null) {
                result.setNextPosition(Long.valueOf(nextPosition));
            }
            result.setObjectCRC64(response.header(OSSHeaders.OSS_HASH_CRC64_ECMA));
            return result;
        }
    }

    public static final class HeadObjectResponseParser extends AbstractResponseParser<HeadObjectResult> {

        @Override
        public HeadObjectResult parseData(Response response,HeadObjectResult result) throws IOException {
            result.setMetadata(parseObjectMetadata(result.getResponseHeader()));
            return result;
        }
    }

    public static final class GetObjectResponseParser extends AbstractResponseParser<GetObjectResult> {

        @Override
        public GetObjectResult parseData(Response response,GetObjectResult result) throws IOException {
            result.setMetadata(parseObjectMetadata(result.getResponseHeader()));
            result.setContentLength(response.body().contentLength());
            result.setObjectContent(response.body().byteStream());
            return result;
        }

        @Override
        public boolean needCloseResponse() {
            // keep body stream open for reading content
            return false;
        }
    }

    public static final class CopyObjectResponseParser extends AbstractResponseParser<CopyObjectResult> {

        @Override
        public CopyObjectResult parseData(Response response,CopyObjectResult result) throws Exception {
            result = parseCopyObjectResponseXML(response.body().byteStream(),result);
            return result;
        }
    }

    public static final class CreateBucketResponseParser extends AbstractResponseParser<CreateBucketResult> {

        @Override
        public CreateBucketResult parseData(Response response,CreateBucketResult result) throws IOException {
            if(result.getResponseHeader().containsKey("Location")) {
                result.bucketLocation = result.getResponseHeader().get("Location");
            }
            return result;
        }
    }

    public static final class DeleteBucketResponseParser extends AbstractResponseParser<DeleteBucketResult> {

        @Override
        public DeleteBucketResult parseData(Response response,DeleteBucketResult result) throws IOException {
            return result;
        }
    }

    public static final class GetBucketACLResponseParser extends AbstractResponseParser<GetBucketACLResult> {

        @Override
        public GetBucketACLResult parseData(Response response,GetBucketACLResult result) throws Exception {
            result = parseGetBucketACLResponse(response.body().byteStream(),result);
            return result;
        }
    }


    public static final class DeleteObjectResponseParser extends AbstractResponseParser<DeleteObjectResult> {

        @Override
        public DeleteObjectResult parseData(Response response,DeleteObjectResult result) throws IOException {
            return result;
        }
    }

    public static final class ListObjectsResponseParser extends AbstractResponseParser<ListObjectsResult> {

        @Override
        public ListObjectsResult parseData(Response response,ListObjectsResult result) throws Exception {
            result = parseObjectListResponse(response.body().byteStream(),result);
            return result;
        }
    }

    public static final class InitMultipartResponseParser extends AbstractResponseParser<InitiateMultipartUploadResult> {

        @Override
        public InitiateMultipartUploadResult parseData(Response response,InitiateMultipartUploadResult result) throws Exception {
                return parseInitMultipartResponseXML(response.body().byteStream(),result);
        }
    }

    public static final class UploadPartResponseParser extends AbstractResponseParser<UploadPartResult> {

        @Override
        public UploadPartResult parseData(Response response,UploadPartResult result) throws IOException {
            result.setETag(trimQuotes(response.header(OSSHeaders.ETAG)));
            return result;
        }
    }

    public static final class AbortMultipartUploadResponseParser extends AbstractResponseParser<AbortMultipartUploadResult> {

        @Override
        public AbortMultipartUploadResult parseData(Response response,AbortMultipartUploadResult result) throws IOException {
            return result;
        }
    }

    public static final class CompleteMultipartUploadResponseParser extends AbstractResponseParser<CompleteMultipartUploadResult> {

        @Override
        public CompleteMultipartUploadResult parseData(Response response,CompleteMultipartUploadResult result) throws Exception {
            if (response.header(OSSHeaders.CONTENT_TYPE).equals("application/xml")) {
                result = parseCompleteMultipartUploadResponseXML(response.body().byteStream(),result);
            } else if (response.body() != null) {
                result.setServerCallbackReturnBody(response.body().string());
            }
            return result;
        }
    }

    public static final class ListPartsResponseParser extends AbstractResponseParser<ListPartsResult> {

        @Override
        public ListPartsResult parseData(Response response,ListPartsResult result) throws Exception {
            result = parseListPartsResponseXML(response.body().byteStream(),result);
            return result;
        }
    }

    private static CopyObjectResult parseCopyObjectResponseXML(InputStream in,CopyObjectResult result)
            throws ParseException, ParserConfigurationException, IOException, SAXException {

        DocumentBuilder builder = domFactory.newDocumentBuilder();
        Document dom = builder.parse(in);
        Element element = dom.getDocumentElement();
        OSSLog.logDebug("[item] - " + element.getNodeName());

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

    private static ListPartsResult parseListPartsResponseXML(InputStream in,ListPartsResult result)
            throws ParserConfigurationException, IOException, SAXException, ParseException {

//        ListPartsResult result = new ListPartsResult();
        DocumentBuilder builder = domFactory.newDocumentBuilder();
        Document dom = builder.parse(in);
        Element element = dom.getDocumentElement();
        OSSLog.logDebug("[parseObjectListResponse] - " + element.getNodeName());

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
            } else if (name.equals("StorageClass")){
                String storageClass = checkChildNotNullAndGetValue(item);
                if(storageClass != null){
                    result.setStorageClass(storageClass);
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

    private static CompleteMultipartUploadResult parseCompleteMultipartUploadResponseXML(InputStream in,CompleteMultipartUploadResult result) throws ParserConfigurationException, IOException, SAXException {
//        CompleteMultipartUploadResult result = new CompleteMultipartUploadResult();
        DocumentBuilder builder = domFactory.newDocumentBuilder();
        Document dom = builder.parse(in);
        Element element = dom.getDocumentElement();
        OSSLog.logDebug("[item] - " + element.getNodeName());

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

    private static InitiateMultipartUploadResult parseInitMultipartResponseXML(InputStream in,InitiateMultipartUploadResult result)
            throws IOException, SAXException, ParserConfigurationException {

//        InitiateMultipartUploadResult result = new InitiateMultipartUploadResult();

        DocumentBuilder builder = domFactory.newDocumentBuilder();
        Document dom = builder.parse(in);
        Element element = dom.getDocumentElement();
        OSSLog.logDebug("[item] - " + element.getNodeName());

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
    private static GetBucketACLResult parseGetBucketACLResponse(InputStream in,GetBucketACLResult result)
            throws ParserConfigurationException, IOException, SAXException, ParseException {
        DocumentBuilder builder = domFactory.newDocumentBuilder();
        Document dom = builder.parse(in);
        Element element = dom.getDocumentElement();
        OSSLog.logDebug("[parseGetBucketACLResponse - " + element.getNodeName());
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
    private static ListObjectsResult parseObjectListResponse(InputStream in,ListObjectsResult result)
            throws ParserConfigurationException, IOException, SAXException, ParseException {
        DocumentBuilder builder = domFactory.newDocumentBuilder();
        Document dom = builder.parse(in);
        Element element = dom.getDocumentElement();
        OSSLog.logDebug("[parseObjectListResponse] - " + element.getNodeName());
        result.clearCommonPrefixes();
        result.clearObjectSummaries();
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
                result.addObjectSummary(parseObjectSummaryXML(item.getChildNodes()));
            } else if (name.equals("CommonPrefixes")) {
                if (item.getChildNodes() == null) {
                    continue;
                }
                String prefix = parseCommonPrefixXML(item.getChildNodes());
                if (prefix != null) {
                    result.addCommonPrefix(prefix);
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

}
