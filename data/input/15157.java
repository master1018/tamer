public class DerOutputStream
extends ByteArrayOutputStream implements DerEncoder {
    public DerOutputStream(int size) { super(size); }
    public DerOutputStream() { }
    public void write(byte tag, byte[] buf) throws IOException {
        write(tag);
        putLength(buf.length);
        write(buf, 0, buf.length);
    }
    public void write(byte tag, DerOutputStream out) throws IOException {
        write(tag);
        putLength(out.count);
        write(out.buf, 0, out.count);
    }
    public void writeImplicit(byte tag, DerOutputStream value)
    throws IOException {
        write(tag);
        write(value.buf, 1, value.count-1);
    }
    public void putDerValue(DerValue val) throws IOException {
        val.encode(this);
    }
    public void putBoolean(boolean val) throws IOException {
        write(DerValue.tag_Boolean);
        putLength(1);
        if (val) {
            write(0xff);
        } else {
            write(0);
        }
    }
    public void putEnumerated(int i) throws IOException {
        write(DerValue.tag_Enumerated);
        putIntegerContents(i);
    }
    public void putInteger(BigInteger i) throws IOException {
        write(DerValue.tag_Integer);
        byte[]    buf = i.toByteArray(); 
        putLength(buf.length);
        write(buf, 0, buf.length);
    }
    public void putInteger(Integer i) throws IOException {
        putInteger(i.intValue());
    }
    public void putInteger(int i) throws IOException {
        write(DerValue.tag_Integer);
        putIntegerContents(i);
    }
    private void putIntegerContents(int i) throws IOException {
        byte[] bytes = new byte[4];
        int start = 0;
        bytes[3] = (byte) (i & 0xff);
        bytes[2] = (byte)((i & 0xff00) >>> 8);
        bytes[1] = (byte)((i & 0xff0000) >>> 16);
        bytes[0] = (byte)((i & 0xff000000) >>> 24);
        if (bytes[0] == (byte)0xff) {
            for (int j = 0; j < 3; j++) {
                if ((bytes[j] == (byte)0xff) &&
                    ((bytes[j+1] & 0x80) == 0x80))
                    start++;
                else
                    break;
             }
         } else if (bytes[0] == 0x00) {
            for (int j = 0; j < 3; j++) {
                if ((bytes[j] == 0x00) &&
                    ((bytes[j+1] & 0x80) == 0))
                    start++;
                else
                    break;
            }
        }
        putLength(4 - start);
        for (int k = start; k < 4; k++)
            write(bytes[k]);
    }
    public void putBitString(byte[] bits) throws IOException {
        write(DerValue.tag_BitString);
        putLength(bits.length + 1);
        write(0);               
        write(bits);
    }
    public void putUnalignedBitString(BitArray ba) throws IOException {
        byte[] bits = ba.toByteArray();
        write(DerValue.tag_BitString);
        putLength(bits.length + 1);
        write(bits.length*8 - ba.length()); 
        write(bits);
    }
    public void putTruncatedUnalignedBitString(BitArray ba) throws IOException {
        putUnalignedBitString(ba.truncate());
    }
    public void putOctetString(byte[] octets) throws IOException {
        write(DerValue.tag_OctetString, octets);
    }
    public void putNull() throws IOException {
        write(DerValue.tag_Null);
        putLength(0);
    }
    public void putOID(ObjectIdentifier oid) throws IOException {
        oid.encode(this);
    }
    public void putSequence(DerValue[] seq) throws IOException {
        DerOutputStream bytes = new DerOutputStream();
        int i;
        for (i = 0; i < seq.length; i++)
            seq[i].encode(bytes);
        write(DerValue.tag_Sequence, bytes);
    }
    public void putSet(DerValue[] set) throws IOException {
        DerOutputStream bytes = new DerOutputStream();
        int i;
        for (i = 0; i < set.length; i++)
            set[i].encode(bytes);
        write(DerValue.tag_Set, bytes);
    }
    public void putOrderedSetOf(byte tag, DerEncoder[] set) throws IOException {
        putOrderedSet(tag, set, lexOrder);
    }
    public void putOrderedSet(byte tag, DerEncoder[] set) throws IOException {
        putOrderedSet(tag, set, tagOrder);
    }
    private static ByteArrayLexOrder lexOrder = new ByteArrayLexOrder();
    private static ByteArrayTagOrder tagOrder = new ByteArrayTagOrder();
    private void putOrderedSet(byte tag, DerEncoder[] set,
                               Comparator<byte[]> order) throws IOException {
        DerOutputStream[] streams = new DerOutputStream[set.length];
        for (int i = 0; i < set.length; i++) {
            streams[i] = new DerOutputStream();
            set[i].derEncode(streams[i]);
        }
        byte[][] bufs = new byte[streams.length][];
        for (int i = 0; i < streams.length; i++) {
            bufs[i] = streams[i].toByteArray();
        }
        Arrays.<byte[]>sort(bufs, order);
        DerOutputStream bytes = new DerOutputStream();
        for (int i = 0; i < streams.length; i++) {
            bytes.write(bufs[i]);
        }
        write(tag, bytes);
    }
    public void putUTF8String(String s) throws IOException {
        writeString(s, DerValue.tag_UTF8String, "UTF8");
    }
    public void putPrintableString(String s) throws IOException {
        writeString(s, DerValue.tag_PrintableString, "ASCII");
    }
    public void putT61String(String s) throws IOException {
        writeString(s, DerValue.tag_T61String, "ISO-8859-1");
    }
    public void putIA5String(String s) throws IOException {
        writeString(s, DerValue.tag_IA5String, "ASCII");
    }
    public void putBMPString(String s) throws IOException {
        writeString(s, DerValue.tag_BMPString, "UnicodeBigUnmarked");
    }
    public void putGeneralString(String s) throws IOException {
        writeString(s, DerValue.tag_GeneralString, "ASCII");
    }
    private void writeString(String s, byte stringTag, String enc)
        throws IOException {
        byte[] data = s.getBytes(enc);
        write(stringTag);
        putLength(data.length);
        write(data);
    }
    public void putUTCTime(Date d) throws IOException {
        putTime(d, DerValue.tag_UtcTime);
    }
    public void putGeneralizedTime(Date d) throws IOException {
        putTime(d, DerValue.tag_GeneralizedTime);
    }
    private void putTime(Date d, byte tag) throws IOException {
        TimeZone tz = TimeZone.getTimeZone("GMT");
        String pattern = null;
        if (tag == DerValue.tag_UtcTime) {
            pattern = "yyMMddHHmmss'Z'";
        } else {
            tag = DerValue.tag_GeneralizedTime;
            pattern = "yyyyMMddHHmmss'Z'";
        }
        SimpleDateFormat sdf = new SimpleDateFormat(pattern, Locale.US);
        sdf.setTimeZone(tz);
        byte[] time = (sdf.format(d)).getBytes("ISO-8859-1");
        write(tag);
        putLength(time.length);
        write(time);
    }
    public void putLength(int len) throws IOException {
        if (len < 128) {
            write((byte)len);
        } else if (len < (1 << 8)) {
            write((byte)0x081);
            write((byte)len);
        } else if (len < (1 << 16)) {
            write((byte)0x082);
            write((byte)(len >> 8));
            write((byte)len);
        } else if (len < (1 << 24)) {
            write((byte)0x083);
            write((byte)(len >> 16));
            write((byte)(len >> 8));
            write((byte)len);
        } else {
            write((byte)0x084);
            write((byte)(len >> 24));
            write((byte)(len >> 16));
            write((byte)(len >> 8));
            write((byte)len);
        }
    }
    public void putTag(byte tagClass, boolean form, byte val) {
        byte tag = (byte)(tagClass | val);
        if (form) {
            tag |= (byte)0x20;
        }
        write(tag);
    }
    public void derEncode(OutputStream out) throws IOException {
        out.write(toByteArray());
    }
}
