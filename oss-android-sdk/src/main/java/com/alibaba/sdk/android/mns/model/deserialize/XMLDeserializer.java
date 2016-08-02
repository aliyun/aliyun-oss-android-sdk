package com.alibaba.sdk.android.mns.model.deserialize;

/**
 * Created by pan.zengp on 2016/7/27.
 */
import com.alibaba.sdk.android.mns.model.deserialize.Deserializer;
import com.alibaba.sdk.android.mns.model.serialize.BaseXMLSerializer;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public abstract class XMLDeserializer<T> extends BaseXMLSerializer<T> implements Deserializer<T> {

    public String safeGetElementContent(Element root, String tagName,
                                        String defualValue) {
        NodeList nodes = root.getElementsByTagName(tagName);
        if (nodes != null) {
            Node node = nodes.item(0);
            if (node == null) {
                return defualValue;
            } else {
                return node.getTextContent();
            }
        }
        return defualValue;
    }

}
