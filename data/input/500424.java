public class X509KeyUsage
    extends ASN1Encodable
{
    public static final int        digitalSignature = 1 << 7; 
    public static final int        nonRepudiation   = 1 << 6;
    public static final int        keyEncipherment  = 1 << 5;
    public static final int        dataEncipherment = 1 << 4;
    public static final int        keyAgreement     = 1 << 3;
    public static final int        keyCertSign      = 1 << 2;
    public static final int        cRLSign          = 1 << 1;
    public static final int        encipherOnly     = 1 << 0;
    public static final int        decipherOnly     = 1 << 15;
    private int usage = 0;
    public X509KeyUsage(
        int usage)
    {
        this.usage = usage;
    }
    public DERObject toASN1Object()
    {
        return new KeyUsage(usage);
    }
}
