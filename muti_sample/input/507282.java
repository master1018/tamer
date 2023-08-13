public class IESParameterSpec
    implements AlgorithmParameterSpec
{
    private byte[]  derivation;
    private byte[]  encoding;
    private int     macKeySize;
    public IESParameterSpec(
        byte[]  derivation,
        byte[]  encoding,
        int     macKeySize)
    {
        this.derivation = new byte[derivation.length];
        System.arraycopy(derivation, 0, this.derivation, 0, derivation.length);
        this.encoding = new byte[encoding.length];
        System.arraycopy(encoding, 0, this.encoding, 0, encoding.length);
        this.macKeySize = macKeySize;           
    }
    public byte[] getDerivationV()
    {
        return derivation;
    }
    public byte[] getEncodingV()
    {
        return encoding;
    }
    public int getMacKeySize()
    {
        return macKeySize;
    }
}
