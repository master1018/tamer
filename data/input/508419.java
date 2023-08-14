public class OtherKeyAttribute
    extends ASN1Encodable
{
    private DERObjectIdentifier keyAttrId;
    private DEREncodable        keyAttr;
    public static OtherKeyAttribute getInstance(
        Object o)
    {
        if (o == null || o instanceof OtherKeyAttribute)
        {
            return (OtherKeyAttribute)o;
        }
        if (o instanceof ASN1Sequence)
        {
            return new OtherKeyAttribute((ASN1Sequence)o);
        }
        throw new IllegalArgumentException("unknown object in factory");
    }
    public OtherKeyAttribute(
        ASN1Sequence seq)
    {
        keyAttrId = (DERObjectIdentifier)seq.getObjectAt(0);
        keyAttr = seq.getObjectAt(1);
    }
    public OtherKeyAttribute(
        DERObjectIdentifier keyAttrId,
        DEREncodable        keyAttr)
    {
        this.keyAttrId = keyAttrId;
        this.keyAttr = keyAttr;
    }
    public DERObjectIdentifier getKeyAttrId()
    {
        return keyAttrId;
    }
    public DEREncodable getKeyAttr()
    {
        return keyAttr;
    }
    public DERObject toASN1Object()
    {
        ASN1EncodableVector v = new ASN1EncodableVector();
        v.add(keyAttrId);
        v.add(keyAttr);
        return new DERSequence(v);
    }
}
