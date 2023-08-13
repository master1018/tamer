public final class DerInputStream extends BerInputStream {
    public DerInputStream(byte[] encoded) throws IOException {
        super(encoded, 0, encoded.length);
    }
    public DerInputStream(byte[] encoded, int offset, int encodingLen)
            throws IOException {
        super(encoded, offset, encodingLen);
    }
    public DerInputStream(InputStream in) throws IOException {
        super(in);
    }
    public final int next() throws IOException {
        int tag = super.next();
        if (length == INDEFINIT_LENGTH) {
            throw new ASN1Exception(Messages.getString("security.105")); 
        }
        return tag;
    }
    private static final byte[] UNUSED_BITS_MASK = new byte[] { 0x01, 0x03,
            0x07, 0x0F, 0x1F, 0x3F, 0x7F };
    public void readBitString() throws IOException {
        if (tag == ASN1Constants.TAG_C_BITSTRING) {
            throw new ASN1Exception(Messages.getString("security.106", tagOffset)); 
        }
        super.readBitString();
        if (length > 1
                && buffer[contentOffset] != 0
                && (buffer[offset - 1] & UNUSED_BITS_MASK[buffer[contentOffset] - 1]) != 0) {
            throw new ASN1Exception(Messages.getString("security.107", contentOffset)); 
        }
    }
    public void readBoolean() throws IOException {
        super.readBoolean();
        if (buffer[contentOffset] != 0 && buffer[contentOffset] != (byte) 0xFF) {
            throw new ASN1Exception(Messages.getString("security.108", contentOffset)); 
        }
    }
    public void readOctetString() throws IOException {
        if (tag == ASN1Constants.TAG_C_OCTETSTRING) {
            throw new ASN1Exception(
                    Messages.getString("security.109", tagOffset)); 
        }
        super.readOctetString();
    }
    public void readSequence(ASN1Sequence sequence) throws IOException {
        super.readSequence(sequence);
    }
    public void readSetOf(ASN1SetOf setOf) throws IOException {
        super.readSetOf(setOf);
    }
    public void readString(ASN1StringType type) throws IOException {
        if (tag == type.constrId) {
            throw new ASN1Exception(Messages.getString("security.10A", tagOffset)); 
        }
        super.readString(type);
    }
    public void readUTCTime() throws IOException {
        if (tag == ASN1Constants.TAG_C_UTCTIME) {
            throw new ASN1Exception(Messages.getString("security.10B", tagOffset)); 
        }
        if (length != ASN1UTCTime.UTC_HMS) {
            throw new ASN1Exception(Messages.getString("security.10C", tagOffset)); 
        }
        super.readUTCTime();
    }
    public void readGeneralizedTime() throws IOException {
        if (tag == ASN1Constants.TAG_C_GENERALIZEDTIME) {
            throw new ASN1Exception(Messages.getString("security.10D", tagOffset)); 
        }
        super.readGeneralizedTime();
    }
}
