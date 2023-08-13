public class DERConstructedSet
    extends ASN1Set
{
    public DERConstructedSet()
    {
    }
    public DERConstructedSet(
        DEREncodable   obj)
    {
        this.addObject(obj);
    }
    public DERConstructedSet(
        DEREncodableVector   v)
    {
        for (int i = 0; i != v.size(); i++)
        {
            this.addObject(v.get(i));
        }
    }
    public void addObject(
        DEREncodable    obj)
    {
        super.addObject(obj);
    }
    public int getSize()
    {
        return size();
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
