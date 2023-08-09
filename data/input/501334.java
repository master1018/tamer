public class KEKIdentifier
    extends ASN1Encodable
{
    private ASN1OctetString    keyIdentifier;
    private DERGeneralizedTime date;
    private OtherKeyAttribute  other;
    public KEKIdentifier(
        byte[]              keyIdentifier,
        DERGeneralizedTime  date,
        OtherKeyAttribute   other)
    {
        this.keyIdentifier = new DEROctetString(keyIdentifier);
        this.date = date;
        this.other = other;
    }
    public KEKIdentifier(
        ASN1Sequence seq)
    {
        keyIdentifier = (ASN1OctetString)seq.getObjectAt(0);
        switch (seq.size())
        {
        case 1:
            break;
        case 2:
            if (seq.getObjectAt(1) instanceof DERGeneralizedTime)
            {
                date = (DERGeneralizedTime)seq.getObjectAt(1); 
            }
            else
            {
                other = OtherKeyAttribute.getInstance(seq.getObjectAt(1));
            }
            break;
        case 3:
            date  = (DERGeneralizedTime)seq.getObjectAt(1);
            other = OtherKeyAttribute.getInstance(seq.getObjectAt(2));
            break;
        default:
                throw new IllegalArgumentException("Invalid KEKIdentifier");
        }
    }
    public static KEKIdentifier getInstance(
        ASN1TaggedObject obj,
        boolean explicit)
    {
        return getInstance(ASN1Sequence.getInstance(obj, explicit));
    }
    public static KEKIdentifier getInstance(
        Object obj)
    {
        if (obj == null || obj instanceof KEKIdentifier)
        {
            return (KEKIdentifier)obj;
        }
        if (obj instanceof ASN1Sequence)
        {
            return new KEKIdentifier((ASN1Sequence)obj);
        }
        throw new IllegalArgumentException("Invalid KEKIdentifier: " + obj.getClass().getName());
    }
    public ASN1OctetString getKeyIdentifier()
    {
        return keyIdentifier;
    }
    public DERGeneralizedTime getDate()
    {
        return date;
    }
    public OtherKeyAttribute getOther()
    {
        return other;
    }
    public DERObject toASN1Object()
    {
        ASN1EncodableVector  v = new ASN1EncodableVector();
        v.add(keyIdentifier);
        if (date != null)
        {
            v.add(date);
        }
        if (other != null)
        {
            v.add(other);
        }
        return new DERSequence(v);
    }
}
