package com.alibaba.sdk.android.mns.model.deserialize;

import com.alibaba.sdk.android.common.ServiceException;
import com.alibaba.sdk.android.mns.common.MNSConstants;
import com.alibaba.sdk.android.mns.model.PagingListResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;

import okhttp3.Response;

/**
 * Created by pan.zengp on 2016/7/30.
 */
public class QueueArrayDeserializer extends AbstractQueueMetaDeserializer<PagingListResult<String>> {

    @Override
    public PagingListResult<String> deserialize(Response response) throws Exception{
        try {
            String responseBody = response.body().string();
            DocumentBuilder builder = getDocumentBuilder();
            InputSource is = new InputSource(new StringReader(responseBody));
            Document doc = builder.parse(is);

            return parseQueueList(doc);
        } catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public PagingListResult<String> parseQueueList(Document doc) {
        NodeList list = doc.getElementsByTagName(MNSConstants.QUEUE_TAG);

        List<String> queues = new ArrayList<String>();

        for (int i = 0; i < list.getLength(); i++) {
            Element e = (Element) list.item(i);
            String tmpQueue = safeGetElementContent(e, MNSConstants.QUEUE_URL_TAG, null);
            if (tmpQueue != null) {
                queues.add(tmpQueue);
            }
        }
        PagingListResult<String> result = null;
        if (queues.size() > 0) {
            result = new PagingListResult<String>();
            list = doc.getElementsByTagName(MNSConstants.NEXT_MARKER_TAG);
            if (list.getLength() > 0) {
                result.setMarker(list.item(0).getTextContent());
            }
            result.setResult(queues);
        }
        return result;
    }
}
