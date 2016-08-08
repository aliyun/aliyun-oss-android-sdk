package com.alibaba.sdk.android.mns.common;

/**
 * Created by pan.zengp on 2016/7/4.
 */
public class MNSConstants {
    public static final int DEFAULT_RETRY_COUNT = 2;
    public static final int DEFAULT_BASE_THREAD_POOL_SIZE = 5;

    public static final String DEFAULT_CHARSET_NAME = "utf-8";
    public static final String DEFAULT_XML_ENCODING = "utf-8";
    public static final String DEFAULT_CONTENT_TYPE = "text/xml;charset=UTF-8";

    public static final String DEFAULT_XML_NAMESPACE = "http://mns.aliyuncs.com/doc/v1";

    public static final String QUEUE_PREFIX = "queues/";
    public static final String TPOIC_PREFIX = "topics/";

    public static final String ACCOUNT_TAG = "Account";
    public static final String QUEUE_TAG = "Queue";
    public static final String TOPIC_TAG = "Topic";
    public static final String QUEUE_NAME_TAG = "QueueName";
    public static final String TOPIC_NAME_TAG = "TopicName";
    public static final String SUBSCRIPTION_TAG = "Subscription";
    public static final String DELAY_SECONDS_TAG = "DelaySeconds";
    public static final String MAX_MESSAGE_SIZE_TAG = "MaximumMessageSize";
    public static final String MESSAGE_RETENTION_PERIOD_TAG = "MessageRetentionPeriod";
    public static final String VISIBILITY_TIMEOUT = "VisibilityTimeout";
    public static final String ACTIVE_MESSAGES_TAG = "ActiveMessages";
    public static final String INACTIVE_MESSAGES_TAG = "InactiveMessages";
    public static final String DELAY_MESSAGES_TAG = "DelayMessages";
    public static final String LASTMODIFYTIME_TAG = "LastModifyTime";
    public static final String CREATE_TIME_TAG = "CreateTime";
    public static final String POLLING_WAITSECONDS_TAG = "PollingWaitSeconds";
    public static final String MESSAGE_COUNT_TAG = "MessageCount";
    public static final String LOGGING_BUCKET_TAG = "LoggingBucket";
    public static final String LOGGING_ENABLED_TAG = "LoggingEnabled";

    public static final String QUEUE_URL_TAG = "QueueURL";
    public static final String NEXT_MARKER_TAG = "NextMarker";
    public static final String TOPIC_URL_TAG = "TopicURL";

    public static final String MESSAGE_LIST_TAG = "Messages";
    public static final String MESSAGE_TAG = "Message";
    public static final String PRIORITY_TAG = "Priority";
    public static final String MESSAGE_ID_TAG = "MessageId";
    public static final String CHANGE_VISIBILITY_TAG = "ChangeVisibility";

    public static final String ENDPOINT_TAG = "Endpoint";
    public static final String NOTIFY_STRATEGY_TAG = "NotifyStrategy";
    public static final String SUBSCRIPTION_NAME_TAG = "SubscriptionName";
    public static final String TOPIC_OWNER_TAG = "TopicOwner";
    public static final String SUBSCRIPTION_STATUS = "State";
    public static final String NOTIFY_CONTENT_FORMAT_TAG = "NotifyContentFormat";
    public static final String SUBSCRIPTION_URL_TAG = "SubscriptionURL";
    public static final String FILTER_TAG_TAG = "FilterTag";


    public static final String RECEIPT_HANDLE_LIST_TAG = "ReceiptHandles";
    public static final String RECEIPT_HANDLE_TAG = "ReceiptHandle";
    public static final String MESSAGE_BODY_TAG = "MessageBody";
    public static final String MESSAGE_BODY_MD5_TAG = "MessageBodyMD5";
    public static final String ENQUEUE_TIME_TAG = "EnqueueTime";
    public static final String NEXT_VISIBLE_TIME_TAG = "NextVisibleTime";
    public static final String FIRST_DEQUEUE_TIME_TAG = "FirstDequeueTime";
    public static final String DEQUEUE_COUNT_TAG = "DequeueCount";
    public static final String MESSAGE_ATTRIBUTES_TAG = "MessageAttributes";
    public static final String DIRECT_MAIL_TAG = "DirectMail";
    public static final String MESSAGE_TAG_TAG = "MessageTag";

    public static final String ERROR_LIST_TAG = "Errors";
    public static final String ERROR_TAG = "Error";
    public static final String ERROR_CODE_TAG = "Code";
    public static final String ERROR_MESSAGE_TAG = "Message";
    public static final String ERROR_REQUEST_ID_TAG = "RequestId";
    public static final String ERROR_HOST_ID_TAG = "HostId";
    public static final String MESSAGE_ERRORCODE_TAG = "ErrorCode";
    public static final String MESSAGE_ERRORMESSAGE_TAG = "ErrorMessage";

    public static final String ACCOUNT_ID_TAG = "AccountId";

    public static final String PARAM_WAITSECONDS = "waitseconds";


    public static final String SUBSRIPTION = "subscriptions";

    public static final Long MAX_MESSAGE_SIZE = 65536L;
    public static final Long DEFAULT_MESSAGE_RETENTION_PERIOD = 86400L;
    public static final Long MAX_MESSAGE_RETENTION_PERIOD = 86400L;
    public static final Long MIN_MESSAGE_RETENTION_PERIOD = 60L;

    public static final String DEFAULT_NOTIFY_CONTENT_TYPE = "XML";

    public static final String LOCATION = "Location";
    public static final String LOCATION_MESSAGES = "messages";

    public static final String X_HEADER_MNS_API_VERSION = "x-mns-version";
    public static final String X_HEADER_MNS_API_VERSION_VALUE = "2015-06-06";


    public static enum MNSType {
        QUEUE,
        MESSAGE
    }
}


