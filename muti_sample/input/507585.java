public class Adler32 implements java.util.zip.Checksum {
    private long adler = 1;
    public long getValue() {
        return adler;
    }
    public void reset() {
        adler = 1;
    }
    public void update(int i) {
        adler = updateByteImpl(i, adler);
    }
    public void update(byte[] buf) {
        update(buf, 0, buf.length);
    }
    public void update(byte[] buf, int off, int nbytes) {
        if (off <= buf.length && nbytes >= 0 && off >= 0
                && buf.length - off >= nbytes) {
            adler = updateImpl(buf, off, nbytes, adler);
        } else {
            throw new ArrayIndexOutOfBoundsException();
        }
    }
    private native long updateImpl(byte[] buf, int off, int nbytes, long adler1);
    private native long updateByteImpl(int val, long adler1);
}
