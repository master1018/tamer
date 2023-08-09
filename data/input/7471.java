abstract class DigestBase extends MessageDigestSpi implements Cloneable {
    private byte[] oneByte;
    private final String algorithm;
    private final int digestLength;
    private final int blockSize;
    final byte[] buffer;
    private int bufOfs;
    long bytesProcessed;
    DigestBase(String algorithm, int digestLength, int blockSize) {
        super();
        this.algorithm = algorithm;
        this.digestLength = digestLength;
        this.blockSize = blockSize;
        buffer = new byte[blockSize];
    }
    DigestBase(DigestBase base) {
        this.algorithm = base.algorithm;
        this.digestLength = base.digestLength;
        this.blockSize = base.blockSize;
        this.buffer = base.buffer.clone();
        this.bufOfs = base.bufOfs;
        this.bytesProcessed = base.bytesProcessed;
    }
    protected final int engineGetDigestLength() {
        return digestLength;
    }
    protected final void engineUpdate(byte b) {
        if (oneByte == null) {
            oneByte = new byte[1];
        }
        oneByte[0] = b;
        engineUpdate(oneByte, 0, 1);
    }
    protected final void engineUpdate(byte[] b, int ofs, int len) {
        if (len == 0) {
            return;
        }
        if ((ofs < 0) || (len < 0) || (ofs > b.length - len)) {
            throw new ArrayIndexOutOfBoundsException();
        }
        if (bytesProcessed < 0) {
            engineReset();
        }
        bytesProcessed += len;
        if (bufOfs != 0) {
            int n = Math.min(len, blockSize - bufOfs);
            System.arraycopy(b, ofs, buffer, bufOfs, n);
            bufOfs += n;
            ofs += n;
            len -= n;
            if (bufOfs >= blockSize) {
                implCompress(buffer, 0);
                bufOfs = 0;
            }
        }
        while (len >= blockSize) {
            implCompress(b, ofs);
            len -= blockSize;
            ofs += blockSize;
        }
        if (len > 0) {
            System.arraycopy(b, ofs, buffer, 0, len);
            bufOfs = len;
        }
    }
    protected final void engineReset() {
        if (bytesProcessed == 0) {
            return;
        }
        implReset();
        bufOfs = 0;
        bytesProcessed = 0;
    }
    protected final byte[] engineDigest() {
        byte[] b = new byte[digestLength];
        try {
            engineDigest(b, 0, b.length);
        } catch (DigestException e) {
            throw (ProviderException)
                new ProviderException("Internal error").initCause(e);
        }
        return b;
    }
    protected final int engineDigest(byte[] out, int ofs, int len)
            throws DigestException {
        if (len < digestLength) {
            throw new DigestException("Length must be at least "
                + digestLength + " for " + algorithm + "digests");
        }
        if ((ofs < 0) || (len < 0) || (ofs > out.length - len)) {
            throw new DigestException("Buffer too short to store digest");
        }
        if (bytesProcessed < 0) {
            engineReset();
        }
        implDigest(out, ofs);
        bytesProcessed = -1;
        return digestLength;
    }
    abstract void implCompress(byte[] b, int ofs);
    abstract void implDigest(byte[] out, int ofs);
    abstract void implReset();
    public abstract Object clone();
    static final byte[] padding;
    static {
        padding = new byte[136];
        padding[0] = (byte)0x80;
    }
}
