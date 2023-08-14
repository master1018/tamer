public final class BerDecoder extends Ber {
    private int origOffset;  
    public BerDecoder(byte buf[], int offset, int bufsize) {
        this.buf = buf;
        this.bufsize = bufsize;
        this.origOffset = offset;
        reset();
    }
    public void reset() {
        offset = origOffset;
    }
    public int getParsePosition() {
        return offset;
    }
    public int parseLength() throws DecodeException {
        int lengthbyte = parseByte();
        if ((lengthbyte & 0x80) == 0x80) {
            lengthbyte &= 0x7f;
            if (lengthbyte == 0) {
                throw new DecodeException(
                    "Indefinite length not supported");
            }
            if (lengthbyte > 4) {
                throw new DecodeException("encoding too long");
            }
            if (bufsize - offset < lengthbyte) {
                throw new DecodeException("Insufficient data");
            }
            int retval = 0;
            for( int i = 0; i < lengthbyte; i++) {
                retval = (retval << 8) + (buf[offset++] & 0xff);
            }
            return retval;
        } else {
            return lengthbyte;
        }
    }
    public int parseSeq(int rlen[]) throws DecodeException {
        int seq = parseByte();
        int len = parseLength();
        if (rlen != null) {
            rlen[0] = len;
        }
        return seq;
    }
    void seek(int i) throws DecodeException {
        if (offset + i > bufsize || offset + i < 0) {
            throw new DecodeException("array index out of bounds");
        }
        offset += i;
    }
    public int parseByte() throws DecodeException {
        if (bufsize - offset < 1) {
            throw new DecodeException("Insufficient data");
        }
        return buf[offset++] & 0xff;
    }
    public int peekByte() throws DecodeException {
        if (bufsize - offset < 1) {
            throw new DecodeException("Insufficient data");
        }
        return buf[offset] & 0xff;
    }
    public boolean parseBoolean() throws DecodeException {
        return ((parseIntWithTag(ASN_BOOLEAN) == 0x00) ? false : true);
    }
    public int parseEnumeration() throws DecodeException {
        return parseIntWithTag(ASN_ENUMERATED);
    }
    public int parseInt() throws DecodeException {
        return parseIntWithTag(ASN_INTEGER);
    }
    private int parseIntWithTag(int tag) throws DecodeException {
        if (parseByte() != tag) {
            throw new DecodeException("Encountered ASN.1 tag " +
                Integer.toString(buf[offset - 1] & 0xff) +
                " (expected tag " + Integer.toString(tag) + ")");
        }
        int len = parseLength();
        if (len > 4) {
            throw new DecodeException("INTEGER too long");
        } else if (len > bufsize - offset) {
            throw new DecodeException("Insufficient data");
        }
        byte fb = buf[offset++];
        int value = 0;
        value = fb & 0x7F;
        for( int i = 1  ; i < len; i++) {
            value <<= 8;
            value |= (buf[offset++] & 0xff);
        }
        if ((fb & 0x80) == 0x80) {
            value = -value;
        }
        return value;
    }
    public String parseString(boolean decodeUTF8) throws DecodeException {
        return parseStringWithTag(ASN_SIMPLE_STRING, decodeUTF8, null);
    }
    public String parseStringWithTag(int tag, boolean decodeUTF8, int rlen[])
        throws DecodeException {
        int st;
        int origOffset = offset;
        if ((st = parseByte()) != tag) {
            throw new DecodeException("Encountered ASN.1 tag " +
                Integer.toString((byte)st) + " (expected tag " + tag + ")");
        }
        int len = parseLength();
        if (len > bufsize - offset) {
            throw new DecodeException("Insufficient data");
        }
        String retstr;
        if (len == 0) {
            retstr = "";
        } else {
            byte[] buf2 = new byte[len];
            System.arraycopy(buf, offset, buf2, 0, len);
            if (decodeUTF8) {
                try {
                    retstr = new String(buf2, "UTF8");
                } catch (UnsupportedEncodingException e) {
                    throw new DecodeException("UTF8 not available on platform");
                }
            } else {
                try {
                    retstr = new String(buf2, "8859_1");
                } catch (UnsupportedEncodingException e) {
                    throw new DecodeException("8859_1 not available on platform");
                }
            }
            offset += len;
        }
        if (rlen != null) {
            rlen[0] = offset - origOffset;
        }
        return retstr;
    }
    public byte[] parseOctetString(int tag, int rlen[]) throws DecodeException {
        int origOffset = offset;
        int st;
        if ((st = parseByte()) != tag) {
            throw new DecodeException("Encountered ASN.1 tag " +
                Integer.toString(st) +
                " (expected tag " + Integer.toString(tag) + ")");
        }
        int len = parseLength();
        if (len > bufsize - offset) {
            throw new DecodeException("Insufficient data");
        }
        byte retarr[] = new byte[len];
        if (len > 0) {
            System.arraycopy(buf, offset, retarr, 0, len);
            offset += len;
        }
        if (rlen != null) {
            rlen[0] = offset - origOffset;
        }
        return retarr;
    }
    public int bytesLeft() {
        return bufsize - offset;
    }
}
