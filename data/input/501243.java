public class DERSet
    extends ASN1Set
{
    public DERSet()
    {
    }
    public DERSet(
        DEREncodable   obj)
    {
        this.addObject(obj);
    }
    public DERSet(
        DEREncodableVector   v)
    {
        this(v, true);
    }
    public DERSet(
        ASN1Encodable[]   a)
    {
        for (int i = 0; i != a.length; i++)
        {
            this.addObject(a[i]);
        }
        this.sort();
    }
    DERSet(
        DEREncodableVector   v,
        boolean              needsSorting)
    {
        for (int i = 0; i != v.size(); i++)
        {
            this.addObject(v.get(i));
        }
        if (needsSorting)
        {
            this.sort();
        }
    }
    void encode(
        DEROutputStream out)
        throws IOException
    {
        ByteArrayOutputStream   bOut = new ByteArrayOutputStream();
        DEROutputStream         dOut = new DEROutputStream(bOut);
        Enumeration             e = this.getObjects();
        while (e.hasMoreElements())
        {
            Object    obj = e.nextElement();
            dOut.writeObject(obj);
        }
        dOut.close();
        byte[]  bytes = bOut.toByteArray();
        out.writeEncoded(SET | CONSTRUCTED, bytes);
    }
}
