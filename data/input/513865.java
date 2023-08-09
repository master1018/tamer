public final class ASN1Explicit extends ASN1Constructured {
    public final ASN1Type type;
    public ASN1Explicit(int tagNumber, ASN1Type type) {
        this(CLASS_CONTEXTSPECIFIC, tagNumber, type);
    }
    public ASN1Explicit(int tagClass, int tagNumber, ASN1Type type) {
        super(tagClass, tagNumber);
        this.type = type;
    }
    public Object decode(BerInputStream in) throws IOException {
        if (constrId != in.tag) {
            throw new ASN1Exception(
                    Messages.getString("security.13F", 
                    new Object[] { in.tagOffset, Integer.toHexString(constrId),
                            Integer.toHexString(in.tag) }));
        }
        in.next();
        in.content = type.decode(in);
        if (in.isVerify) {
            return null;
        }
        return getDecodedObject(in);
    }
    public void encodeContent(BerOutputStream out) {
        out.encodeExplicit(this);
    }
    public void setEncodingContent(BerOutputStream out) {
        out.getExplicitLength(this);
    }
    public String toString() {
        return super.toString() + " for type " + type; 
    }
}
