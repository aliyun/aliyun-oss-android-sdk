package com.alibaba.sdk.android.mns.internal;

import com.alibaba.sdk.android.common.ServiceException;
import com.alibaba.sdk.android.mns.common.MNSConstants;
import com.alibaba.sdk.android.mns.common.MNSHeaders;
import com.alibaba.sdk.android.mns.model.deserialize.ChangeVisibilityDeserializer;
import com.alibaba.sdk.android.mns.model.deserialize.MessageDeserializer;
import com.alibaba.sdk.android.mns.model.deserialize.QueueArrayDeserializer;
import com.alibaba.sdk.android.mns.model.result.ChangeMessageVisibilityResult;
import com.alibaba.sdk.android.mns.model.result.CreateQueueResult;
import com.alibaba.sdk.android.mns.model.result.DeleteMessageResult;
import com.alibaba.sdk.android.mns.model.result.DeleteQueueResult;
import com.alibaba.sdk.android.mns.model.result.GetQueueAttributesResult;
import com.alibaba.sdk.android.mns.model.result.ListQueueResult;
import com.alibaba.sdk.android.mns.model.result.PeekMessageResult;
import com.alibaba.sdk.android.mns.model.result.ReceiveMessageResult;
import com.alibaba.sdk.android.mns.model.result.SendMessageResult;
import com.alibaba.sdk.android.mns.model.result.SetQueueAttributesResult;
import com.alibaba.sdk.android.mns.model.deserialize.ErrorMessageListDeserializer;
import com.alibaba.sdk.android.mns.model.deserialize.QueueMetaDeserializer;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Headers;
import okhttp3.Response;
/**
 * Created by pan.zengp on 2016/7/27.
 */
public class ResponseParsers {

    public static Map<String, String> parseResponseHeader(Response response) {
        Map<String, String> result = new HashMap<String, String>();
        Headers headers = response.headers();
        for (int i = 0; i < headers.size(); i++) {
            result.put(headers.name(i), headers.value(i));
        }
        return result;
    }

    public static void safeCloseResponse(Response response) {
        try {
            response.body().close();
        } catch(Exception e) {
        }
    }

    public static final class CreateQueueResponseParser implements ResponseParser<CreateQueueResult>{
        @Override
        public CreateQueueResult parse(Response response)
                throws IOException {
            try{
                CreateQueueResult result = new CreateQueueResult();
                result.setRequestId(response.header(MNSHeaders.MNS_HEADER_REQUEST_ID));
                result.setStatusCode(response.code());
                result.setResponseHeader(parseResponseHeader(response));
                result.setQueueLocation(result.getResponseHeader().get(MNSConstants.LOCATION));
                return result;
            } catch(Exception e)
            {
                throw new IOException(e.getMessage(), e);
            } finally {
                 safeCloseResponse(response);
            }
        }
    }

    public static final class DeleteQueueResponseParser implements ResponseParser<DeleteQueueResult>{
        @Override
        public DeleteQueueResult parse(Response response)
                throws IOException {
            try{
                DeleteQueueResult result = new DeleteQueueResult();
                result.setRequestId(response.header(MNSHeaders.MNS_HEADER_REQUEST_ID));
                result.setStatusCode(response.code());
                result.setResponseHeader(parseResponseHeader(response));
                return result;
            } catch(Exception e)
            {
                throw new IOException(e.getMessage(), e);
            } finally {
                 safeCloseResponse(response);
            }
        }
    }

    public static final class SetQueueAttributesResponseParser implements ResponseParser<SetQueueAttributesResult>{
        @Override
        public SetQueueAttributesResult parse(Response response)
                throws IOException {
            try{
                SetQueueAttributesResult result = new SetQueueAttributesResult();
                result.setRequestId(response.header(MNSHeaders.MNS_HEADER_REQUEST_ID));
                result.setStatusCode(response.code());
                result.setResponseHeader(parseResponseHeader(response));
                return result;
            } catch(Exception e)
            {
                throw new IOException(e.getMessage(), e);
            } finally {
                 safeCloseResponse(response);
            }
        }
    }

    public static final class GetQueueAttributesResponseParser implements ResponseParser<GetQueueAttributesResult>{
        @Override
        public GetQueueAttributesResult parse(Response response)
                throws IOException {
            try{
                GetQueueAttributesResult result = new GetQueueAttributesResult();
                result.setRequestId(response.header(MNSHeaders.MNS_HEADER_REQUEST_ID));
                result.setStatusCode(response.code());
                result.setResponseHeader(parseResponseHeader(response));
                QueueMetaDeserializer deserializer = new QueueMetaDeserializer();
                result.setQueueMeta(deserializer.deserialize(response));
                return result;
            } catch(Exception e)
            {
                throw new IOException(e.getMessage(), e);
            } finally {
                 safeCloseResponse(response);
            }
        }
    }

