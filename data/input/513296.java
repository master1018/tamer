public class OriginatorInfo
    extends ASN1Encodable
{
    private ASN1Set certs;
    private ASN1Set crls;
    public OriginatorInfo(
        ASN1Set certs,
        ASN1Set crls)
    {
        this.certs = certs;
        this.crls = crls;
    }
    public OriginatorInfo(
        ASN1Sequence seq)
    {
        switch (seq.size())
        {
        case 0:     
            break;
        case 1:
            ASN1TaggedObject o = (ASN1TaggedObject)seq.getObjectAt(0);
            switch (o.getTagNo())
            {
            case 0 :
                certs = ASN1Set.getInstance(o, false);
                break;
            case 1 :
                crls = ASN1Set.getInstance(o, false);
                break;
            default:
                throw new IllegalArgumentException("Bad tag in OriginatorInfo: " + o.getTagNo());
            }
            break;
        case 2:
            certs = ASN1Set.getInstance((ASN1TaggedObject)seq.getObjectAt(0), false);
            crls  = ASN1Set.getInstance((ASN1TaggedObject)seq.getObjectAt(1), false);
            break;
        default:
            throw new IllegalArgumentException("OriginatorInfo too big");
        }
    }
    public static OriginatorInfo getInstance(
        ASN1TaggedObject    obj,
        boolean             explicit)
    {
        return getInstance(ASN1Sequence.getInstance(obj, explicit));
    }
    public static OriginatorInfo getInstance(
        Object obj)
    {
        if (obj == null || obj instanceof OriginatorInfo)
        {
            return (OriginatorInfo)obj;
        }
        if (obj instanceof ASN1Sequence)
        {
            return new OriginatorInfo((ASN1Sequence)obj);
        }
        throw new IllegalArgumentException("Invalid OriginatorInfo: " + obj.getClass().getName());
    }
    public ASN1Set getCertificates()
    {
        return certs;
    }
    public ASN1Set getCRLs()
    {
        return crls;
    }
    public DERObject toASN1Object()
    {
        ASN1EncodableVector  v = new ASN1EncodableVector();
        if (certs != null)
        {
            v.add(new DERTaggedObject(false, 0, certs));
        }
        if (crls != null)
        {
            v.add(new DERTaggedObject(false, 1, crls));
        }
        return new DERSequence(v);
    }
}
