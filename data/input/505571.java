public class CertStatus
    extends ASN1Encodable
    implements ASN1Choice
{
    private int             tagNo;
    private DEREncodable    value;
    public CertStatus()
    {
        tagNo = 0;
        value = DERNull.THE_ONE;
    }
    public CertStatus(
        RevokedInfo info)
    {
        tagNo = 1;
        value = info;
    }
    public CertStatus(
        int tagNo,
        DEREncodable    value)
    {
        this.tagNo = tagNo;
        this.value = value;
    }
    public CertStatus(
        ASN1TaggedObject    choice)
    {
        this.tagNo = choice.getTagNo();
        switch (choice.getTagNo())
        {
        case 0:
            value = DERNull.THE_ONE;
            break;
        case 1:
            value = RevokedInfo.getInstance(choice, false);
            break;
        case 2:
            value = DERNull.THE_ONE;
        }
    }
    public static CertStatus getInstance(
        Object  obj)
    {
        if (obj == null || obj instanceof CertStatus)
        {
            return (CertStatus)obj;
        }
        else if (obj instanceof ASN1TaggedObject)
        {
            return new CertStatus((ASN1TaggedObject)obj);
        }
        throw new IllegalArgumentException("unknown object in factory");
    }
    public static CertStatus getInstance(
        ASN1TaggedObject obj,
        boolean          explicit)
    {
        return getInstance(obj.getObject()); 
    }
    public int getTagNo()
    {
        return tagNo;
    }
    public DEREncodable getStatus()
    {
        return value;
    }
    public DERObject toASN1Object()
    {
        return new DERTaggedObject(false, tagNo, value);
    }
}
