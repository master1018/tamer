public class DERTaggedObject
    extends ASN1TaggedObject
{
    public DERTaggedObject(
        int             tagNo,
        DEREncodable    obj)
    {
        super(tagNo, obj);
    }
    public DERTaggedObject(
        boolean         explicit,
        int             tagNo,
        DEREncodable    obj)
    {
        super(explicit, tagNo, obj);
    }
    public DERTaggedObject(
        int             tagNo)
    {
        super(false, tagNo, new DERSequence());
    }
    void encode(
        DEROutputStream  out)
        throws IOException
    {
        if (!empty)
        {
            ByteArrayOutputStream   bOut = new ByteArrayOutputStream();
            DEROutputStream         dOut = new DEROutputStream(bOut);
            dOut.writeObject(obj);
            dOut.close();
            byte[]  bytes = bOut.toByteArray();
            if (explicit)
            {
                out.writeEncoded(CONSTRUCTED | TAGGED | tagNo, bytes);
            }
            else
            {
                if ((bytes[0] & CONSTRUCTED) != 0)
                {
                    bytes[0] = (byte)(CONSTRUCTED | TAGGED | tagNo);
                }
                else
                {
                    bytes[0] = (byte)(TAGGED | tagNo);
                }
                out.write(bytes);
            }
        }
        else
        {
            out.writeEncoded(CONSTRUCTED | TAGGED | tagNo, new byte[0]);
        }
    }
}
