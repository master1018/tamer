public class DerInputStream {
    DerInputBuffer      buffer;
    public byte         tag;
    public DerInputStream(byte[] data) throws IOException {
        init(data, 0, data.length);
    }
    public DerInputStream(byte[] data, int offset, int len) throws IOException {
        init(data, offset, len);
    }
    private void init(byte[] data, int offset, int len) throws IOException {
        if ((offset+2 > data.length) || (offset+len > data.length)) {
            throw new IOException("Encoding bytes too short");
        }
        if (DerIndefLenConverter.isIndefinite(data[offset+1])) {
            byte[] inData = new byte[len];
            System.arraycopy(data, offset, inData, 0, len);
            DerIndefLenConverter derIn = new DerIndefLenConverter();
            buffer = new DerInputBuffer(derIn.convert(inData));
        } else
            buffer = new DerInputBuffer(data, offset, len);
        buffer.mark(Integer.MAX_VALUE);
    }
    DerInputStream(DerInputBuffer buf) {
        buffer = buf;
        buffer.mark(Integer.MAX_VALUE);
    }
    public DerInputStream subStream(int len, boolean do_skip)
    throws IOException {
        DerInputBuffer  newbuf = buffer.dup();
        newbuf.truncate(len);
        if (do_skip) {
            buffer.skip(len);
        }
        return new DerInputStream(newbuf);
    }
    public byte[] toByteArray() {
        return buffer.toByteArray();
    }
    public int getInteger() throws IOException {
        if (buffer.read() != DerValue.tag_Integer) {
            throw new IOException("DER input, Integer tag error");
        }
        return buffer.getInteger(getLength(buffer));
    }
    public BigInteger getBigInteger() throws IOException {
        if (buffer.read() != DerValue.tag_Integer) {
            throw new IOException("DER input, Integer tag error");
        }
        return buffer.getBigInteger(getLength(buffer), false);
    }
    public BigInteger getPositiveBigInteger() throws IOException {
        if (buffer.read() != DerValue.tag_Integer) {
            throw new IOException("DER input, Integer tag error");
        }
        return buffer.getBigInteger(getLength(buffer), true);
    }
    public int getEnumerated() throws IOException {
        if (buffer.read() != DerValue.tag_Enumerated) {
            throw new IOException("DER input, Enumerated tag error");
        }
        return buffer.getInteger(getLength(buffer));
    }
    public byte[] getBitString() throws IOException {
        if (buffer.read() != DerValue.tag_BitString)
            throw new IOException("DER input not an bit string");
        return buffer.getBitString(getLength(buffer));
    }
    public BitArray getUnalignedBitString() throws IOException {
        if (buffer.read() != DerValue.tag_BitString)
            throw new IOException("DER input not a bit string");
        int length = getLength(buffer) - 1;
        int validBits = length*8 - buffer.read();
        byte[] repn = new byte[length];
        if ((length != 0) && (buffer.read(repn) != length))
            throw new IOException("short read of DER bit string");
        return new BitArray(validBits, repn);
    }
    public byte[] getOctetString() throws IOException {
        if (buffer.read() != DerValue.tag_OctetString)
            throw new IOException("DER input not an octet string");
        int length = getLength(buffer);
        byte[] retval = new byte[length];
        if ((length != 0) && (buffer.read(retval) != length))
            throw new IOException("short read of DER octet string");
        return retval;
    }
    public void getBytes(byte[] val) throws IOException {
        if ((val.length != 0) && (buffer.read(val) != val.length)) {
            throw new IOException("short read of DER octet string");
        }
    }
    public void getNull() throws IOException {
        if (buffer.read() != DerValue.tag_Null || buffer.read() != 0)
            throw new IOException("getNull, bad data");
    }
    public ObjectIdentifier getOID() throws IOException {
        return new ObjectIdentifier(this);
    }
    public DerValue[] getSequence(int startLen) throws IOException {
        tag = (byte)buffer.read();
        if (tag != DerValue.tag_Sequence)
            throw new IOException("Sequence tag error");
        return readVector(startLen);
    }
    public DerValue[] getSet(int startLen) throws IOException {
        tag = (byte)buffer.read();
        if (tag != DerValue.tag_Set)
            throw new IOException("Set tag error");
        return readVector(startLen);
    }
    public DerValue[] getSet(int startLen, boolean implicit)
        throws IOException {
        tag = (byte)buffer.read();
        if (!implicit) {
            if (tag != DerValue.tag_Set) {
                throw new IOException("Set tag error");
            }
        }
        return (readVector(startLen));
    }
    protected DerValue[] readVector(int startLen) throws IOException {
        DerInputStream  newstr;
        byte lenByte = (byte)buffer.read();
        int len = getLength((lenByte & 0xff), buffer);
        if (len == -1) {
           int readLen = buffer.available();
           int offset = 2;     
           byte[] indefData = new byte[readLen + offset];
           indefData[0] = tag;
           indefData[1] = lenByte;
           DataInputStream dis = new DataInputStream(buffer);
           dis.readFully(indefData, offset, readLen);
           dis.close();
           DerIndefLenConverter derIn = new DerIndefLenConverter();
           buffer = new DerInputBuffer(derIn.convert(indefData));
           if (tag != buffer.read())
                throw new IOException("Indefinite length encoding" +
                        " not supported");
           len = DerInputStream.getLength(buffer);
        }
        if (len == 0)
            return new DerValue[0];
        if (buffer.available() == len)
            newstr = this;
        else
            newstr = subStream(len, true);
        Vector<DerValue> vec = new Vector<DerValue>(startLen);
        DerValue value;
        do {
            value = new DerValue(newstr.buffer);
            vec.addElement(value);
        } while (newstr.available() > 0);
        if (newstr.available() != 0)
            throw new IOException("extra data at end of vector");
        int             i, max = vec.size();
        DerValue[]      retval = new DerValue[max];
        for (i = 0; i < max; i++)
            retval[i] = vec.elementAt(i);
        return retval;
    }
    public DerValue getDerValue() throws IOException {
        return new DerValue(buffer);
    }
    public String getUTF8String() throws IOException {
        return readString(DerValue.tag_UTF8String, "UTF-8", "UTF8");
    }
    public String getPrintableString() throws IOException {
        return readString(DerValue.tag_PrintableString, "Printable",
                          "ASCII");
    }
    public String getT61String() throws IOException {
        return readString(DerValue.tag_T61String, "T61", "ISO-8859-1");
    }
    public String getIA5String() throws IOException {
        return readString(DerValue.tag_IA5String, "IA5", "ASCII");
    }
    public String getBMPString() throws IOException {
        return readString(DerValue.tag_BMPString, "BMP",
                          "UnicodeBigUnmarked");
    }
    public String getGeneralString() throws IOException {
        return readString(DerValue.tag_GeneralString, "General",
                          "ASCII");
    }
    private String readString(byte stringTag, String stringName,
                              String enc) throws IOException {
        if (buffer.read() != stringTag)
            throw new IOException("DER input not a " +
                                  stringName + " string");
        int length = getLength(buffer);
        byte[] retval = new byte[length];
        if ((length != 0) && (buffer.read(retval) != length))
            throw new IOException("short read of DER " +
                                  stringName + " string");
        return new String(retval, enc);
    }
    public Date getUTCTime() throws IOException {
        if (buffer.read() != DerValue.tag_UtcTime)
            throw new IOException("DER input, UTCtime tag invalid ");
        return buffer.getUTCTime(getLength(buffer));
    }
    public Date getGeneralizedTime() throws IOException {
        if (buffer.read() != DerValue.tag_GeneralizedTime)
            throw new IOException("DER input, GeneralizedTime tag invalid ");
        return buffer.getGeneralizedTime(getLength(buffer));
    }
    int getByte() throws IOException {
        return (0x00ff & buffer.read());
    }
    public int peekByte() throws IOException {
        return buffer.peek();
    }
    int getLength() throws IOException {
        return getLength(buffer);
    }
    static int getLength(InputStream in) throws IOException {
        return getLength(in.read(), in);
    }
    static int getLength(int lenByte, InputStream in) throws IOException {
        int value, tmp;
        tmp = lenByte;
        if ((tmp & 0x080) == 0x00) { 
            value = tmp;
        } else {                     
            tmp &= 0x07f;
            if (tmp == 0)
                return -1;
            if (tmp < 0 || tmp > 4)
                throw new IOException("DerInputStream.getLength(): lengthTag="
                    + tmp + ", "
                    + ((tmp < 0) ? "incorrect DER encoding." : "too big."));
            for (value = 0; tmp > 0; tmp --) {
                value <<= 8;
                value += 0x0ff & in.read();
            }
        }
        return value;
    }
    public void mark(int value) { buffer.mark(value); }
    public void reset() { buffer.reset(); }
    public int available() { return buffer.available(); }
}
