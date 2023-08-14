public class CRC32 implements java.util.zip.Checksum {
    private long crc = 0L;
    long tbytes = 0L;
    public long getValue() {
        return crc;
    }
    public void reset() {
        tbytes = crc = 0;
    }
    public void update(int val) {
        crc = updateByteImpl((byte) val, crc);
    }
    public void update(byte[] buf) {
        update(buf, 0, buf.length);
    }
    public void update(byte[] buf, int off, int nbytes) {
        if (off <= buf.length && nbytes >= 0 && off >= 0
                && buf.length - off >= nbytes) {
            tbytes += nbytes;
            crc = updateImpl(buf, off, nbytes, crc);
        } else {
            throw new ArrayIndexOutOfBoundsException();
        }
    }
    private native long updateImpl(byte[] buf, int off, int nbytes, long crc1);
    private native long updateByteImpl(byte val, long crc1);
}
