public class BERTaggedObject
    extends DERTaggedObject
{
    public BERTaggedObject(
        int             tagNo,
        DEREncodable    obj)
    {
        super(tagNo, obj);
    }
    public BERTaggedObject(
        boolean         explicit,
        int             tagNo,
        DEREncodable    obj)
    {
        super(explicit, tagNo, obj);
    }
    public BERTaggedObject(
        int             tagNo)
    {
        super(false, tagNo, new BERSequence());
    }
    void encode(
        DEROutputStream  out)
        throws IOException
    {
        if (out instanceof ASN1OutputStream || out instanceof BEROutputStream)
        {
            out.write(CONSTRUCTED | TAGGED | tagNo);
            out.write(0x80);
            if (!empty)
            {
                if (!explicit)
                {
                    if (obj instanceof ASN1OctetString)
                    {
                        Enumeration  e;
                        if (obj instanceof BERConstructedOctetString)
                        {
                            e = ((BERConstructedOctetString)obj).getObjects();
                        }
                        else
                        {
                            ASN1OctetString             octs = (ASN1OctetString)obj;
                            BERConstructedOctetString   berO = new BERConstructedOctetString(octs.getOctets());
                            e = berO.getObjects();
                        }
                        while (e.hasMoreElements())
                        {
                            out.writeObject(e.nextElement());
                        }
                    }
                    else if (obj instanceof ASN1Sequence)
                    {
                        Enumeration  e = ((ASN1Sequence)obj).getObjects();
                        while (e.hasMoreElements())
                        {
                            out.writeObject(e.nextElement());
                        }
                    }
                    else if (obj instanceof ASN1Set)
                    {
                        Enumeration  e = ((ASN1Set)obj).getObjects();
                        while (e.hasMoreElements())
                        {
                            out.writeObject(e.nextElement());
                        }
                    }
                    else
                    {
                        throw new RuntimeException("not implemented: " + obj.getClass().getName());
                    }
                }
                else
                {
                    out.writeObject(obj);
                }
            }
            out.write(0x00);
            out.write(0x00);
        }
        else
        {
            super.encode(out);
        }
    }
}
