public class X509Principal
    extends X509Name
    implements Principal
{
    private static ASN1Sequence readSequence(
        ASN1InputStream aIn)
        throws IOException
    {
        try
        {
            return ASN1Sequence.getInstance(aIn.readObject());
        }
        catch (IllegalArgumentException e)
        {
            throw new IOException("not an ASN.1 Sequence: " + e);
        }
    }
    public X509Principal(
        byte[]  bytes)
        throws IOException
    {
        super(readSequence(new ASN1InputStream(bytes)));
    }
    public X509Principal(
        X509Name  name)
    {
        super((ASN1Sequence)name.getDERObject());
    }
    public X509Principal(
        Hashtable  attributes)
    {
        super(attributes);
    }
    public X509Principal(
        Vector      ordering,
        Hashtable   attributes)
    {
        super(ordering, attributes);
    }
    public X509Principal(
        Vector      oids,
        Vector      values)
    {
        super(oids, values);
    }
    public X509Principal(
        String  dirName)
    {
        super(dirName);
    }
    public X509Principal(
        boolean reverse,
        String  dirName)
    {
        super(reverse, dirName);
    }
    public X509Principal(
        boolean     reverse,
        Hashtable   lookUp,
        String      dirName)
    {
        super(reverse, lookUp, dirName);
    }
    public String getName()
    {
        return this.toString();
    }
    public byte[] getEncoded()
    {
        ByteArrayOutputStream   bOut = new ByteArrayOutputStream();
        DEROutputStream         dOut = new DEROutputStream(bOut);
        try
        {
            dOut.writeObject(this);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e.toString());
        }
        return bOut.toByteArray();
    }
}
