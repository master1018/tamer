public class BufferedBlockCipher
{
    protected byte[]        buf;
    protected int           bufOff;
    protected boolean       forEncryption;
    protected BlockCipher   cipher;
    protected boolean       partialBlockOkay;
    protected boolean       pgpCFB;
    protected BufferedBlockCipher()
    {
    }
    public BufferedBlockCipher(
        BlockCipher     cipher)
    {
        this.cipher = cipher;
        buf = new byte[cipher.getBlockSize()];
        bufOff = 0;
        String  name = cipher.getAlgorithmName();
        int     idx = name.indexOf('/') + 1;
        pgpCFB = (idx > 0 && name.startsWith("PGP", idx));
        if (pgpCFB)
        {
            partialBlockOkay = true;
        }
        else
        {
            partialBlockOkay = (idx > 0 && (name.startsWith("CFB", idx) || name.startsWith("OFB", idx) || name.startsWith("OpenPGP", idx) || name.startsWith("SIC", idx) || name.startsWith("GCTR", idx)));
        }
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
        this.forEncryption = forEncryption;
        reset();
        cipher.init(forEncryption, params);
    }
    public int getBlockSize()
    {
        return cipher.getBlockSize();
    }
    public int getUpdateOutputSize(
        int len)
    {
        int total       = len + bufOff;
        int leftOver;
        if (pgpCFB)
        {
            leftOver    = total % buf.length - (cipher.getBlockSize() + 2);
        }
        else
        {
            leftOver    = total % buf.length;
        }
        return total - leftOver;
    }
    public int getOutputSize(
        int len)
    {
        int total       = len + bufOff;
        int leftOver;
        if (pgpCFB)
        {
            leftOver    = total % buf.length - (cipher.getBlockSize() + 2);
        }
        else
        {
            leftOver    = total % buf.length;
            if (leftOver == 0)
            {
                return total;
            }
        }
        return total - leftOver + buf.length;
    }
    public int processByte(
        byte        in,
        byte[]      out,
        int         outOff)
        throws DataLengthException, IllegalStateException
    {
        int         resultLen = 0;
        buf[bufOff++] = in;
        if (bufOff == buf.length)
        {
            resultLen = cipher.processBlock(buf, 0, out, outOff);
            bufOff = 0;
        }
        return resultLen;
    }
    public int processBytes(
        byte[]      in,
        int         inOff,
        int         len,
        byte[]      out,
        int         outOff)
        throws DataLengthException, IllegalStateException
    {
        if (len < 0)
        {
            throw new IllegalArgumentException("Can't have a negative input length!");
        }
        int blockSize   = getBlockSize();
        int length      = getUpdateOutputSize(len);
        if (length > 0)
        {
            if ((outOff + length) > out.length)
            {
                throw new DataLengthException("output buffer too short");
            }
        }
        int resultLen = 0;
        int gapLen = buf.length - bufOff;
        if (len > gapLen)
        {
            System.arraycopy(in, inOff, buf, bufOff, gapLen);
            resultLen += cipher.processBlock(buf, 0, out, outOff);
            bufOff = 0;
            len -= gapLen;
            inOff += gapLen;
            while (len > buf.length)
            {
                resultLen += cipher.processBlock(in, inOff, out, outOff + resultLen);
                len -= blockSize;
                inOff += blockSize;
            }
        }
        System.arraycopy(in, inOff, buf, bufOff, len);
        bufOff += len;
        if (bufOff == buf.length)
        {
            resultLen += cipher.processBlock(buf, 0, out, outOff + resultLen);
            bufOff = 0;
        }
        return resultLen;
    }
    public int doFinal(
        byte[]  out,
        int     outOff)
        throws DataLengthException, IllegalStateException, InvalidCipherTextException
    {
        int resultLen = 0;
        if (outOff + bufOff > out.length)
        {
            throw new DataLengthException("output buffer too short for doFinal()");
        }
        if (bufOff != 0 && partialBlockOkay)
        {
            cipher.processBlock(buf, 0, buf, 0);
            resultLen = bufOff;
            bufOff = 0;
            System.arraycopy(buf, 0, out, outOff, resultLen);
        }
        else if (bufOff != 0)
        {
            throw new DataLengthException("data not block size aligned");
        }
        reset();
        return resultLen;
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
