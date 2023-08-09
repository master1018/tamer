public abstract class ASN1Encodable
    implements DEREncodable
{
    public static final String DER = "DER";
    public static final String BER = "BER";
    public byte[] getEncoded() 
        throws IOException
    {
        ByteArrayOutputStream   bOut = new ByteArrayOutputStream();
        ASN1OutputStream        aOut = new ASN1OutputStream(bOut);
        aOut.writeObject(this);
        return bOut.toByteArray();
    }
    public byte[] getEncoded(
        String encoding) 
        throws IOException
    {
        if (encoding.equals(DER))
        {
            ByteArrayOutputStream   bOut = new ByteArrayOutputStream();
            DEROutputStream         dOut = new DEROutputStream(bOut);
            dOut.writeObject(this);
            return bOut.toByteArray();
        }
        return this.getEncoded();
    }
    public byte[] getDEREncoded()
    {
        try
        {
            return this.getEncoded(DER);
        }
        catch (IOException e)
        {
            return null;
        }
    }
    public int hashCode()
    {
        return this.toASN1Object().hashCode();
    }
    public boolean equals(
        Object  o)
    {
        if ((o == null) || !(o instanceof DEREncodable))
        {
            return false;
        }
        DEREncodable other = (DEREncodable)o;
        return this.toASN1Object().equals(other.getDERObject());
    }
    public DERObject getDERObject()
    {        
        return this.toASN1Object();
    }
    public abstract DERObject toASN1Object();
}
