package com.alibaba.sdk.android.mns.model.deserialize;

import com.alibaba.sdk.android.common.ServiceException;
import com.alibaba.sdk.android.mns.common.MNSConstants;
import com.alibaba.sdk.android.mns.common.MNSHeaders;
import com.alibaba.sdk.android.mns.model.Message;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import java.io.StringReader;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;

import okhttp3.Response;

/**
 * Created by pan.zengp on 2016/7/31.
 */
public class MessageDeserializer extends XMLDeserializer<Message> {
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

                if (rootName.equals(MNSConstants.MESSAGE_TAG)) {
                    String messageId = safeGetElementContent(root, MNSConstants.MESSAGE_ID_TAG, null);
                    if (messageId != null)
                        message.setMessageId(messageId);

                    String messageBodyMd5 = safeGetElementContent(root, MNSConstants.MESSAGE_BODY_MD5_TAG, null);
                    if (messageBodyMd5 != null)
                        message.setMessageBodyMd5(messageBodyMd5);

                    String receiptHandle = safeGetElementContent(root, MNSConstants.RECEIPT_HANDLE_TAG, null);
                    if (receiptHandle != null)
                        message.setReceiptHandle(receiptHandle);

                    String messageBody = safeGetElementContent(root, MNSConstants.MESSAGE_BODY_TAG, null);
                    if (messageBody != null)
                        message.setMessageBody(messageBody);

                    String enqueTime = safeGetElementContent(root, MNSConstants.ENQUEUE_TIME_TAG, null);
                    if (enqueTime != null)
                        message.setEnqueueTime(new Date(Long.parseLong(enqueTime)));

                    String nextVisibleTime = safeGetElementContent(root,
                            MNSConstants.NEXT_VISIBLE_TIME_TAG, null);
                    if (nextVisibleTime != null)
                        message.setNextVisibleTime(new Date(Long.parseLong(nextVisibleTime)));

                    String firstDequeueTime = safeGetElementContent(root,
                            MNSConstants.FIRST_DEQUEUE_TIME_TAG, null);
                    if (firstDequeueTime != null)
                        message.setFirstDequeueTime(new Date(
                                Long.parseLong(firstDequeueTime)));

                    String dequeueCount = safeGetElementContent(root, MNSConstants.DEQUEUE_COUNT_TAG,
                            null);
                    if (dequeueCount != null)
                        message.setDequeueCount(Integer.parseInt(dequeueCount));

                    String priority = safeGetElementContent(root, MNSConstants.PRIORITY_TAG, null);
                    if (priority != null) {
                        message.setPriority(Integer.parseInt(priority));
                    }

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