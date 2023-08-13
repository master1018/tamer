public class CCMParameters
    implements CipherParameters
{
    private byte[] associatedText;
    private byte[] nonce;
    private KeyParameter key;
    private int macSize;
    public CCMParameters(KeyParameter key, int macSize, byte[] nonce, byte[] associatedText)
    {
        this.key = key;
        this.nonce = nonce;
        this.macSize = macSize;
        this.associatedText = associatedText;
    }
    public KeyParameter getKey()
    {
        return key;
    }
    public int getMacSize()
    {
        return macSize;
    }
    public byte[] getAssociatedText()
    {
        return associatedText;
    }
    public byte[] getNonce()
    {
        return nonce;
    }
}
