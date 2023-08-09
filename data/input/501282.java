public class AttributeType {
    public final ObjectIdentifier oid;
    public final ASN1Type type;
    public AttributeType(ObjectIdentifier oid, ASN1Type type) {
        this.oid = oid;
        this.type = type;
    }
}
