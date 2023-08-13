public class ContentInfo {
    private static int[]  pkcs7 = {1, 2, 840, 113549, 1, 7};
    private static int[]   data = {1, 2, 840, 113549, 1, 7, 1};
    private static int[]  sdata = {1, 2, 840, 113549, 1, 7, 2};
    private static int[]  edata = {1, 2, 840, 113549, 1, 7, 3};
    private static int[] sedata = {1, 2, 840, 113549, 1, 7, 4};
    private static int[]  ddata = {1, 2, 840, 113549, 1, 7, 5};
    private static int[] crdata = {1, 2, 840, 113549, 1, 7, 6};
    private static int[] nsdata = {2, 16, 840, 1, 113730, 2, 5};
    private static int[] tstInfo = {1, 2, 840, 113549, 1, 9, 16, 1, 4};
    private static final int[] OLD_SDATA = {1, 2, 840, 1113549, 1, 7, 2};
    private static final int[] OLD_DATA = {1, 2, 840, 1113549, 1, 7, 1};
    public static ObjectIdentifier PKCS7_OID;
    public static ObjectIdentifier DATA_OID;
    public static ObjectIdentifier SIGNED_DATA_OID;
    public static ObjectIdentifier ENVELOPED_DATA_OID;
    public static ObjectIdentifier SIGNED_AND_ENVELOPED_DATA_OID;
    public static ObjectIdentifier DIGESTED_DATA_OID;
    public static ObjectIdentifier ENCRYPTED_DATA_OID;
    public static ObjectIdentifier OLD_SIGNED_DATA_OID;
    public static ObjectIdentifier OLD_DATA_OID;
    public static ObjectIdentifier NETSCAPE_CERT_SEQUENCE_OID;
    public static ObjectIdentifier TIMESTAMP_TOKEN_INFO_OID;
    static {
        PKCS7_OID =  ObjectIdentifier.newInternal(pkcs7);
        DATA_OID = ObjectIdentifier.newInternal(data);
        SIGNED_DATA_OID = ObjectIdentifier.newInternal(sdata);
        ENVELOPED_DATA_OID = ObjectIdentifier.newInternal(edata);
        SIGNED_AND_ENVELOPED_DATA_OID = ObjectIdentifier.newInternal(sedata);
        DIGESTED_DATA_OID = ObjectIdentifier.newInternal(ddata);
        ENCRYPTED_DATA_OID = ObjectIdentifier.newInternal(crdata);
        OLD_SIGNED_DATA_OID = ObjectIdentifier.newInternal(OLD_SDATA);
        OLD_DATA_OID = ObjectIdentifier.newInternal(OLD_DATA);
        NETSCAPE_CERT_SEQUENCE_OID = ObjectIdentifier.newInternal(nsdata);
        TIMESTAMP_TOKEN_INFO_OID = ObjectIdentifier.newInternal(tstInfo);
    }
    ObjectIdentifier contentType;
    DerValue content; 
    public ContentInfo(ObjectIdentifier contentType, DerValue content) {
        this.contentType = contentType;
        this.content = content;
    }
    public ContentInfo(byte[] bytes) {
        DerValue octetString = new DerValue(DerValue.tag_OctetString, bytes);
        this.contentType = DATA_OID;
        this.content = octetString;
    }
    public ContentInfo(DerInputStream derin)
        throws IOException, ParsingException
    {
        this(derin, false);
    }
    public ContentInfo(DerInputStream derin, boolean oldStyle)
        throws IOException, ParsingException
    {
        DerInputStream disType;
        DerInputStream disTaggedContent;
        DerValue type;
        DerValue taggedContent;
        DerValue[] typeAndContent;
        DerValue[] contents;
        typeAndContent = derin.getSequence(2);
        type = typeAndContent[0];
        disType = new DerInputStream(type.toByteArray());
        contentType = disType.getOID();
        if (oldStyle) {
            content = typeAndContent[1];
        } else {
            if (typeAndContent.length > 1) { 
                taggedContent = typeAndContent[1];
                disTaggedContent
                    = new DerInputStream(taggedContent.toByteArray());
                contents = disTaggedContent.getSet(1, true);
                content = contents[0];
            }
        }
    }
    public DerValue getContent() {
        return content;
    }
    public ObjectIdentifier getContentType() {
        return contentType;
    }
    public byte[] getData() throws IOException {
        if (contentType.equals(DATA_OID) ||
            contentType.equals(OLD_DATA_OID) ||
            contentType.equals(TIMESTAMP_TOKEN_INFO_OID)) {
            if (content == null)
                return null;
            else
                return content.getOctetString();
        }
        throw new IOException("content type is not DATA: " + contentType);
    }
    public void encode(DerOutputStream out) throws IOException {
        DerOutputStream contentDerCode;
        DerOutputStream seq;
        seq = new DerOutputStream();
        seq.putOID(contentType);
        if (content != null) {
            DerValue taggedContent = null;
            contentDerCode = new DerOutputStream();
            content.encode(contentDerCode);
            taggedContent = new DerValue((byte)0xA0,
                                         contentDerCode.toByteArray());
            seq.putDerValue(taggedContent);
        }
        out.write(DerValue.tag_Sequence, seq);
    }
    public byte[] getContentBytes() throws IOException {
        if (content == null)
            return null;
        DerInputStream dis = new DerInputStream(content.toByteArray());
        return dis.getOctetString();
    }
    public String toString() {
        String out = "";
        out += "Content Info Sequence\n\tContent type: " + contentType + "\n";
        out += "\tContent: " + content;
        return out;
    }
}
