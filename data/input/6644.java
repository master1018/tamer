public class GSSHeader {
    private ObjectIdentifier mechOid = null;
    private byte[] mechOidBytes = null;
    private int mechTokenLength = 0;
    public static final int TOKEN_ID=0x60;
    public GSSHeader(ObjectIdentifier mechOid, int mechTokenLength)
        throws IOException {
        this.mechOid = mechOid;
        DerOutputStream temp = new DerOutputStream();
        temp.putOID(mechOid);
        mechOidBytes = temp.toByteArray();
        this.mechTokenLength = mechTokenLength;
    }
    public GSSHeader(InputStream is)
        throws IOException, GSSException {
        int tag = is.read();
        if (tag != TOKEN_ID)
            throw new GSSException(GSSException.DEFECTIVE_TOKEN, -1,
                                   "GSSHeader did not find the right tag");
        int length = getLength(is);
        DerValue temp = new DerValue(is);
        mechOidBytes = temp.toByteArray();
        mechOid = temp.getOID();
        mechTokenLength = length - mechOidBytes.length;
    }
    public ObjectIdentifier getOid() {
        return mechOid;
    }
    public int getMechTokenLength() {
        return mechTokenLength;
    }
    public int getLength() {
        int lenField = mechOidBytes.length + mechTokenLength;
        return (1 + getLenFieldSize(lenField) + mechOidBytes.length);
    }
    public static int getMaxMechTokenSize(ObjectIdentifier mechOid,
                                          int maxTotalSize) {
        int mechOidBytesSize = 0;
        try {
            DerOutputStream temp = new DerOutputStream();
            temp.putOID(mechOid);
            mechOidBytesSize = temp.toByteArray().length;
        } catch (IOException e) {
        }
        maxTotalSize -= (1 + mechOidBytesSize);
        maxTotalSize -= 5;
        return maxTotalSize;
    }
    private int getLenFieldSize(int len) {
        int retVal = 1;
        if (len < 128) {
            retVal=1;
        } else if (len < (1 << 8)) {
            retVal=2;
        } else if (len < (1 << 16)) {
            retVal=3;
        } else if (len < (1 << 24)) {
            retVal=4;
        } else {
            retVal=5; 
        }
        return retVal;
    }
    public int encode(OutputStream os) throws IOException {
        int retVal = 1 + mechOidBytes.length;
        os.write(TOKEN_ID);
        int length = mechOidBytes.length + mechTokenLength;
        retVal += putLength(length, os);
        os.write(mechOidBytes);
        return retVal;
    }
    private int getLength(InputStream in) throws IOException {
        return getLength(in.read(), in);
    }
    private int getLength(int lenByte, InputStream in) throws IOException {
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
    private int putLength(int len, OutputStream out) throws IOException {
        int retVal = 0;
        if (len < 128) {
            out.write((byte)len);
            retVal=1;
        } else if (len < (1 << 8)) {
            out.write((byte)0x081);
            out.write((byte)len);
            retVal=2;
        } else if (len < (1 << 16)) {
            out.write((byte)0x082);
            out.write((byte)(len >> 8));
            out.write((byte)len);
            retVal=3;
        } else if (len < (1 << 24)) {
            out.write((byte)0x083);
            out.write((byte)(len >> 16));
            out.write((byte)(len >> 8));
            out.write((byte)len);
            retVal=4;
        } else {
            out.write((byte)0x084);
            out.write((byte)(len >> 24));
            out.write((byte)(len >> 16));
            out.write((byte)(len >> 8));
            out.write((byte)len);
            retVal=5;
        }
        return retVal;
    }
    private void debug(String str) {
        System.err.print(str);
    }
    private  String getHexBytes(byte[] bytes, int len)
        throws IOException {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < len; i++) {
            int b1 = (bytes[i]>>4) & 0x0f;
            int b2 = bytes[i] & 0x0f;
            sb.append(Integer.toHexString(b1));
            sb.append(Integer.toHexString(b2));
            sb.append(' ');
        }
        return sb.toString();
    }
}
