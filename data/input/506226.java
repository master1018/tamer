public class X509Attribute
    extends ASN1Encodable
{
    Attribute    attr;
    X509Attribute(
        ASN1Encodable   at)
    {
        this.attr = Attribute.getInstance(at);
    }
    public X509Attribute(
        String          oid,
        ASN1Encodable   value)
    {
        this.attr = new Attribute(new DERObjectIdentifier(oid), new DERSet(value));
    }
    public X509Attribute(
        String              oid,
        ASN1EncodableVector value)
    {
        this.attr = new Attribute(new DERObjectIdentifier(oid), new DERSet(value));
    }
    public String getOID()
    {
        return attr.getAttrType().getId();
    }
    public ASN1Encodable[] getValues()
    {
        ASN1Set         s = attr.getAttrValues();
        ASN1Encodable[] values = new ASN1Encodable[s.size()];
        for (int i = 0; i != s.size(); i++)
        {
            values[i] = (ASN1Encodable)s.getObjectAt(i);
        }
        return values;
    }
    public DERObject toASN1Object()
    {
        return attr.toASN1Object();
    }
}
