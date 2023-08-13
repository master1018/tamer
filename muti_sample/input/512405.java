public class StreamBlockCipher
    implements StreamCipher
{
    private BlockCipher  cipher;
    private byte[]  oneByte = new byte[1];
    public StreamBlockCipher(
        BlockCipher cipher)
    {
        if (cipher.getBlockSize() != 1)
        {
            throw new IllegalArgumentException("block cipher block size != 1.");
        }
        this.cipher = cipher;
    }
    public void init(
        boolean forEncryption,
        CipherParameters params)
    {
        cipher.init(forEncryption, params);
    }
    public String getAlgorithmName()
    {
        return cipher.getAlgorithmName();
    }
    public byte returnByte(
        byte    in)
    {
        oneByte[0] = in;
        cipher.processBlock(oneByte, 0, oneByte, 0);
        return oneByte[0];
    }
    public void processBytes(
        byte[]  in,
        int     inOff,
        int     len,
        byte[]  out,
        int     outOff)
        throws DataLengthException
    {
        if (outOff + len > out.length)
        {
            throw new DataLengthException("output buffer too small in processBytes()");
        }
        for (int i = 0; i != len; i++)
        {
                cipher.processBlock(in, inOff + i, out, outOff + i);
        }
    }
    public void reset()
    {
        cipher.reset();
    }
}
