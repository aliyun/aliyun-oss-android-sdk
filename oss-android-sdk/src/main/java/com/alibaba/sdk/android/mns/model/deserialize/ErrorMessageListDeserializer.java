package com.alibaba.sdk.android.mns.model.deserialize;

import com.alibaba.sdk.android.common.ServiceException;
import com.alibaba.sdk.android.mns.common.MNSConstants;
import com.alibaba.sdk.android.mns.common.MNSHeaders;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;

import okhttp3.Response;

/**
 * Created by pan.zengp on 2016/7/27.
 */
public class ErrorMessageListDeserializer extends XMLDeserializer<ServiceException> {
    @Override
    public ServiceException deserialize(Response response) throws Exception{

//       byte[] bytes = new byte[1024];
//       while(stream.read(bytes, 0, stream.available())>0){
//       System.out.println(new String(bytes));
//       }
        int statusCode = response.code();
        String requestId = response.header(MNSHeaders.MNS_HEADER_REQUEST_ID);
        String code = null;
        String message = null;
        String hostId = null;
        String errorMessage = null;
        try{
            errorMessage = response.body().string();
            DocumentBuilder builder = getDocumentBuilder();
            InputSource is = new InputSource(new StringReader(errorMessage));
            Document doc = builder.parse(is);

            Element root = doc.getDocumentElement();

            if (root != null) {
                String rootName = root.getNodeName();

                if (rootName.equals(MNSConstants.ERROR_TAG)) {
                    code = safeGetElementContent(root, MNSConstants.ERROR_CODE_TAG, "");
                    message = safeGetElementContent(root, MNSConstants.ERROR_MESSAGE_TAG, "");
                    requestId = safeGetElementContent(root, MNSConstants.ERROR_REQUEST_ID_TAG, "");
                    hostId = safeGetElementContent(root, MNSConstants.ERROR_HOST_ID_TAG, "");

                    return new ServiceException(statusCode, message, code, requestId, hostId, errorMessage);
                }
            }
        } catch(Exception e)
        {
            e.printStackTrace();
        }
        return new ServiceException(statusCode, message, code, requestId, hostId, errorMessage);
    }
}
