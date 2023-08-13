public class AccessDescription {
    private final String accessMethod;
    private final GeneralName accessLocation;
    private byte [] encoding;
    public AccessDescription(String accessMethod, GeneralName accessLocation) {
        this.accessMethod = accessMethod;
        this.accessLocation = accessLocation;
    }
    private AccessDescription(String accessMethod, GeneralName accessLocation,
            byte[] encoding) {
        this.accessMethod = accessMethod;
        this.accessLocation = accessLocation;
        this.encoding = encoding;
    }
    public byte[] getEncoded() {
        if (encoding == null) {
            encoding = ASN1.encode(this);
        }
        return encoding;
    }
    public String toString() {
        StringBuilder res = new StringBuilder();
        res.append("\n-- AccessDescription:"); 
        res.append("\naccessMethod:  "); 
        res.append(accessMethod);
        res.append("\naccessLocation:  "); 
        res.append(accessLocation);
        res.append("\n-- AccessDescription END\n"); 
        return res.toString();
    }
    public GeneralName getAccessLocation() {
        return accessLocation;
    }
    public String getAccessMethod() {
        return accessMethod;
    }
    public static final ASN1Sequence ASN1 = new ASN1Sequence(new ASN1Type[] {
            ASN1Oid.getInstance(), 
            GeneralName.ASN1 }) {
        protected Object getDecodedObject(BerInputStream in) {
            Object[] values = (Object[]) in.content;
            return new AccessDescription(
                    ObjectIdentifier.toString((int[]) values[0]), 
                    (GeneralName) values[1], in.getEncoded());
        }
        protected void getValues(Object object, Object[] values) {
            AccessDescription ad = (AccessDescription) object;
            values[0] = ObjectIdentifier.toIntArray(ad.accessMethod);
            values[1] = ad.accessLocation;
        }
    };
}
