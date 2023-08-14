public class IEKeySpec
    implements KeySpec, IESKey
{
    private PublicKey   pubKey;
    private PrivateKey  privKey;
    public IEKeySpec(
        PrivateKey  privKey,
        PublicKey   pubKey)
    {
        this.privKey = privKey;
        this.pubKey = pubKey;
    }
    public PublicKey getPublic()
    {
        return pubKey;
    }
    public PrivateKey getPrivate()
    {
        return privKey;
    }
    public String getAlgorithm()
    {
        return "IES";
    }
    public String getFormat()
    {
        return null;
    }
    public byte[] getEncoded()
    {
        return null;
    }
}
