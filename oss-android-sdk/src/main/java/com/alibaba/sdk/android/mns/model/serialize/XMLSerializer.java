package com.alibaba.sdk.android.mns.model.serialize;

/**
 * Created by pan.zengp on 2016/7/27.
 */
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public abstract class XMLSerializer<T> extends BaseXMLSerializer<T> implements Serializer<T> {

    public Element safeCreateContentElement(Document doc, String tagName,
                                            Object value, String defaultValue) {
        if (value == null && defaultValue == null) {
            return null;
        }

        Element node = doc.createElement(tagName);
        if (value != null) {
            node.setTextContent(value.toString());
        } else {
            node.setTextContent(defaultValue);
        }
        return node;
    }

    public Element safeCreateBooleanContentElement(Document doc, String tagName,
                                            Integer value, String defaultValue) {
        if (value == null && defaultValue == null) {
            return null;
        }

        Element node = doc.createElement(tagName);
        if (value != null) {
            if (value > 0) {
                node.setTextContent("true");
            }
            else {
                node.setTextContent("false");
            }
        } else {
            node.setTextContent(defaultValue);
        }
        return node;
    }
}
