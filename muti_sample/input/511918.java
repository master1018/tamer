public class CFBBlockCipher
    implements BlockCipher
{
    private byte[]          IV;
    private byte[]          cfbV;
    private byte[]          cfbOutV;
    private int             blockSize;
    private BlockCipher     cipher = null;
    private boolean         encrypting;
    public CFBBlockCipher(
        BlockCipher cipher,
        int         bitBlockSize)
    {
        this.cipher = cipher;
        this.blockSize = bitBlockSize / 8;
        this.IV = new byte[cipher.getBlockSize()];
        this.cfbV = new byte[cipher.getBlockSize()];
        this.cfbOutV = new byte[cipher.getBlockSize()];
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
        this.encrypting = encrypting;
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
        return cipher.getAlgorithmName() + "/CFB" + (blockSize * 8);
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
        return (encrypting) ? encryptBlock(in, inOff, out, outOff) : decryptBlock(in, inOff, out, outOff);
    }
    public int encryptBlock(
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
        cipher.processBlock(cfbV, 0, cfbOutV, 0);
        for (int i = 0; i < blockSize; i++)
        {
            out[outOff + i] = (byte)(cfbOutV[i] ^ in[inOff + i]);
        }
        System.arraycopy(cfbV, blockSize, cfbV, 0, cfbV.length - blockSize);
        System.arraycopy(out, outOff, cfbV, cfbV.length - blockSize, blockSize);
        return blockSize;
    }
    public int decryptBlock(
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
        cipher.processBlock(cfbV, 0, cfbOutV, 0);
        System.arraycopy(cfbV, blockSize, cfbV, 0, cfbV.length - blockSize);
        System.arraycopy(in, inOff, cfbV, cfbV.length - blockSize, blockSize);
        for (int i = 0; i < blockSize; i++)
        {
            out[outOff + i] = (byte)(cfbOutV[i] ^ in[inOff + i]);
        }
        return blockSize;
    }
    public void reset()
    {
        System.arraycopy(IV, 0, cfbV, 0, IV.length);
        cipher.reset();
    }
}
