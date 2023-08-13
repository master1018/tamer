public class ISO9796d1Encoding
    implements AsymmetricBlockCipher
{
    private static byte[]    shadows = { 0xe, 0x3, 0x5, 0x8, 0x9, 0x4, 0x2, 0xf,
                                    0x0, 0xd, 0xb, 0x6, 0x7, 0xa, 0xc, 0x1 };
    private static byte[]    inverse = { 0x8, 0xf, 0x6, 0x1, 0x5, 0x2, 0xb, 0xc,
                                    0x3, 0x4, 0xd, 0xa, 0xe, 0x9, 0x0, 0x7 };
    private AsymmetricBlockCipher   engine;
    private boolean                 forEncryption;
    private int                     bitSize;
    private int                     padBits = 0;
    public ISO9796d1Encoding(
        AsymmetricBlockCipher   cipher)
    {
        this.engine = cipher;
    }   
    public AsymmetricBlockCipher getUnderlyingCipher()
    {
        return engine;
    }
    public void init(
        boolean             forEncryption,
        CipherParameters    param)
    {
        RSAKeyParameters  kParam = null;
        if (param instanceof ParametersWithRandom)
        {
            ParametersWithRandom    rParam = (ParametersWithRandom)param;
            kParam = (RSAKeyParameters)rParam.getParameters();
        }
        else
        {
            kParam = (RSAKeyParameters)param;
        }
        engine.init(forEncryption, kParam);
        bitSize = kParam.getModulus().bitLength();
        this.forEncryption = forEncryption;
    }
    public int getInputBlockSize()
    {
        int     baseBlockSize = engine.getInputBlockSize();
        if (forEncryption)
        {
            return (baseBlockSize + 1) / 2;
        }
        else
        {
            return baseBlockSize;
        }
    }
    public int getOutputBlockSize()
    {
        int     baseBlockSize = engine.getOutputBlockSize();
        if (forEncryption)
        {
            return baseBlockSize;
        }
        else
        {
            return (baseBlockSize + 1) / 2;
        }
    }
    public void setPadBits(
        int     padBits)
    {
        if (padBits > 7)
        {
            throw new IllegalArgumentException("padBits > 7");
        }
        this.padBits = padBits;
    }
    public int getPadBits()
    {
        return padBits;
    }
    public byte[] processBlock(
        byte[]  in,
        int     inOff,
        int     inLen)
        throws InvalidCipherTextException
    {
        if (forEncryption)
        {
            return encodeBlock(in, inOff, inLen);
        }
        else
        {
            return decodeBlock(in, inOff, inLen);
        }
    }
    private byte[] encodeBlock(
        byte[]  in,
        int     inOff,
        int     inLen)
        throws InvalidCipherTextException
    {
        byte[]  block = new byte[(bitSize + 7) / 8];
        int     r = padBits + 1;
        int     z = inLen;
        int     t = (bitSize + 13) / 16;
        for (int i = 0; i < t; i += z)
        {
            if (i > t - z)
            {
                System.arraycopy(in, inOff + inLen - (t - i),
                                    block, block.length - t, t - i);
            }
            else
            {
                System.arraycopy(in, inOff, block, block.length - (i + z), z);
            }
        }
        for (int i = block.length - 2 * t; i != block.length; i += 2)
        {
            byte    val = block[block.length - t + i / 2];
            block[i] = (byte)((shadows[(val & 0xff) >>> 4] << 4)
                                                | shadows[val & 0x0f]);
            block[i + 1] = val;
        }
        block[block.length - 2 * z] ^= r;
        block[block.length - 1] = (byte)((block[block.length - 1] << 4) | 0x06);
        int maxBit = (8 - (bitSize - 1) % 8);
        int offSet = 0;
        if (maxBit != 8)
        {
            block[0] &= 0xff >>> maxBit;
            block[0] |= 0x80 >>> maxBit;
        }
        else
        {
            block[0] = 0x00;
            block[1] |= 0x80;
            offSet = 1;
        }
        return engine.processBlock(block, offSet, block.length - offSet);
    }
    private byte[] decodeBlock(
        byte[]  in,
        int     inOff,
        int     inLen)
        throws InvalidCipherTextException
    {
        byte[]  block = engine.processBlock(in, inOff, inLen);
        int     r = 1;
        int     t = (bitSize + 13) / 16;
        if ((block[block.length - 1] & 0x0f) != 0x6)
        {
            throw new InvalidCipherTextException("invalid forcing byte in block");
        }
        block[block.length - 1] = (byte)(((block[block.length - 1] & 0xff) >>> 4) | ((inverse[(block[block.length - 2] & 0xff) >> 4]) << 4));
        block[0] = (byte)((shadows[(block[1] & 0xff) >>> 4] << 4)
                                                | shadows[block[1] & 0x0f]);
        boolean boundaryFound = false;
        int     boundary = 0;
        for (int i = block.length - 1; i >= block.length - 2 * t; i -= 2)
        {
            int val = ((shadows[(block[i] & 0xff) >>> 4] << 4)
                                        | shadows[block[i] & 0x0f]);
            if (((block[i - 1] ^ val) & 0xff) != 0)
            {
                if (!boundaryFound)
                {
                    boundaryFound = true;
                    r = (block[i - 1] ^ val) & 0xff;
                    boundary = i - 1;
                }
                else
                {
                    throw new InvalidCipherTextException("invalid tsums in block");
                }
            }
        }
        block[boundary] = 0;
        byte[]  nblock = new byte[(block.length - boundary) / 2];
        for (int i = 0; i < nblock.length; i++)
        {
            nblock[i] = block[2 * i + boundary + 1];
        }
        padBits = r - 1;
        return nblock;
    }
}
