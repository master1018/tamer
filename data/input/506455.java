class MacCFBBlockCipher
{
    private byte[]          IV;
    private byte[]          cfbV;
    private byte[]          cfbOutV;
    private int                 blockSize;
    private BlockCipher         cipher = null;
    public MacCFBBlockCipher(
        BlockCipher         cipher,
        int                 bitBlockSize)
    {
        this.cipher = cipher;
        this.blockSize = bitBlockSize / 8;
        this.IV = new byte[cipher.getBlockSize()];
        this.cfbV = new byte[cipher.getBlockSize()];
        this.cfbOutV = new byte[cipher.getBlockSize()];
    }
    public void init(
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
    public void reset()
    {
        System.arraycopy(IV, 0, cfbV, 0, IV.length);
        cipher.reset();
    }
    void getMacBlock(
        byte[]  mac)
    {
        cipher.processBlock(cfbV, 0, mac, 0);
    }
}
public class CFBBlockCipherMac
    implements Mac
{
    private byte[]              mac;
    private byte[]              buf;
    private int                 bufOff;
    private MacCFBBlockCipher   cipher;
    private BlockCipherPadding  padding = null;
    private int                 macSize;
    public CFBBlockCipherMac(
        BlockCipher     cipher)
    {
        this(cipher, 8, (cipher.getBlockSize() * 8) / 2, null);
    }
    public CFBBlockCipherMac(
        BlockCipher         cipher,
        BlockCipherPadding  padding)
    {
        this(cipher, 8, (cipher.getBlockSize() * 8) / 2, padding);
    }
    public CFBBlockCipherMac(
        BlockCipher         cipher,
        int                 cfbBitSize,
        int                 macSizeInBits)
    {
        this(cipher, cfbBitSize, macSizeInBits, null);
    }
    public CFBBlockCipherMac(
        BlockCipher         cipher,
        int                 cfbBitSize,
        int                 macSizeInBits,
        BlockCipherPadding  padding)
    {
        if ((macSizeInBits % 8) != 0)
        {
            throw new IllegalArgumentException("MAC size must be multiple of 8");
        }
        mac = new byte[cipher.getBlockSize()];
        this.cipher = new MacCFBBlockCipher(cipher, cfbBitSize);
        this.padding = padding;
        this.macSize = macSizeInBits / 8;
        buf = new byte[this.cipher.getBlockSize()];
        bufOff = 0;
    }
    public String getAlgorithmName()
    {
        return cipher.getAlgorithmName();
    }
    public void init(
        CipherParameters    params)
    {
        reset();
        cipher.init(params);
    }
    public int getMacSize()
    {
        return macSize;
    }
    public void update(
        byte        in)
    {
        int         resultLen = 0;
        if (bufOff == buf.length)
        {
            resultLen = cipher.processBlock(buf, 0, mac, 0);
            bufOff = 0;
        }
        buf[bufOff++] = in;
    }
    public void update(
        byte[]      in,
        int         inOff,
        int         len)
    {
        if (len < 0)
        {
            throw new IllegalArgumentException("Can't have a negative input length!");
        }
        int blockSize = cipher.getBlockSize();
        int resultLen = 0;
        int gapLen = blockSize - bufOff;
        if (len > gapLen)
        {
            System.arraycopy(in, inOff, buf, bufOff, gapLen);
            resultLen += cipher.processBlock(buf, 0, mac, 0);
            bufOff = 0;
            len -= gapLen;
            inOff += gapLen;
            while (len > blockSize)
            {
                resultLen += cipher.processBlock(in, inOff, mac, 0);
                len -= blockSize;
                inOff += blockSize;
            }
        }
        System.arraycopy(in, inOff, buf, bufOff, len);
        bufOff += len;
    }
    public int doFinal(
        byte[]  out,
        int     outOff)
    {
        int blockSize = cipher.getBlockSize();
        if (this.padding == null)
        {
            while (bufOff < blockSize)
            {
                buf[bufOff] = 0;
                bufOff++;
            }
        }
        else
        {
            padding.addPadding(buf, bufOff);
        }
        cipher.processBlock(buf, 0, mac, 0);
        cipher.getMacBlock(mac);
        System.arraycopy(mac, 0, out, outOff, macSize);
        reset();
        return macSize;
    }
    public void reset()
    {
        for (int i = 0; i < buf.length; i++)
        {
            buf[i] = 0;
        }
        bufOff = 0;
        cipher.reset();
    }
}
