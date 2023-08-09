public final class BerEncoder extends Ber {
    private int curSeqIndex;
    private int seqOffset[];
    private static final int INITIAL_SEQUENCES = 16;
    private static final int DEFAULT_BUFSIZE = 1024;
    private static final int BUF_GROWTH_FACTOR = 8;
    public BerEncoder() {
        this(DEFAULT_BUFSIZE);
    }
    public BerEncoder(int bufsize) {
        buf = new byte[bufsize];
        this.bufsize = bufsize;
        offset = 0;
        seqOffset = new int[INITIAL_SEQUENCES];
        curSeqIndex = 0;
    }
    public void reset() {
        while (offset > 0) {
            buf[--offset] = 0;
        }
        while (curSeqIndex > 0) {
            seqOffset[--curSeqIndex] = 0;
        }
    }
    public int getDataLen() {
        return offset;
    }
    public byte[] getBuf() {
        if (curSeqIndex != 0) {
            throw new IllegalStateException("BER encode error: Unbalanced SEQUENCEs.");
        }
        return buf;
    }
    public byte[] getTrimmedBuf() {
        int len = getDataLen();
        byte[] trimBuf = new byte[len];
        System.arraycopy(getBuf(), 0, trimBuf, 0, len);
        return trimBuf;
    }
    public void beginSeq(int tag) {
        if (curSeqIndex >= seqOffset.length) {
            int[] seqOffsetTmp = new int[seqOffset.length * 2];
            for (int i = 0; i < seqOffset.length; i++) {
                seqOffsetTmp[i] = seqOffset[i];
            }
            seqOffset = seqOffsetTmp;
        }
        encodeByte(tag);
        seqOffset[curSeqIndex] = offset;
        ensureFreeBytes(3);
        offset += 3;
        curSeqIndex++;
    }
    public void endSeq() throws EncodeException {
        curSeqIndex--;
        if (curSeqIndex < 0) {
            throw new IllegalStateException("BER encode error: Unbalanced SEQUENCEs.");
        }
        int start = seqOffset[curSeqIndex] + 3; 
        int len = offset - start;
        if (len <= 0x7f) {
            shiftSeqData(start, len, -2);
            buf[seqOffset[curSeqIndex]] = (byte) len;
        } else if (len <= 0xff) {
            shiftSeqData(start, len, -1);
            buf[seqOffset[curSeqIndex]] = (byte) 0x81;
            buf[seqOffset[curSeqIndex] + 1] = (byte) len;
        } else if (len <= 0xffff) {
            buf[seqOffset[curSeqIndex]] = (byte) 0x82;
            buf[seqOffset[curSeqIndex] + 1] = (byte) (len >> 8);
            buf[seqOffset[curSeqIndex] + 2] = (byte) len;
        } else if (len <= 0xffffff) {
            shiftSeqData(start, len, 1);
            buf[seqOffset[curSeqIndex]] = (byte) 0x83;
            buf[seqOffset[curSeqIndex] + 1] = (byte) (len >> 16);
            buf[seqOffset[curSeqIndex] + 2] = (byte) (len >> 8);
            buf[seqOffset[curSeqIndex] + 3] = (byte) len;
        } else {
            throw new EncodeException("SEQUENCE too long");
        }
    }
    private void shiftSeqData(int start, int len, int shift) {
        if (shift > 0) {
            ensureFreeBytes(shift);
        }
        System.arraycopy(buf, start, buf, start + shift, len);
        offset += shift;
    }
    public void encodeByte(int b) {
        ensureFreeBytes(1);
        buf[offset++] = (byte) b;
    }
    public void encodeInt(int i) {
        encodeInt(i, 0x02);
    }
    public void encodeInt(int i, int tag) {
        int mask = 0xff800000;
        int intsize = 4;
        while( (((i & mask) == 0) || ((i & mask) == mask)) && (intsize > 1) ) {
            intsize--;
            i <<= 8;
        }
        encodeInt(i, tag, intsize);
    }
    private void encodeInt(int i, int tag, int intsize) {
        if (intsize > 4) {
            throw new IllegalArgumentException("BER encode error: INTEGER too long.");
        }
        ensureFreeBytes(2 + intsize);
        buf[offset++] = (byte) tag;
        buf[offset++] = (byte) intsize;
        int mask = 0xff000000;
        while (intsize-- > 0) {
            buf[offset++] = (byte) ((i & mask) >> 24);
            i <<= 8;
        }
    }
    public void encodeBoolean(boolean b) {
        encodeBoolean(b, ASN_BOOLEAN);
    }
    public void encodeBoolean(boolean b, int tag) {
        ensureFreeBytes(3);
        buf[offset++] = (byte) tag;
        buf[offset++] = 0x01;
        buf[offset++] = b ? (byte) 0xff : (byte) 0x00;
    }
    public void encodeString(String str, boolean encodeUTF8)
        throws EncodeException {
        encodeString(str, ASN_OCTET_STR, encodeUTF8);
    }
    public void encodeString(String str, int tag, boolean encodeUTF8)
        throws EncodeException {
        encodeByte(tag);
        int i = 0;
        int count;
        byte[] bytes = null;
        if (str == null) {
            count = 0;
        } else if (encodeUTF8) {
            try {
                bytes = str.getBytes("UTF8");
                count = bytes.length;
            } catch (UnsupportedEncodingException e) {
                throw new EncodeException("UTF8 not available on platform");
            }
        } else {
            try {
                bytes = str.getBytes("8859_1");
                count = bytes.length;
            } catch (UnsupportedEncodingException e) {
                throw new EncodeException("8859_1 not available on platform");
            }
        }
        encodeLength(count);
        ensureFreeBytes(count);
        while (i < count) {
            buf[offset++] = bytes[i++];
        }
    }
    public void encodeOctetString(byte tb[], int tag, int tboffset, int length)
        throws EncodeException {
        encodeByte(tag);
        encodeLength(length);
        if (length > 0) {
            ensureFreeBytes(length);
            System.arraycopy(tb, tboffset, buf, offset, length);
            offset += length;
        }
    }
    public void encodeOctetString(byte tb[], int tag) throws EncodeException {
        encodeOctetString(tb, tag, 0, tb.length);
    }
    private void encodeLength(int len) throws EncodeException {
        ensureFreeBytes(4);     
        if (len < 128) {
            buf[offset++] = (byte) len;
        } else if (len <= 0xff) {
            buf[offset++] = (byte) 0x81;
            buf[offset++] = (byte) len;
        } else if (len <= 0xffff) {
            buf[offset++] = (byte) 0x82;
            buf[offset++] = (byte) (len >> 8);
            buf[offset++] = (byte) (len & 0xff);
        } else if (len <= 0xffffff) {
            buf[offset++] = (byte) 0x83;
            buf[offset++] = (byte) (len >> 16);
            buf[offset++] = (byte) (len >> 8);
            buf[offset++] = (byte) (len & 0xff);
        } else {
            throw new EncodeException("string too long");
        }
    }
    public void encodeStringArray(String strs[], boolean encodeUTF8)
        throws EncodeException {
        if (strs == null)
            return;
        for (int i = 0; i < strs.length; i++) {
            encodeString(strs[i], encodeUTF8);
        }
    }
    private void ensureFreeBytes(int len) {
        if (bufsize - offset < len) {
            int newsize = bufsize * BUF_GROWTH_FACTOR;
            if (newsize - offset < len) {
                newsize += len;
            }
            byte newbuf[] = new byte[newsize];
            System.arraycopy(buf, 0, newbuf, 0, offset);
            buf = newbuf;
            bufsize = newsize;
        }
    }
}
