package com.alibaba.sdk.android.mns.model.serialize;

import com.alibaba.sdk.android.mns.common.MNSConstants;
import com.alibaba.sdk.android.mns.model.Message;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Created by pan.zengp on 2016/7/31.
 */
public class MessageSerializer extends XMLSerializer<Message> {
    @Override
    public String serialize(Message obj, String encoding)
            throws Exception {
        Document doc = getDocumentBuilder().newDocument();

        Element root = doc.createElementNS(MNSConstants.DEFAULT_XML_NAMESPACE, MNSConstants.MESSAGE_TAG);
        doc.appendChild(root);

        Element node = safeCreateContentElement(doc, MNSConstants.MESSAGE_BODY_TAG,
                obj.getMessageBody(), null);
        if (node != null) {
            root.appendChild(node);
        }

        node = safeCreateContentElement(doc, MNSConstants.DELAY_SECONDS_TAG,
                obj.getDelaySeconds(), null);
        if (node != null) {
            root.appendChild(node);
        }

        node = safeCreateContentElement(doc, MNSConstants.PRIORITY_TAG,
                obj.getPriority(), null);
        if (node != null) {
            root.appendChild(node);
        }

        return XmlUtil.xmlNodeToString(doc, encoding);
    }
}
