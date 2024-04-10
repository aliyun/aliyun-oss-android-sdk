package com.alibaba.sdk.android.oss.network;

import android.net.Uri;
import android.os.ParcelFileDescriptor;

import com.alibaba.sdk.android.oss.callback.OSSProgressCallback;
import com.alibaba.sdk.android.oss.common.utils.CRC64;
import com.alibaba.sdk.android.oss.model.OSSRequest;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.CheckedInputStream;

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

    private ExecutionContext context;

    private byte[] content;
    private String filePath;
    private Uri fileUri;

    private boolean isCheckCRC64 = false;

    public ProgressTouchableRequestBody(byte[] content, String contentType, ExecutionContext context, boolean checkCRC64) {
        this.content = content;
        this.contentType = contentType;
        this.contentLength = content.length;
        this.callback = context.getProgressCallback();
        this.request = (T) context.getRequest();
        this.context = context;
        this.isCheckCRC64 = checkCRC64;
    }

    public ProgressTouchableRequestBody(String filePath, String contentType, ExecutionContext context, boolean checkCRC64) {
        this.filePath = filePath;
        File file = new File(filePath);
        this.contentType = contentType;
        this.contentLength = file.length();
        this.callback = context.getProgressCallback();
        this.request = (T) context.getRequest();
        this.context = context;
        this.isCheckCRC64 = checkCRC64;
    }

    public ProgressTouchableRequestBody(Uri fileUri, String contentType, ExecutionContext context, boolean checkCRC64) throws IOException {
        ParcelFileDescriptor parcelFileDescriptor = null;
        try {
            parcelFileDescriptor = context.getApplicationContext().getContentResolver().openFileDescriptor(fileUri, "r");
            this.contentLength = parcelFileDescriptor.getStatSize();
        } finally {
            if (parcelFileDescriptor != null) {
                parcelFileDescriptor.close();
            }
        }
        this.fileUri = fileUri;
        this.contentType = contentType;
        this.callback = context.getProgressCallback();
        this.request = (T) context.getRequest();
        this.context = context;
        this.isCheckCRC64 = checkCRC64;
    }

    public ProgressTouchableRequestBody(InputStream input, long contentLength, String contentType, ExecutionContext context, boolean checkCRC64) {
        this.inputStream = input;
        this.contentType = contentType;
        this.contentLength = contentLength;
        this.callback = context.getProgressCallback();
        this.request = (T) context.getRequest();
        this.isCheckCRC64 = checkCRC64;
    }

    @Deprecated
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
        InputStream inputStream = null;
        if (content != null) {
            inputStream = new ByteArrayInputStream(content);
        } else if (filePath != null) {
            File file = new File(filePath);
            inputStream = new FileInputStream(file);
        } else if (fileUri != null) {
            ParcelFileDescriptor parcelFileDescriptor = null;
            inputStream = context.getApplicationContext().getContentResolver().openInputStream(fileUri);
        } else if (this.inputStream != null) {
            inputStream = this.inputStream;
        }
        if (inputStream == null) {
            return;
        }
        if (isCheckCRC64) {
            inputStream = new CheckedInputStream(inputStream, new CRC64());
        }
        Source source = Okio.source(inputStream);
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
