package com.alibaba.sdk.android.oss.network;

import com.alibaba.sdk.android.oss.callback.OSSProgressCallback;
import com.alibaba.sdk.android.oss.model.OSSRequest;

import java.io.IOException;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.BufferedSink;
import okio.Okio;
import okio.Source;

/**
 * Created by jingdan on 2017/9/12.
 */

public class ProgressTouchableRequestBody<T extends OSSRequest> extends RequestBody {
    private static final int SEGMENT_SIZE = 2048; // okio.Segment.SIZE

    private InputStream inputStream;
    private String contentType;
    private long contentLength;
    private OSSProgressCallback callback;
    private T request;

    public ProgressTouchableRequestBody(InputStream input, long contentLength, String contentType, ExecutionContext context) {
        this.inputStream = input;
        this.contentType = contentType;
        this.contentLength = contentLength;
        this.callback = context.getProgressCallback();
        this.request = (T) context.getRequest();
    }

    @Override
    public MediaType contentType() {
        return MediaType.parse(this.contentType);
    }

    @Override
    public long contentLength() throws IOException {
        return this.contentLength;
    }

    @Override
    public void writeTo(BufferedSink sink) throws IOException {
        Source source = Okio.source(this.inputStream);
        long total = 0;
        long read, toRead, remain;

        while (total < contentLength) {
            remain = contentLength - total;
            toRead = Math.min(remain, SEGMENT_SIZE);

            read = source.read(sink.buffer(), toRead);
            if (read == -1) {
                break;
            }

            total += read;
            sink.flush();

            if (callback != null && total != 0) {
                callback.onProgress(request, total, contentLength);
            }
        }
        if (source != null) {
            source.close();
        }
    }
}
