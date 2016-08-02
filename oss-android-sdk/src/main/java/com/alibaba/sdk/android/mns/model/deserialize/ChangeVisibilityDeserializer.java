package com.alibaba.sdk.android.mns.model.deserialize;

import com.alibaba.sdk.android.mns.common.MNSConstants;
import com.alibaba.sdk.android.mns.model.Message;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import java.io.StringReader;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;

import okhttp3.Response;

/**
 * Created by pan.zengp on 2016/8/1.
 */
public class ChangeVisibilityDeserializer extends XMLDeserializer<Message> {
    @Override
    public Message deserialize(Response response) throws Exception {
        Message message = new Message();
        try{
            String xmlMessage = response.body().string();
            DocumentBuilder builder = getDocumentBuilder();
            InputSource is = new InputSource(new StringReader(xmlMessage));
            Document doc = builder.parse(is);

            Element root = doc.getDocumentElement();

            if (root != null) {
                String rootName = root.getNodeName();

                if (rootName.equals(MNSConstants.CHANGE_VISIBILITY_TAG)) {
                    String receiptHandle = safeGetElementContent(root, MNSConstants.RECEIPT_HANDLE_TAG, null);
                    if (receiptHandle != null)
                        message.setReceiptHandle(receiptHandle);

                    String nextVisibleTime = safeGetElementContent(root,
                            MNSConstants.NEXT_VISIBLE_TIME_TAG, null);
                    if (nextVisibleTime != null)
                        message.setNextVisibleTime(new Date(Long.parseLong(nextVisibleTime)));

                    return message;
                }
            }
        } catch(Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }
}
