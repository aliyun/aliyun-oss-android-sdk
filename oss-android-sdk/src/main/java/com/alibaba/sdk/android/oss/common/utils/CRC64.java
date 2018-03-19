package com.alibaba.sdk.android.oss.common.utils;

import java.util.zip.Checksum;

/**
 * CRC-64 implementation with ability to combine checksums calculated over different blocks of data.
 * Standard ECMA-182, http://www.ecma-international.org/publications/standards/Ecma-182.htm
 */
public class CRC64 implements Checksum {

    private final static long POLY = (long) 0xc96c5795d7870f42L; // ECMA-182

    /* CRC64 calculation table. */
    private final static long[][] table;
    // dimension of GF(2) vectors (length of CRC)
    private static final int GF2_DIM = 64;

    static {
        table = new long[8][256];

        for (int n = 0; n < 256; n++) {
            long crc = n;
            for (int k = 0; k < 8; k++) {
                if ((crc & 1) == 1) {
                    crc = (crc >>> 1) ^ POLY;
                } else {
                    crc = (crc >>> 1);
                }
            }
            table[0][n] = crc;
        }

        /* generate nested CRC table for future slice-by-8 lookup */
        for (int n = 0; n < 256; n++) {
            long crc = table[0][n];
            for (int k = 1; k < 8; k++) {
                crc = table[0][(int) (crc & 0xff)] ^ (crc >>> 8);
                table[k][n] = crc;
            }
        }
    }

    /* Current CRC value. */
    private long value;

    /**
     * Initialize with a value of zero.
     */
    public CRC64() {
        this.value = 0;
    }

    private static long gf2MatrixTimes(long[] mat, long vec) {
        long sum = 0;
        int idx = 0;
        while (vec != 0) {
            if ((vec & 1) == 1)
                sum ^= mat[idx];
            vec >>>= 1;
            idx++;
        }
        return sum;
    }

    private static void gf2MatrixSquare(long[] square, long[] mat) {
        for (int n = 0; n < GF2_DIM; n++)
            square[n] = gf2MatrixTimes(mat, mat[n]);
    }

    /*
     * Return the CRC-64 of two sequential blocks, where summ1 is the CRC-64 of
     * the first block, summ2 is the CRC-64 of the second block, and len2 is the
     * length of the second block.
     */
    static public long combine(long crcLast, long crcNext, long len2) {
        // degenerate case.
        if (len2 == 0)
            return crcLast;

        int n;
        long row;
        long[] even = new long[GF2_DIM]; // even-power-of-two zeros operator
        long[] odd = new long[GF2_DIM]; // odd-power-of-two zeros operator

        // put operator for one zero bit in odd
        odd[0] = POLY; // CRC-64 polynomial

        row = 1;
        for (n = 1; n < GF2_DIM; n++) {
            odd[n] = row;
            row <<= 1;
        }

        // put operator for two zero bits in even
        gf2MatrixSquare(even, odd);

        // put operator for four zero bits in odd
        gf2MatrixSquare(odd, even);

        // apply len2 zeros to crc1 (first square will put the operator for one
        // zero byte, eight zero bits, in even)
        long crc1 = crcLast;
        long crc2 = crcNext;
        do {
            // apply zeros operator for this bit of len2
            gf2MatrixSquare(even, odd);
            if ((len2 & 1) == 1)
                crc1 = gf2MatrixTimes(even, crc1);
            len2 >>>= 1;

            // if no more bits set, then done
            if (len2 == 0)
                break;

            // another iteration of the loop with odd and even swapped
            gf2MatrixSquare(odd, even);
            if ((len2 & 1) == 1)
                crc1 = gf2MatrixTimes(odd, crc1);
            len2 >>>= 1;

            // if no more bits set, then done
        } while (len2 != 0);

        // return combined crc.
        crc1 ^= crc2;
        return crc1;
    }

    /**
     * Get long representation of current CRC64 value.
     */
    public long getValue() {
        return this.value;
    }

    @Override
    public void reset() {
        this.value = 0;
    }

    @Override
    public void update(int val) {
        byte[] b = new byte[1];
        b[0] = (byte) (val & 0xff);
        update(b, b.length);
    }

    /**
     * Update CRC64 with new byte block.
     */
    public void update(byte[] b, int len) {
        update(b, 0, len);
    }

    @Override
    public void update(byte[] b, int off, int len) {

        this.value = ~this.value;

        /* fast middle processing, 8 bytes (aligned!) per loop */

        int idx = off;
        while (len >= 8) {
            this.value = table[7][(int) (value & 0xff ^ (b[idx] & 0xff))]
                    ^ table[6][(int) ((value >>> 8) & 0xff ^ (b[idx + 1] & 0xff))]
                    ^ table[5][(int) ((value >>> 16) & 0xff ^ (b[idx + 2] & 0xff))]
                    ^ table[4][(int) ((value >>> 24) & 0xff ^ (b[idx + 3] & 0xff))]
                    ^ table[3][(int) ((value >>> 32) & 0xff ^ (b[idx + 4] & 0xff))]
                    ^ table[2][(int) ((value >>> 40) & 0xff ^ (b[idx + 5] & 0xff))]
                    ^ table[1][(int) ((value >>> 48) & 0xff ^ (b[idx + 6] & 0xff))]
                    ^ table[0][(int) ((value >>> 56) ^ b[idx + 7] & 0xff)];
            idx += 8;
            len -= 8;
        }

        /* process remaining bytes (can't be larger than 8) */
        while (len > 0) {
            value = table[0][(int) ((this.value ^ b[idx]) & 0xff)] ^ (this.value >>> 8);
            idx++;
            len--;
        }

        this.value = ~this.value;
    }
}