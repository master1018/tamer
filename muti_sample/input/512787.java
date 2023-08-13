public abstract class ASN1Type implements ASN1Constants {
    public final int id;
    public final int constrId;
    public ASN1Type(int tagNumber) {
        this(CLASS_UNIVERSAL, tagNumber);
    }
    public ASN1Type(int tagClass, int tagNumber) {
        if (tagNumber < 0) {
            throw new IllegalArgumentException(Messages.getString("security.102")); 
        }
        if (tagClass != CLASS_UNIVERSAL && tagClass != CLASS_APPLICATION
                && tagClass != CLASS_CONTEXTSPECIFIC
                && tagClass != CLASS_PRIVATE) {
            throw new IllegalArgumentException(Messages.getString("security.103")); 
        }
        if (tagNumber < 31) {
            this.id = tagClass + tagNumber;
        } else {
            throw new IllegalArgumentException(
                    Messages.getString("security.104")); 
        }
        this.constrId = this.id + PC_CONSTRUCTED;
    }
    public final Object decode(byte[] encoded) throws IOException {
        return decode(new DerInputStream(encoded));
    }
    public final Object decode(byte[] encoded, int offset, int encodingLen)
            throws IOException {
        return decode(new DerInputStream(encoded, offset, encodingLen));
    }
    public final Object decode(InputStream in) throws IOException {
        return decode(new DerInputStream(in));
    }
    public final void verify(byte[] encoded) throws IOException {
        DerInputStream decoder = new DerInputStream(encoded);
        decoder.setVerify();
        decode(decoder);
    }
    public final void verify(InputStream in) throws IOException {
        DerInputStream decoder = new DerInputStream(in);
        decoder.setVerify();
        decode(decoder);
    }
    public final byte[] encode(Object object) {
        DerOutputStream out = new DerOutputStream(this, object);
        return out.encoded;
    }
    public abstract Object decode(BerInputStream in) throws IOException;
    public abstract boolean checkTag(int identifier);
    protected Object getDecodedObject(BerInputStream in) throws IOException {
        return in.content;
    }
    public abstract void encodeASN(BerOutputStream out);
    public abstract void encodeContent(BerOutputStream out);
    public abstract void setEncodingContent(BerOutputStream out);
    public int getEncodedLength(BerOutputStream out) { 
        int len = 1; 
        len++;
        if (out.length > 127) {
            len++;
            for (int cur = out.length >> 8; cur > 0; len++) {
                cur = cur >> 8;
            }
        }
        len += out.length;
        return len;
    }
    public String toString() {
        return this.getClass().getName() + "(tag: 0x" 
                + Integer.toHexString(0xff & this.id) + ")"; 
    }
}
