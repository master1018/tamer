public class ContentIdentifier
    extends ASN1Encodable
{
     ASN1OctetString value;
    public static ContentIdentifier getInstance(Object o)
    {
        if (o == null || o instanceof ContentIdentifier)
        {
            return (ContentIdentifier) o;
        }
        else if (o instanceof ASN1OctetString)
        {
            return new ContentIdentifier((ASN1OctetString) o);
        }
        throw new IllegalArgumentException(
                "unknown object in 'ContentIdentifier' factory : "
                        + o.getClass().getName() + ".");
    }
    public ContentIdentifier(
        ASN1OctetString value)
    {
        this.value = value;
    }
    public ContentIdentifier(
        byte[] value)
    {
        this(new DEROctetString(value));
    }
    public ASN1OctetString getValue()
    {
        return value;
    }
    public DERObject toASN1Object()
    {
        return value;
    }
}
