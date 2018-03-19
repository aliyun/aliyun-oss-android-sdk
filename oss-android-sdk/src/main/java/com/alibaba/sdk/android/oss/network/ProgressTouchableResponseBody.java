package com.alibaba.sdk.android.oss.network;

import com.alibaba.sdk.android.oss.callback.OSSProgressCallback;
import com.alibaba.sdk.android.oss.model.OSSRequest;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import okio.ForwardingSource;
import okio.Okio;
import okio.Source;

/**
 * Created by jingdan on 2017/9/12.
 * response progress
 */

public class ProgressTouchableResponseBody<T extends OSSRequest> extends ResponseBody {

    private final ResponseBody mResponseBody;
    private OSSProgressCallback mProgressListener;
    private BufferedSource mBufferedSource;
    private T request;

    public ProgressTouchableResponseBody(ResponseBody responseBody, ExecutionContext context) {
        this.mResponseBody = responseBody;
        this.mProgressListener = context.getProgressCallback();
        this.request = (T) context.getRequest();
    }

    @Override
    public MediaType contentType() {
        return mResponseBody.contentType();
    }

    @Override
    public long contentLength() {
        return mResponseBody.contentLength();
    }

    @Override
    public BufferedSource source() {
        if (mBufferedSource == null) {
            mBufferedSource = Okio.buffer(source(mResponseBody.source()));
        }
        return mBufferedSource;
    }

    private Source source(Source source) {
        return new ForwardingSource(source) {
            private long totalBytesRead = 0L;

            @Override
            public long read(Buffer sink, long byteCount) throws IOException {
                long bytesRead = super.read(sink, byteCount);
                totalBytesRead += bytesRead != -1 ? bytesRead : 0;
                //callback
                if (mProgressListener != null && bytesRead != -1 && totalBytesRead != 0) {
                    mProgressListener.onProgress(request, totalBytesRead, mResponseBody.contentLength());
                }
                return bytesRead;
            }
        };
    }
}
