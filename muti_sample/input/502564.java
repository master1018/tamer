public class OriginatorIdentifierOrKey
    extends ASN1Encodable
{
    private DEREncodable id;
    public OriginatorIdentifierOrKey(
        IssuerAndSerialNumber id)
    {
        this.id = id;
    }
    public OriginatorIdentifierOrKey(
        ASN1OctetString id)
    {
        this.id = new DERTaggedObject(false, 0, id);
    }
    public OriginatorIdentifierOrKey(
        OriginatorPublicKey id)
    {
        this.id = new DERTaggedObject(false, 1, id);
    }
    public OriginatorIdentifierOrKey(
        DERObject id)
    {
        this.id = id;
    }
    public static OriginatorIdentifierOrKey getInstance(
        ASN1TaggedObject    o,
        boolean             explicit)
    {
        if (!explicit)
        {
            throw new IllegalArgumentException(
                    "Can't implicitly tag OriginatorIdentifierOrKey");
        }
        return getInstance(o.getObject());
    }
    public static OriginatorIdentifierOrKey getInstance(
        Object o)
    {
        if (o == null || o instanceof OriginatorIdentifierOrKey)
        {
            return (OriginatorIdentifierOrKey)o;
        }
        if (o instanceof DERObject)
        {
            return new OriginatorIdentifierOrKey((DERObject)o);
        }
        throw new IllegalArgumentException("Invalid OriginatorIdentifierOrKey: " + o.getClass().getName());
    } 
    public DEREncodable getId()
    {
        return id;
    }
    public DERObject toASN1Object()
    {
        return id.getDERObject();
    }
}
