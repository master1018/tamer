public class BERNull
    extends DERNull
{
    static public final BERNull THE_ONE = new BERNull();
    private BERNull()
    {
    }
    void encode(
        DEROutputStream  out)
        throws IOException
    {
        if (out instanceof ASN1OutputStream || out instanceof BEROutputStream)
        {
            out.write(NULL);
        }
        else
        {
            super.encode(out);
        }
    }
}
