package com.alibaba.sdk.android.mns.model.serialize;

/**
 * Created by pan.zengp on 2016/7/27.
 */
import org.w3c.dom.Node;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.OutputStream;
import java.io.StringWriter;

public class XmlUtil {
    private static TransformerFactory transFactory = TransformerFactory.newInstance();


    public static void output(Node node, String encoding,
                              OutputStream outputStream) throws TransformerException {
        Transformer transformer = transFactory.newTransformer();
        transformer.setOutputProperty("encoding", encoding);

        DOMSource source = new DOMSource();
        source.setNode(node);

        StreamResult result = new StreamResult();
        result.setOutputStream(outputStream);

        transformer.transform(source, result);
    }

    public static String xmlNodeToString(Node node, String encoding)
            throws TransformerException {
        Transformer transformer = transFactory.newTransformer();
        transformer.setOutputProperty("encoding", encoding);
        StringWriter strWtr = new StringWriter();

        DOMSource source = new DOMSource();
        source.setNode(node);
        StreamResult result = new StreamResult(strWtr);
        transformer.transform(source, result);
        return strWtr.toString();

    }
}
