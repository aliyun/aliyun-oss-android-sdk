package com.alibaba.sdk.android.mns.model.deserialize;

import com.alibaba.sdk.android.mns.model.QueueMeta;
import com.alibaba.sdk.android.mns.model.deserialize.AbstractQueueMetaDeserializer;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;

import okhttp3.Response;

/**
 * Created by pan.zengp on 2016/7/30.
 */
public class QueueMetaDeserializer extends AbstractQueueMetaDeserializer<QueueMeta> {

    @Override
    public QueueMeta deserialize(Response response) throws Exception {
        try {
            String responseBody = response.body().string();
            DocumentBuilder builder = getDocumentBuilder();
            InputSource is = new InputSource(new StringReader(responseBody));
            Document doc = builder.parse(is);

            Element root = doc.getDocumentElement();

            return parseQueueMeta(root);
        } catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

}
