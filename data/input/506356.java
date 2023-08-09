public abstract class GeneralDigest
    implements ExtendedDigest
{
    private static final int BYTE_LENGTH = 64;
    private byte[]  xBuf;
    private int     xBufOff;
    private long    byteCount;
    protected GeneralDigest()
    {
        xBuf = new byte[4];
        xBufOff = 0;
    }
    protected GeneralDigest(GeneralDigest t)
    {
        xBuf = new byte[t.xBuf.length];
        System.arraycopy(t.xBuf, 0, xBuf, 0, t.xBuf.length);
        xBufOff = t.xBufOff;
        byteCount = t.byteCount;
    }
    public void update(
        byte in)
    {
        xBuf[xBufOff++] = in;
        if (xBufOff == xBuf.length)
        {
            processWord(xBuf, 0);
            xBufOff = 0;
        }
        byteCount++;
    }
    public void update(
        byte[]  in,
        int     inOff,
        int     len)
    {
        while ((xBufOff != 0) && (len > 0))
        {
            update(in[inOff]);
            inOff++;
            len--;
        }
        while (len > xBuf.length)
        {
            processWord(in, inOff);
            inOff += xBuf.length;
            len -= xBuf.length;
            byteCount += xBuf.length;
        }
        while (len > 0)
        {
            update(in[inOff]);
            inOff++;
            len--;
        }
    }
    public void finish()
    {
        long    bitLength = (byteCount << 3);
        update((byte)128);
        while (xBufOff != 0)
        {
            update((byte)0);
        }
        processLength(bitLength);
        processBlock();
    }
    public void reset()
    {
        byteCount = 0;
        xBufOff = 0;
        for (int i = 0; i < xBuf.length; i++)
        {
            xBuf[i] = 0;
        }
    }
    public int getByteLength()
    {
        return BYTE_LENGTH;
    }
    protected abstract void processWord(byte[] in, int inOff);
    protected abstract void processLength(long bitLength);
    protected abstract void processBlock();
}
