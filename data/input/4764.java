public class PKCS10Attribute implements DerEncoder {
    protected ObjectIdentifier  attributeId = null;
    protected Object            attributeValue = null;
    public PKCS10Attribute(DerValue derVal) throws IOException {
        PKCS9Attribute attr = new PKCS9Attribute(derVal);
        this.attributeId = attr.getOID();
        this.attributeValue = attr.getValue();
    }
    public PKCS10Attribute(ObjectIdentifier attributeId,
                           Object attributeValue) {
        this.attributeId = attributeId;
        this.attributeValue = attributeValue;
    }
    public PKCS10Attribute(PKCS9Attribute attr) {
        this.attributeId = attr.getOID();
        this.attributeValue = attr.getValue();
    }
    public void derEncode(OutputStream out) throws IOException {
        PKCS9Attribute attr = new PKCS9Attribute(attributeId, attributeValue);
        attr.derEncode(out);
    }
    public ObjectIdentifier getAttributeId() {
        return (attributeId);
    }
    public Object getAttributeValue() {
        return (attributeValue);
    }
    public String toString() {
        return (attributeValue.toString());
    }
}
