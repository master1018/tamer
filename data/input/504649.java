public class SignerIdentifier
    extends ASN1Encodable
{
    private DEREncodable id;
    public SignerIdentifier(
        IssuerAndSerialNumber id)
    {
        this.id = id;
    }
    public SignerIdentifier(
        ASN1OctetString id)
    {
        this.id = new DERTaggedObject(false, 0, id);
    }
    public SignerIdentifier(
        DERObject id)
    {
        this.id = id;
    }
    public static SignerIdentifier getInstance(
        Object o)
    {
        if (o == null || o instanceof SignerIdentifier)
        {
            return (SignerIdentifier)o;
        }
        if (o instanceof IssuerAndSerialNumber)
        {
            return new SignerIdentifier((IssuerAndSerialNumber)o);
        }
        if (o instanceof ASN1OctetString)
        {
            return new SignerIdentifier((ASN1OctetString)o);
        }
        if (o instanceof DERObject)
        {
            return new SignerIdentifier((DERObject)o);
        }
        throw new IllegalArgumentException(
             "Illegal object in SignerIdentifier: " + o.getClass().getName());
    } 
    public boolean isTagged()
    {
        return (id instanceof ASN1TaggedObject);
    }
    public DEREncodable getId()
    {
        if (id instanceof ASN1TaggedObject)
        {
            return ASN1OctetString.getInstance((ASN1TaggedObject)id, false);
        }
        return id;
    }
    public DERObject toASN1Object()
    {
        return id.getDERObject();
    }
}
