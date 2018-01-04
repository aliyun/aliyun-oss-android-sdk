package com.alibaba.sdk.android.oss.internal;

import com.alibaba.sdk.android.oss.common.utils.OSSUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.CheckedInputStream;
import java.util.zip.Checksum;

/**
 * Created by jingdan on 2017/11/29.
 */

public class CheckCRC64DownloadInputStream extends CheckedInputStream {

    private long mTotalBytesRead;
    private long mTotalLength;
    private long mServerCRC64;
    private String mRequestId;
    private long mClientCRC64;

    /**
     * Constructs a new {@code CheckedInputStream} on {@code InputStream}
     * {@code is}. The checksum will be calculated using the algorithm
     * implemented by {@code csum}.
     * <p>
     * <p><strong>Warning:</strong> passing a null source creates an invalid
     * {@code CheckedInputStream}. All operations on such a stream will fail.
     *
     * @param is   the input stream to calculate checksum from.
     * @param csum
     */
    public CheckCRC64DownloadInputStream(InputStream is, Checksum csum, long total, long serverCRC64, String requestId) {
        super(is, csum);
        this.mTotalLength = total;
        this.mServerCRC64 = serverCRC64;
        this.mRequestId = requestId;
    }

    @Override
    public int read() throws IOException {
        int read = super.read();
        checkCRC64(read);
        return read;
    }

    @Override
    public int read(byte[] buffer, int byteOffset, int byteCount) throws IOException {
        int read = super.read(buffer, byteOffset, byteCount);
        checkCRC64(read);
        return read;
    }

    private void checkCRC64(int byteRead) throws IOException {
        mTotalBytesRead += byteRead;
        if (mTotalBytesRead >= mTotalLength) {
            this.mClientCRC64 = getChecksum().getValue();
            OSSUtils.checkChecksum(mClientCRC64, mServerCRC64, mRequestId);
        }
    }

    public long getClientCRC64() {
        return mClientCRC64;
    }
}
