public class SICBlockCipher implements BlockCipher
{
    private final BlockCipher     cipher;
    private final int             blockSize;
    private byte[]          IV;
    private byte[]          counter;
    private byte[]          counterOut;
    public SICBlockCipher(BlockCipher c)
    {
        this.cipher = c;
        this.blockSize = cipher.getBlockSize();
        this.IV = new byte[blockSize];
        this.counter = new byte[blockSize];
        this.counterOut = new byte[blockSize];
    }
    public BlockCipher getUnderlyingCipher()
    {
        return cipher;
    }
    public void init(
        boolean             forEncryption, 
        CipherParameters    params)
        throws IllegalArgumentException
    {
        if (params instanceof ParametersWithIV)
        {
          ParametersWithIV ivParam = (ParametersWithIV)params;
          byte[]           iv      = ivParam.getIV();
          System.arraycopy(iv, 0, IV, 0, IV.length);
          reset();
          cipher.init(true, ivParam.getParameters());
        }
    }
    public String getAlgorithmName()
    {
        return cipher.getAlgorithmName() + "/SIC";
    }
    public int getBlockSize()
    {
        return cipher.getBlockSize();
    }
    public int processBlock(byte[] in, int inOff, byte[] out, int outOff)
          throws DataLengthException, IllegalStateException
    {
        cipher.processBlock(counter, 0, counterOut, 0);
        for (int i = 0; i < counterOut.length; i++)
        {
          out[outOff + i] = (byte)(counterOut[i] ^ in[inOff + i]);
        }
        int    carry = 1;
        for (int i = counter.length - 1; i >= 0; i--)
        {
            int    x = (counter[i] & 0xff) + carry;
            if (x > 0xff)
            {
                carry = 1;
            }
            else
            {
                carry = 0;
            }
            counter[i] = (byte)x;
        }
        return counter.length;
    }
    public void reset()
    {
        System.arraycopy(IV, 0, counter, 0, counter.length);
        cipher.reset();
    }
}
