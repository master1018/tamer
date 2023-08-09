public class BERSet
    extends DERSet
{
    public BERSet()
    {
    }
    public BERSet(
        DEREncodable    obj)
    {
        super(obj);
    }
    public BERSet(
        DEREncodableVector   v)
    {
        super(v, false);
    }
    BERSet(
        DEREncodableVector   v,
        boolean              needsSorting)
    {
        super(v, needsSorting);
    }
    void encode(
        DEROutputStream out)
        throws IOException
    {
        if (out instanceof ASN1OutputStream || out instanceof BEROutputStream)
        {
            out.write(SET | CONSTRUCTED);
            out.write(0x80);
            Enumeration e = getObjects();
            while (e.hasMoreElements())
            {
                out.writeObject(e.nextElement());
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
