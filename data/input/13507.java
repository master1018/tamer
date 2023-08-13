final class ISO10126Padding implements Padding {
    private int blockSize;
    ISO10126Padding(int blockSize) {
        this.blockSize = blockSize;
    }
    public void padWithLen(byte[] in, int off, int len)
        throws ShortBufferException
    {
        if (in == null)
            return;
        if ((off + len) > in.length) {
            throw new ShortBufferException("Buffer too small to hold padding");
        }
        byte paddingOctet = (byte) (len & 0xff);
        byte[] padding = new byte[len];
        SunJCE.RANDOM.nextBytes(padding);
        padding[len-1] = paddingOctet;
        System.arraycopy(padding, 0, in, off, len);
        return;
    }
    public int unpad(byte[] in, int off, int len) {
        if ((in == null) ||
            (len == 0)) { 
            return 0;
        }
        byte lastByte = in[off + len - 1];
        int padValue = (int)lastByte & 0x0ff;
        if ((padValue < 0x01)
            || (padValue > blockSize)) {
            return -1;
        }
        int start = off + len - ((int)lastByte & 0x0ff);
        if (start < off) {
            return -1;
        }
        return start;
    }
    public int padLength(int len) {
        int paddingOctet = blockSize - (len % blockSize);
        return paddingOctet;
    }
}
