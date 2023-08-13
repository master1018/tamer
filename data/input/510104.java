public class GOFBBlockCipher
    implements BlockCipher
{
    private byte[]          IV;
    private byte[]          ofbV;
    private byte[]          ofbOutV;
    private final int             blockSize;
    private final BlockCipher     cipher;
    boolean firstStep = true;
    int N3;
    int N4;
    static final int C1 = 16843012; 
    static final int C2 = 16843009; 
    public GOFBBlockCipher(
        BlockCipher cipher)
    {
        this.cipher = cipher;
        this.blockSize = cipher.getBlockSize();
        if (blockSize != 8)
        {
            throw new IllegalArgumentException("GTCR only for 64 bit block ciphers");
        }
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
        firstStep = true;
        N3 = 0;
        N4 = 0;
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
        return cipher.getAlgorithmName() + "/GCTR";
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
        if (firstStep)
        {
            firstStep = false;
            cipher.processBlock(ofbV, 0, ofbOutV, 0);
            N3 = bytesToint(ofbOutV, 0);
            N4 = bytesToint(ofbOutV, 4);
        }
        N3 += C2;
        N4 += C1;
        intTobytes(N3, ofbV, 0);
        intTobytes(N4, ofbV, 4);
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
    private int bytesToint(
        byte[]  in,
        int     inOff)
    {
        return  ((in[inOff + 3] << 24) & 0xff000000) + ((in[inOff + 2] << 16) & 0xff0000) +
                ((in[inOff + 1] << 8) & 0xff00) + (in[inOff] & 0xff);
    }
    private void intTobytes(
            int     num,
            byte[]  out,
            int     outOff)
    {
            out[outOff + 3] = (byte)(num >>> 24);
            out[outOff + 2] = (byte)(num >>> 16);
            out[outOff + 1] = (byte)(num >>> 8);
            out[outOff] =     (byte)num;
    }
}
