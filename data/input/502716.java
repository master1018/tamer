public class DERNull
    extends ASN1Null
{
    static public final DERNull THE_ONE = new DERNull();
    private static final byte[]  zeroBytes = new byte[0];
     DERNull()
    {
    }
    void encode(
        DEROutputStream  out)
        throws IOException
    {
        out.writeEncoded(NULL, zeroBytes);
    }
    public boolean equals(
        Object o)
    {
        if ((o == null) || !(o instanceof DERNull))
        {
            return false;
        }
        return true;
    }
    public int hashCode()
    {
        return 0;
    }
}
