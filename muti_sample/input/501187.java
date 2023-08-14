public class DEROctetString
    extends ASN1OctetString
{
    public DEROctetString(
        byte[]  string)
    {
        super(string);
    }
    public DEROctetString(
        DEREncodable  obj)
    {
        super(obj);
    }
    void encode(
        DEROutputStream out)
        throws IOException
    {
        out.writeEncoded(OCTET_STRING, string);
    }
}
