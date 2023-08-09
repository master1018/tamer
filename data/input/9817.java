public class DerValue {
    public static final byte TAG_UNIVERSAL = (byte)0x000;
    public static final byte TAG_APPLICATION = (byte)0x040;
    public static final byte TAG_CONTEXT = (byte)0x080;
    public static final byte TAG_PRIVATE = (byte)0x0c0;
    public byte                 tag;
    protected DerInputBuffer    buffer;
    public final DerInputStream data;
    private int                 length;
    public final static byte    tag_Boolean = 0x01;
    public final static byte    tag_Integer = 0x02;
    public final static byte    tag_BitString = 0x03;
    public final static byte    tag_OctetString = 0x04;
    public final static byte    tag_Null = 0x05;
    public final static byte    tag_ObjectId = 0x06;
    public final static byte    tag_Enumerated = 0x0A;
    public final static byte    tag_UTF8String = 0x0C;
    public final static byte    tag_PrintableString = 0x13;
    public final static byte    tag_T61String = 0x14;
    public final static byte    tag_IA5String = 0x16;
    public final static byte    tag_UtcTime = 0x17;
    public final static byte    tag_GeneralizedTime = 0x18;
    public final static byte    tag_GeneralString = 0x1B;
    public final static byte    tag_UniversalString = 0x1C;
    public final static byte    tag_BMPString = 0x1E;
    public final static byte    tag_Sequence = 0x30;
    public final static byte    tag_SequenceOf = 0x30;
    public final static byte    tag_Set = 0x31;
    public final static byte    tag_SetOf = 0x31;
    public boolean isUniversal()      { return ((tag & 0x0c0) == 0x000); }
    public boolean isApplication()    { return ((tag & 0x0c0) == 0x040); }
    public boolean isContextSpecific() { return ((tag & 0x0c0) == 0x080); }
    public boolean isContextSpecific(byte cntxtTag) {
        if (!isContextSpecific()) {
            return false;
        }
        return ((tag & 0x01f) == cntxtTag);
    }
    boolean isPrivate()        { return ((tag & 0x0c0) == 0x0c0); }
    public boolean isConstructed()    { return ((tag & 0x020) == 0x020); }
    public boolean isConstructed(byte constructedTag) {
        if (!isConstructed()) {
            return false;
        }
        return ((tag & 0x01f) == constructedTag);
    }
    public DerValue(String value) throws IOException {
        boolean isPrintableString = true;
        for (int i = 0; i < value.length(); i++) {
            if (!isPrintableStringChar(value.charAt(i))) {
                isPrintableString = false;
                break;
            }
        }
        data = init(isPrintableString ? tag_PrintableString : tag_UTF8String, value);
    }
    public DerValue(byte stringTag, String value) throws IOException {
        data = init(stringTag, value);
    }
    public DerValue(byte tag, byte[] data) {
        this.tag = tag;
        buffer = new DerInputBuffer(data.clone());
        length = data.length;
        this.data = new DerInputStream(buffer);
        this.data.mark(Integer.MAX_VALUE);
    }
    DerValue(DerInputBuffer in) throws IOException {
        tag = (byte)in.read();
        byte lenByte = (byte)in.read();
        length = DerInputStream.getLength((lenByte & 0xff), in);
        if (length == -1) {  
            DerInputBuffer inbuf = in.dup();
            int readLen = inbuf.available();
            int offset = 2;     
            byte[] indefData = new byte[readLen + offset];
            indefData[0] = tag;
            indefData[1] = lenByte;
            DataInputStream dis = new DataInputStream(inbuf);
            dis.readFully(indefData, offset, readLen);
            dis.close();
            DerIndefLenConverter derIn = new DerIndefLenConverter();
            inbuf = new DerInputBuffer(derIn.convert(indefData));
            if (tag != inbuf.read())
                throw new IOException
                        ("Indefinite length encoding not supported");
            length = DerInputStream.getLength(inbuf);
            buffer = inbuf.dup();
            buffer.truncate(length);
            data = new DerInputStream(buffer);
            in.skip(length + offset);
        } else {
            buffer = in.dup();
            buffer.truncate(length);
            data = new DerInputStream(buffer);
            in.skip(length);
        }
    }
    public DerValue(byte[] buf) throws IOException {
        data = init(true, new ByteArrayInputStream(buf));
    }
    public DerValue(byte[] buf, int offset, int len) throws IOException {
        data = init(true, new ByteArrayInputStream(buf, offset, len));
    }
    public DerValue(InputStream in) throws IOException {
        data = init(false, in);
    }
    private DerInputStream init(byte stringTag, String value) throws IOException {
        String enc = null;
        tag = stringTag;
        switch (stringTag) {
        case tag_PrintableString:
        case tag_IA5String:
        case tag_GeneralString:
            enc = "ASCII";
            break;
        case tag_T61String:
            enc = "ISO-8859-1";
            break;
        case tag_BMPString:
            enc = "UnicodeBigUnmarked";
            break;
        case tag_UTF8String:
            enc = "UTF8";
            break;
        default:
            throw new IllegalArgumentException("Unsupported DER string type");
        }
        byte[] buf = value.getBytes(enc);
        length = buf.length;
        buffer = new DerInputBuffer(buf);
        DerInputStream result = new DerInputStream(buffer);
        result.mark(Integer.MAX_VALUE);
        return result;
    }
    private DerInputStream init(boolean fullyBuffered, InputStream in)
            throws IOException {
        tag = (byte)in.read();
        byte lenByte = (byte)in.read();
        length = DerInputStream.getLength((lenByte & 0xff), in);
        if (length == -1) { 
            int readLen = in.available();
            int offset = 2;     
            byte[] indefData = new byte[readLen + offset];
            indefData[0] = tag;
            indefData[1] = lenByte;
            DataInputStream dis = new DataInputStream(in);
            dis.readFully(indefData, offset, readLen);
            dis.close();
            DerIndefLenConverter derIn = new DerIndefLenConverter();
            in = new ByteArrayInputStream(derIn.convert(indefData));
            if (tag != in.read())
                throw new IOException
                        ("Indefinite length encoding not supported");
            length = DerInputStream.getLength(in);
        }
        if (fullyBuffered && in.available() != length)
            throw new IOException("extra data given to DerValue constructor");
        byte[] bytes = IOUtils.readFully(in, length, true);
        buffer = new DerInputBuffer(bytes);
        return new DerInputStream(buffer);
    }
    public void encode(DerOutputStream out)
    throws IOException {
        out.write(tag);
        out.putLength(length);
        if (length > 0) {
            byte[] value = new byte[length];
            synchronized (data) {
                buffer.reset();
                if (buffer.read(value) != length) {
                    throw new IOException("short DER value read (encode)");
                }
                out.write(value);
            }
        }
    }
    public final DerInputStream getData() {
        return data;
    }
    public final byte getTag() {
        return tag;
    }
    public boolean getBoolean() throws IOException {
        if (tag != tag_Boolean) {
            throw new IOException("DerValue.getBoolean, not a BOOLEAN " + tag);
        }
        if (length != 1) {
            throw new IOException("DerValue.getBoolean, invalid length "
                                        + length);
        }
        if (buffer.read() != 0) {
            return true;
        }
        return false;
    }
    public ObjectIdentifier getOID() throws IOException {
        if (tag != tag_ObjectId)
            throw new IOException("DerValue.getOID, not an OID " + tag);
        return new ObjectIdentifier(buffer);
    }
    private byte[] append(byte[] a, byte[] b) {
        if (a == null)
            return b;
        byte[] ret = new byte[a.length + b.length];
        System.arraycopy(a, 0, ret, 0, a.length);
        System.arraycopy(b, 0, ret, a.length, b.length);
        return ret;
    }
    public byte[] getOctetString() throws IOException {
        byte[] bytes;
        if (tag != tag_OctetString && !isConstructed(tag_OctetString)) {
            throw new IOException(
                "DerValue.getOctetString, not an Octet String: " + tag);
        }
        bytes = new byte[length];
        if (length == 0) {
            return bytes;
        }
        if (buffer.read(bytes) != length)
            throw new IOException("short read on DerValue buffer");
        if (isConstructed()) {
            DerInputStream in = new DerInputStream(bytes);
            bytes = null;
            while (in.available() != 0) {
                bytes = append(bytes, in.getOctetString());
            }
        }
        return bytes;
    }
    public int getInteger() throws IOException {
        if (tag != tag_Integer) {
            throw new IOException("DerValue.getInteger, not an int " + tag);
        }
        return buffer.getInteger(data.available());
    }
    public BigInteger getBigInteger() throws IOException {
        if (tag != tag_Integer)
            throw new IOException("DerValue.getBigInteger, not an int " + tag);
        return buffer.getBigInteger(data.available(), false);
    }
    public BigInteger getPositiveBigInteger() throws IOException {
        if (tag != tag_Integer)
            throw new IOException("DerValue.getBigInteger, not an int " + tag);
        return buffer.getBigInteger(data.available(), true);
    }
    public int getEnumerated() throws IOException {
        if (tag != tag_Enumerated) {
            throw new IOException("DerValue.getEnumerated, incorrect tag: "
                                  + tag);
        }
        return buffer.getInteger(data.available());
    }
    public byte[] getBitString() throws IOException {
        if (tag != tag_BitString)
            throw new IOException(
                "DerValue.getBitString, not a bit string " + tag);
        return buffer.getBitString();
    }
    public BitArray getUnalignedBitString() throws IOException {
        if (tag != tag_BitString)
            throw new IOException(
                "DerValue.getBitString, not a bit string " + tag);
        return buffer.getUnalignedBitString();
    }
    public String getAsString() throws IOException {
        if (tag == tag_UTF8String)
            return getUTF8String();
        else if (tag == tag_PrintableString)
            return getPrintableString();
        else if (tag == tag_T61String)
            return getT61String();
        else if (tag == tag_IA5String)
            return getIA5String();
        else if (tag == tag_BMPString)
            return getBMPString();
        else if (tag == tag_GeneralString)
            return getGeneralString();
        else
            return null;
    }
    public byte[] getBitString(boolean tagImplicit) throws IOException {
        if (!tagImplicit) {
            if (tag != tag_BitString)
                throw new IOException("DerValue.getBitString, not a bit string "
                                       + tag);
            }
        return buffer.getBitString();
    }
    public BitArray getUnalignedBitString(boolean tagImplicit)
    throws IOException {
        if (!tagImplicit) {
            if (tag != tag_BitString)
                throw new IOException("DerValue.getBitString, not a bit string "
                                       + tag);
            }
        return buffer.getUnalignedBitString();
    }
    public byte[] getDataBytes() throws IOException {
        byte[] retVal = new byte[length];
        synchronized (data) {
            data.reset();
            data.getBytes(retVal);
        }
        return retVal;
    }
    public String getPrintableString()
    throws IOException {
        if (tag != tag_PrintableString)
            throw new IOException(
                "DerValue.getPrintableString, not a string " + tag);
        return new String(getDataBytes(), "ASCII");
    }
    public String getT61String() throws IOException {
        if (tag != tag_T61String)
            throw new IOException(
                "DerValue.getT61String, not T61 " + tag);
        return new String(getDataBytes(), "ISO-8859-1");
    }
    public String getIA5String() throws IOException {
        if (tag != tag_IA5String)
            throw new IOException(
                "DerValue.getIA5String, not IA5 " + tag);
        return new String(getDataBytes(), "ASCII");
    }
    public String getBMPString() throws IOException {
        if (tag != tag_BMPString)
            throw new IOException(
                "DerValue.getBMPString, not BMP " + tag);
        return new String(getDataBytes(), "UnicodeBigUnmarked");
    }
    public String getUTF8String() throws IOException {
        if (tag != tag_UTF8String)
            throw new IOException(
                "DerValue.getUTF8String, not UTF-8 " + tag);
        return new String(getDataBytes(), "UTF8");
    }
    public String getGeneralString() throws IOException {
        if (tag != tag_GeneralString)
            throw new IOException(
                "DerValue.getGeneralString, not GeneralString " + tag);
        return new String(getDataBytes(), "ASCII");
    }
    public Date getUTCTime() throws IOException {
        if (tag != tag_UtcTime) {
            throw new IOException("DerValue.getUTCTime, not a UtcTime: " + tag);
        }
        return buffer.getUTCTime(data.available());
    }
    public Date getGeneralizedTime() throws IOException {
        if (tag != tag_GeneralizedTime) {
            throw new IOException(
                "DerValue.getGeneralizedTime, not a GeneralizedTime: " + tag);
        }
        return buffer.getGeneralizedTime(data.available());
    }
    public boolean equals(Object other) {
        if (other instanceof DerValue)
            return equals((DerValue)other);
        else
            return false;
    }
    public boolean equals(DerValue other) {
        if (this == other) {
            return true;
        }
        if (tag != other.tag) {
            return false;
        }
        if (data == other.data) {
            return true;
        }
        return (System.identityHashCode(this.data)
                > System.identityHashCode(other.data)) ?
                doEquals(this, other):
                doEquals(other, this);
    }
    private static boolean doEquals(DerValue d1, DerValue d2) {
        synchronized (d1.data) {
            synchronized (d2.data) {
                d1.data.reset();
                d2.data.reset();
                return d1.buffer.equals(d2.buffer);
            }
        }
    }
    public String toString() {
        try {
            String str = getAsString();
            if (str != null)
                return "\"" + str + "\"";
            if (tag == tag_Null)
                return "[DerValue, null]";
            if (tag == tag_ObjectId)
                return "OID." + getOID();
            else
                return "[DerValue, tag = " + tag
                        + ", length = " + length + "]";
        } catch (IOException e) {
            throw new IllegalArgumentException("misformatted DER value");
        }
    }
    public byte[] toByteArray() throws IOException {
        DerOutputStream out = new DerOutputStream();
        encode(out);
        data.reset();
        return out.toByteArray();
    }
    public DerInputStream toDerInputStream() throws IOException {
        if (tag == tag_Sequence || tag == tag_Set)
            return new DerInputStream(buffer);
        throw new IOException("toDerInputStream rejects tag type " + tag);
    }
    public int length() {
        return length;
    }
    public static boolean isPrintableStringChar(char ch) {
        if ((ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z') ||
            (ch >= '0' && ch <= '9')) {
            return true;
        } else {
            switch (ch) {
                case ' ':       
                case '\'':      
                case '(':       
                case ')':       
                case '+':       
                case ',':       
                case '-':       
                case '.':       
                case '/':       
                case ':':       
                case '=':       
                case '?':       
                    return true;
                default:
                    return false;
            }
        }
    }
    public static byte createTag(byte tagClass, boolean form, byte val) {
        byte tag = (byte)(tagClass | val);
        if (form) {
            tag |= (byte)0x20;
        }
        return (tag);
    }
    public void resetTag(byte tag) {
        this.tag = tag;
    }
    public int hashCode() {
        return toString().hashCode();
    }
}
