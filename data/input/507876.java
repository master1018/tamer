public class NullEngine implements BlockCipher
{
    protected static final int BLOCK_SIZE = 1;
    public NullEngine()
    {
        super();
    }
    public void init(boolean forEncryption, CipherParameters params) throws IllegalArgumentException
    {
    }
    public String getAlgorithmName()
    {
        return "Null";
    }
    public int getBlockSize()
    {
        return BLOCK_SIZE;
    }
    public int processBlock(byte[] in, int inOff, byte[] out, int outOff)
        throws DataLengthException, IllegalStateException
    {
            if ((inOff + BLOCK_SIZE) > in.length)
            {
                throw new DataLengthException("input buffer too short");
            }
            if ((outOff + BLOCK_SIZE) > out.length)
            {
                throw new DataLengthException("output buffer too short");
            }
            for (int i = 0; i < BLOCK_SIZE; ++i)
            {
                out[outOff + i] = in[inOff + i];
            }
            return BLOCK_SIZE;
    }
    public void reset()
    {
    }
}
