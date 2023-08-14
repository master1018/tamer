public class ISO9796d2Signer
    implements SignerWithRecovery
{
    static final public int   TRAILER_IMPLICIT    = 0xBC;
    static final public int   TRAILER_RIPEMD160   = 0x31CC;
    static final public int   TRAILER_RIPEMD128   = 0x32CC;
    static final public int   TRAILER_SHA1        = 0x33CC;
    private Digest                      digest;
    private AsymmetricBlockCipher       cipher;
    private int         trailer;
    private int         keyBits;
    private byte[]      block;
    private byte[]      mBuf;
    private int         messageLength;
    private boolean     fullMessage;
    private byte[]      recoveredMessage;
    public ISO9796d2Signer(
        AsymmetricBlockCipher   cipher,
        Digest                  digest,
        boolean                 implicit)
    {
        this.cipher = cipher;
        this.digest = digest;
        if (implicit)
        {
            trailer = TRAILER_IMPLICIT;
        }
        else
        {
            if (digest instanceof SHA1Digest)
            {
                trailer = TRAILER_SHA1;
            }
            else
            {
                throw new IllegalArgumentException("no valid trailer for digest");
            }
        }
    }
    public ISO9796d2Signer(
        AsymmetricBlockCipher   cipher,
        Digest                  digest)
    {
        this(cipher, digest, false);
    }
    public void init(
        boolean                 forSigning,
        CipherParameters        param)
    {
        RSAKeyParameters  kParam = (RSAKeyParameters)param;
        cipher.init(forSigning, kParam);
        keyBits = kParam.getModulus().bitLength();
        block = new byte[(keyBits + 7) / 8];
        if (trailer == TRAILER_IMPLICIT)
        {
            mBuf = new byte[block.length - digest.getDigestSize() - 2];
        }
        else
        {
            mBuf = new byte[block.length - digest.getDigestSize() - 3];
        }
        reset();
    }
    private boolean isSameAs(
        byte[]    a,
        byte[]    b)
    {
        if (messageLength > mBuf.length)
        {
            if (mBuf.length > b.length)
            {
                return false;
            }
            for (int i = 0; i != mBuf.length; i++)
            {
                if (a[i] != b[i])
                {
                    return false;
                }
            }
        }
        else
        {
            if (messageLength != b.length)
            {
                return false;
            }
            for (int i = 0; i != b.length; i++)
            {
                if (a[i] != b[i])
                {
                    return false;
                }
            }
        }
        return true;
    }
    private void clearBlock(
        byte[]  block)
    {
        for (int i = 0; i != block.length; i++)
        {
            block[i] = 0;
        }
    }
    public void update(
        byte    b)
    {
        digest.update(b);
        if (messageLength < mBuf.length)
        {
            mBuf[messageLength] = b;
        }
        messageLength++;
    }
    public void update(
        byte[]  in,
        int     off,
        int     len)
    {
        digest.update(in, off, len);
        if (messageLength < mBuf.length)
        {
            for (int i = 0; i < len && (i + messageLength) < mBuf.length; i++)
            {
                mBuf[messageLength + i] = in[off + i];
            }
        }
        messageLength += len;
    }
    public void reset()
    {
        digest.reset();
        messageLength = 0;
        clearBlock(mBuf);
        if (recoveredMessage != null)
        {
            clearBlock(recoveredMessage);
        }
        recoveredMessage = null;
        fullMessage = false;
    }
    public byte[] generateSignature()
        throws CryptoException
    {
        int     digSize = digest.getDigestSize();
        int t = 0;
        int delta = 0;
        if (trailer == TRAILER_IMPLICIT)
        {
            t = 8;
            delta = block.length - digSize - 1;
            digest.doFinal(block, delta);
            block[block.length - 1] = (byte)TRAILER_IMPLICIT;
        }
        else
        {
            t = 16;
            delta = block.length - digSize - 2;
            digest.doFinal(block, delta);
            block[block.length - 2] = (byte)(trailer >>> 8);
            block[block.length - 1] = (byte)trailer;
        }
        byte    header = 0;
        int     x = (digSize + messageLength) * 8 + t + 4 - keyBits;
        if (x > 0)
        {
            int mR = messageLength - ((x + 7) / 8);
            header = 0x60;
            delta -= mR;
            System.arraycopy(mBuf, 0, block, delta, mR);
        }
        else
        {
            header = 0x40;
            delta -= messageLength;
            System.arraycopy(mBuf, 0, block, delta, messageLength);
        }
        if ((delta - 1) > 0)
        {
            for (int i = delta - 1; i != 0; i--)
            {
                block[i] = (byte)0xbb;
            }
            block[delta - 1] ^= (byte)0x01;
            block[0] = (byte)0x0b;
            block[0] |= header;
        }
        else
        {
            block[0] = (byte)0x0a;
            block[0] |= header;
        }
        byte[]  b = cipher.processBlock(block, 0, block.length);
        clearBlock(mBuf);
        clearBlock(block);
        return b;
    }
    public boolean verifySignature(
        byte[]      signature)
    {
        byte[]      block = null;
        try
        {
            block = cipher.processBlock(signature, 0, signature.length);
        }
        catch (Exception e)
        {
            return false;
        }
        if (((block[0] & 0xC0) ^ 0x40) != 0)
        {
            clearBlock(mBuf);
            clearBlock(block);
            return false;
        }
        if (((block[block.length - 1] & 0xF) ^ 0xC) != 0)
        {
            clearBlock(mBuf);
            clearBlock(block);
            return false;
        }
        int     delta = 0;
        if (((block[block.length - 1] & 0xFF) ^ 0xBC) == 0)
        {
            delta = 1;
        }
        else
        {
            int sigTrail = ((block[block.length - 2] & 0xFF) << 8) | (block[block.length - 1] & 0xFF);
            switch (sigTrail)
            {
            case TRAILER_SHA1:
                    if (!(digest instanceof SHA1Digest))
                    {
                        throw new IllegalStateException("signer should be initialised with SHA1");
                    }
                    break;
            default:
                throw new IllegalArgumentException("unrecognised hash in signature");
            }
            delta = 2;
        }
        int mStart = 0;
        for (mStart = 0; mStart != block.length; mStart++)
        {
            if (((block[mStart] & 0x0f) ^ 0x0a) == 0)
            {
                break;
            }
        }
        mStart++;
        byte[]  hash = new byte[digest.getDigestSize()];
        int off = block.length - delta - hash.length;
        if ((off - mStart) <= 0)
        {
            clearBlock(mBuf);
            clearBlock(block);
            return false;
        }
        if ((block[0] & 0x20) == 0)
        {
            fullMessage = true;
            digest.reset();
            digest.update(block, mStart, off - mStart);
            digest.doFinal(hash, 0);
            for (int i = 0; i != hash.length; i++)
            {
                block[off + i] ^= hash[i];
                if (block[off + i] != 0)
                {
                    clearBlock(mBuf);
                    clearBlock(block);
                    return false;
                }
            }
            recoveredMessage = new byte[off - mStart];
            System.arraycopy(block, mStart, recoveredMessage, 0, recoveredMessage.length);
        }
        else
        {
            fullMessage = false;
            digest.doFinal(hash, 0);
            for (int i = 0; i != hash.length; i++)
            {
                block[off + i] ^= hash[i];
                if (block[off + i] != 0)
                {
                    clearBlock(mBuf);
                    clearBlock(block);
                    return false;
                }
            }
            recoveredMessage = new byte[off - mStart];
            System.arraycopy(block, mStart, recoveredMessage, 0, recoveredMessage.length);
        }
        if (messageLength != 0)
        {
            if (!isSameAs(mBuf, recoveredMessage))
            {
                clearBlock(mBuf);
                clearBlock(block);
                return false;
            }
        }
        clearBlock(mBuf);
        clearBlock(block);
        return true;
    }
    public boolean hasFullMessage()
    {
        return fullMessage;
    }
    public byte[] getRecoveredMessage()
    {
        return recoveredMessage;
    }
}