    public static final class ListQueueResponseParser implements ResponseParser<ListQueueResult>{
        @Override
        public ListQueueResult parse(Response response)
                throws IOException {
            try{
                ListQueueResult result = new ListQueueResult();
                result.setRequestId(response.header(MNSHeaders.MNS_HEADER_REQUEST_ID));
                result.setStatusCode(response.code());
                result.setResponseHeader(parseResponseHeader(response));
                QueueArrayDeserializer deserializer = new QueueArrayDeserializer();
                result.setQueueLists(deserializer.deserialize(response));
                return result;
            } catch(Exception e)
            {
                throw new IOException(e.getMessage(), e);
            } finally {
                 safeCloseResponse(response);
            }
        }
    }

    public static final class SendMessageResponseParser implements ResponseParser<SendMessageResult>{
        @Override
        public SendMessageResult parse(Response response)
                throws IOException {
            try{
                SendMessageResult result = new SendMessageResult();
                result.setRequestId(response.header(MNSHeaders.MNS_HEADER_REQUEST_ID));
                result.setStatusCode(response.code());
                result.setResponseHeader(parseResponseHeader(response));
                MessageDeserializer deserializer = new MessageDeserializer();
                result.setMessageResponse(deserializer.deserialize(response));
                return result;
            } catch(Exception e)
            {
                throw new IOException(e.getMessage(), e);
            } finally {
                 safeCloseResponse(response);
            }
        }
    }

    public static final class ReceiveMessageParser implements ResponseParser<ReceiveMessageResult>{
        @Override
        public ReceiveMessageResult parse(Response response)
                throws IOException {
            try{
                ReceiveMessageResult result = new ReceiveMessageResult();
                result.setRequestId(response.header(MNSHeaders.MNS_HEADER_REQUEST_ID));
                result.setStatusCode(response.code());
                result.setResponseHeader(parseResponseHeader(response));
                MessageDeserializer deserializer = new MessageDeserializer();
                result.setMessage(deserializer.deserialize(response));
                return result;
            } catch(Exception e)
            {
                throw new IOException(e.getMessage(), e);
            } finally {
                 safeCloseResponse(response);
            }
        }
    }

    public static final class DeleteMessageParser implements ResponseParser<DeleteMessageResult>{
        @Override
        public DeleteMessageResult parse(Response response)
                throws IOException {
            try{
                DeleteMessageResult result = new DeleteMessageResult();
                result.setRequestId(response.header(MNSHeaders.MNS_HEADER_REQUEST_ID));
                result.setStatusCode(response.code());
                result.setResponseHeader(parseResponseHeader(response));
                return result;
            } catch(Exception e)
            {
                throw new IOException(e.getMessage(), e);
            } finally {
                 safeCloseResponse(response);
            }
        }
    }

    public static final class ChangeMessageVisibilityParser implements ResponseParser<ChangeMessageVisibilityResult>{
        @Override
        public ChangeMessageVisibilityResult parse(Response response)
                throws IOException {
            try{
                ChangeMessageVisibilityResult result = new ChangeMessageVisibilityResult();
                result.setRequestId(response.header(MNSHeaders.MNS_HEADER_REQUEST_ID));
                result.setStatusCode(response.code());
                result.setResponseHeader(parseResponseHeader(response));
                ChangeVisibilityDeserializer deserializer = new ChangeVisibilityDeserializer();
                result.setChangeVisibleResponse(deserializer.deserialize(response));
                return result;
            } catch(Exception e)
            {
                throw new IOException(e.getMessage(), e);
            } finally {
                 safeCloseResponse(response);
            }
        }
    }

    public static final class PeekMessageParser implements ResponseParser<PeekMessageResult>{
        @Override
        public PeekMessageResult parse(Response response)
                throws IOException {
            try{
                PeekMessageResult result = new PeekMessageResult();
                result.setRequestId(response.header(MNSHeaders.MNS_HEADER_REQUEST_ID));
                result.setStatusCode(response.code());
                result.setResponseHeader(parseResponseHeader(response));
                MessageDeserializer deserializer = new MessageDeserializer();
                result.setMessage(deserializer.deserialize(response));
                return result;
            } catch(Exception e)
            {
                throw new IOException(e.getMessage(), e);
            } finally {
                 safeCloseResponse(response);
            }
        }
    }

    public static ServiceException parseResponseErrorXML(Response response)
            throws IOException {
        try {
            ErrorMessageListDeserializer deserializer = new ErrorMessageListDeserializer();
            return deserializer.deserialize(response);
        } catch(Exception e) {
            throw new IOException(e.getMessage(), e);
        } finally {
             safeCloseResponse(response);
        }
    }
}
