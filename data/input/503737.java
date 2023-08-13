public class ASN1Implicit extends ASN1Type {
    private static final int TAGGING_PRIMITIVE = 0;
    private static final int TAGGING_CONSTRUCTED = 1;
    private static final int TAGGING_STRING = 2;
    private final ASN1Type type;
    private final int taggingType;
    public ASN1Implicit(int tagNumber, ASN1Type type) {
        this(CLASS_CONTEXTSPECIFIC, tagNumber, type);
    }
    public ASN1Implicit(int tagClass, int tagNumber, ASN1Type type) {
        super(tagClass, tagNumber);
        if ((type instanceof ASN1Choice) || (type instanceof ASN1Any)) {
            throw new IllegalArgumentException(
                    Messages.getString("security.9F")); 
        }
        this.type = type;
        if (type.checkTag(type.id)) {
            if (type.checkTag(type.constrId)) {
                taggingType = TAGGING_STRING;
            } else {
                taggingType = TAGGING_PRIMITIVE;
            }
        } else {
            taggingType = TAGGING_CONSTRUCTED;
        }
    }
    public final boolean checkTag(int identifier) {
        switch (taggingType) {
        case TAGGING_PRIMITIVE:
            return id == identifier;
        case TAGGING_CONSTRUCTED:
            return constrId == identifier;
        default: 
            return id == identifier || constrId == identifier;
        }
    }
    public Object decode(BerInputStream in) throws IOException {
        if (!checkTag(in.tag)) {
            throw new ASN1Exception(Messages.getString("security.100", 
                    new Object[] { in.tagOffset, Integer.toHexString(id),
                            Integer.toHexString(in.tag) }));
        }
        if (id == in.tag) {
            in.tag = type.id;
        } else {
            in.tag = type.constrId;
        }
        in.content = type.decode(in);
        if (in.isVerify) {
            return null;
        }
        return getDecodedObject(in);
    }
    public void encodeASN(BerOutputStream out) {
        if (taggingType == TAGGING_CONSTRUCTED) {
            out.encodeTag(constrId);
        } else {
            out.encodeTag(id);
        }
        encodeContent(out);
    }
    public void encodeContent(BerOutputStream out) {
        type.encodeContent(out);
    }
    public void setEncodingContent(BerOutputStream out) {
        type.setEncodingContent(out);
    }
}
