package com.alibaba.sdk.android.mns.model.deserialize;

import com.alibaba.sdk.android.mns.common.MNSConstants;
import com.alibaba.sdk.android.mns.model.QueueMeta;

import org.w3c.dom.Element;

import java.util.Date;

/**
 * Created by pan.zengp on 2016/7/30.
 */
public abstract class AbstractQueueMetaDeserializer<T> extends
        XMLDeserializer<T> {

    public AbstractQueueMetaDeserializer() {
        super();
    }

    protected QueueMeta parseQueueMeta(Element root) {
        QueueMeta meta = new QueueMeta();

        String queueName = safeGetElementContent(root, MNSConstants.QUEUE_NAME_TAG, null);
        meta.setQueueName(queueName);

        String delaySeconds = safeGetElementContent(root, MNSConstants.DELAY_SECONDS_TAG,
                "0");
        meta.setDelaySeconds(Long.parseLong(delaySeconds));

        String maxMessageSize = safeGetElementContent(root,
                MNSConstants.MAX_MESSAGE_SIZE_TAG, "0");
        meta.setMaxMessageSize(Long.parseLong(maxMessageSize));

        String messageRetentionPeriod = safeGetElementContent(root,
                MNSConstants.MESSAGE_RETENTION_PERIOD_TAG, "0");
        meta.setMessageRetentionPeriod(Long.parseLong(messageRetentionPeriod));

        String visibiltyTimeout = safeGetElementContent(root,
                MNSConstants.VISIBILITY_TIMEOUT, "0");
        meta.setVisibilityTimeout(Long.parseLong(visibiltyTimeout));

        String createTime = safeGetElementContent(root, MNSConstants.CREATE_TIME_TAG, "0");
        meta.setCreateTime(new Date(Long.parseLong(createTime) * 1000));

        String lastModifyTime = safeGetElementContent(root, MNSConstants.LASTMODIFYTIME_TAG,
                "0");
        meta.setLastModifyTime(new Date(Long.parseLong(lastModifyTime) * 1000));

        String waitSeconds = safeGetElementContent(root, MNSConstants.POLLING_WAITSECONDS_TAG,
                "0");
        meta.setPollingWaitSeconds(Integer.parseInt(waitSeconds));

        String activeMessages = safeGetElementContent(root,
                MNSConstants.ACTIVE_MESSAGES_TAG, "0");
        meta.setActiveMessages(Long.parseLong(activeMessages));

        String inactiveMessages = safeGetElementContent(root,
                MNSConstants.INACTIVE_MESSAGES_TAG, "0");
        meta.setInactiveMessages(Long.parseLong(inactiveMessages));

        String delayMessages = safeGetElementContent(root, MNSConstants.DELAY_MESSAGES_TAG,
                "0");
        meta.setDelayMessages(Long.parseLong(delayMessages));

        String queueURL = safeGetElementContent(root, MNSConstants.QUEUE_URL_TAG, null);
        meta.setQueueURL(queueURL);

        String loggingEnabled = safeGetElementContent(root, MNSConstants.LOGGING_ENABLED_TAG,
                "false");
        meta.setLoggingEnabled(Boolean.parseBoolean(loggingEnabled));

        return meta;
    }

}
