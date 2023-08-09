public class PKIFreeText
    extends ASN1Encodable
{
    ASN1Sequence strings;
    public static PKIFreeText getInstance(
        ASN1TaggedObject    obj,
        boolean             explicit)
    {
        return getInstance(ASN1Sequence.getInstance(obj, explicit));
    }
    public static PKIFreeText getInstance(
        Object obj)
    {
        if (obj instanceof PKIFreeText)
        {
            return (PKIFreeText)obj;
        }
        else if (obj instanceof ASN1Sequence)
        {
            return new PKIFreeText((ASN1Sequence)obj);
        }
        throw new IllegalArgumentException("Unknown object in factory");
    }
    public PKIFreeText(
        ASN1Sequence seq)
    {
        Enumeration e = seq.getObjects();
        while (e.hasMoreElements())
        {
            if (!(e.nextElement() instanceof DERUTF8String))
            {
                throw new IllegalArgumentException("attempt to insert non UTF8 STRING into PKIFreeText");
            }
        }
        strings = seq;
    }
    public PKIFreeText(
        DERUTF8String p)
    {
        strings = new DERSequence(p);
    }
    public int size()
    {
        return strings.size();
    }
    public DERUTF8String getStringAt(
        int i)
    {
        return (DERUTF8String)strings.getObjectAt(i);
    }
    public DERObject toASN1Object()
    {
        return strings;
    }
}
