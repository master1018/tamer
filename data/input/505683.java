public class OFBBlockCipher
    implements BlockCipher
{
    private byte[]          IV;
    private byte[]          ofbV;
    private byte[]          ofbOutV;
    private final int             blockSize;
    private final BlockCipher     cipher;
    public OFBBlockCipher(
        BlockCipher cipher,
        int         blockSize)
    {
        this.cipher = cipher;
        this.blockSize = blockSize / 8;
        this.IV = new byte[cipher.getBlockSize()];
        this.ofbV = new byte[cipher.getBlockSize()];
        this.ofbOutV = new byte[cipher.getBlockSize()];
    }
    public BlockCipher getUnderlyingCipher()
    {
        return cipher;
    }
    public void init(
        boolean             encrypting, 
        CipherParameters    params)
        throws IllegalArgumentException
    {
        if (params instanceof ParametersWithIV)
        {
                ParametersWithIV ivParam = (ParametersWithIV)params;
                byte[]      iv = ivParam.getIV();
                if (iv.length < IV.length)
                {
                    System.arraycopy(iv, 0, IV, IV.length - iv.length, iv.length); 
                    for (int i = 0; i < IV.length - iv.length; i++)
                    {
                        IV[i] = 0;
                    }
                }
                else
                {
                    System.arraycopy(iv, 0, IV, 0, IV.length);
                }
                reset();
                cipher.init(true, ivParam.getParameters());
        }
        else
        {
                reset();
                cipher.init(true, params);
        }
    }
    public String getAlgorithmName()
    {
        return cipher.getAlgorithmName() + "/OFB" + (blockSize * 8);
    }
    public int getBlockSize()
    {
        return blockSize;
    }
    public int processBlock(
        byte[]      in,
        int         inOff,
        byte[]      out,
        int         outOff)
        throws DataLengthException, IllegalStateException
    {
        if ((inOff + blockSize) > in.length)
        {
            throw new DataLengthException("input buffer too short");
        }
        if ((outOff + blockSize) > out.length)
        {
            throw new DataLengthException("output buffer too short");
        }
        cipher.processBlock(ofbV, 0, ofbOutV, 0);
        for (int i = 0; i < blockSize; i++)
        {
            out[outOff + i] = (byte)(ofbOutV[i] ^ in[inOff + i]);
        }
        System.arraycopy(ofbV, blockSize, ofbV, 0, ofbV.length - blockSize);
        System.arraycopy(ofbOutV, 0, ofbV, ofbV.length - blockSize, blockSize);
        return blockSize;
    }
    public void reset()
    {
        System.arraycopy(IV, 0, ofbV, 0, IV.length);
        cipher.reset();
    }
}
