class AuthenticatedAttributes {
    private byte[] encoding;
    private List authenticatedAttributes;
    public AuthenticatedAttributes(byte[] encoding, List authenticatedAttributes) {
        this.encoding = encoding;
        this.authenticatedAttributes = authenticatedAttributes;
    }
    public List getAttributes() {
        return authenticatedAttributes;
    }
    public byte[] getEncoded() {
        if (encoding == null) {
            encoding = ASN1.encode(this);
        }
        return encoding;
    }
    public static final ASN1SetOf ASN1 =
        new ASN1SetOf(AttributeTypeAndValue.ASN1) {
        public Object getDecodedObject(BerInputStream in) {
            return new AuthenticatedAttributes(in.getEncoded(),
                    (List) in.content);
        }
    };
}
