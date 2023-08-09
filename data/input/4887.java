final class PKCS5Padding implements Padding {
    private int blockSize;
    PKCS5Padding(int blockSize) {
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
        for (int i = 0; i < len; i++) {
            in[i + off] = paddingOctet;
        }
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
        for (int i = 0; i < ((int)lastByte & 0x0ff); i++) {
            if (in[start+i] != lastByte) {
                return -1;
            }
        }
        return start;
    }
    public int padLength(int len) {
        int paddingOctet = blockSize - (len % blockSize);
        return paddingOctet;
    }
}
