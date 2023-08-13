public class OtherRecipientInfo
    extends ASN1Encodable
{
    private DERObjectIdentifier    oriType;
    private DEREncodable           oriValue;
    public OtherRecipientInfo(
        DERObjectIdentifier     oriType,
        DEREncodable            oriValue)
    {
        this.oriType = oriType;
        this.oriValue = oriValue;
    }
    public OtherRecipientInfo(
        ASN1Sequence seq)
    {
        oriType = DERObjectIdentifier.getInstance(seq.getObjectAt(1));
        oriValue = seq.getObjectAt(2);
    }
    public static OtherRecipientInfo getInstance(
        ASN1TaggedObject    obj,
        boolean             explicit)
    {
        return getInstance(ASN1Sequence.getInstance(obj, explicit));
    }
    public static OtherRecipientInfo getInstance(
        Object obj)
    {
        if (obj == null || obj instanceof OtherRecipientInfo)
        {
            return (OtherRecipientInfo)obj;
        }
        if (obj instanceof ASN1Sequence)
        {
            return new OtherRecipientInfo((ASN1Sequence)obj);
        }
        throw new IllegalArgumentException("Invalid OtherRecipientInfo: " + obj.getClass().getName());
    }
    public DERObjectIdentifier getType()
    {
        return oriType;
    }
    public DEREncodable getValue()
    {
        return oriValue;
    }
    public DERObject toASN1Object()
    {
        ASN1EncodableVector  v = new ASN1EncodableVector();
        v.add(oriType);
        v.add(oriValue);
        return new DERSequence(v);
    }
}
