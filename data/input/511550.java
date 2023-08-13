public class StringBufferInputStream extends InputStream {
    protected String buffer;
    protected int count;
    protected int pos;
    public StringBufferInputStream(String str) {
        if (str == null) {
            throw new NullPointerException();
        }
        buffer = str;
        count = str.length();
    }
    @Override
    public synchronized int available() {
        return count - pos;
    }
    @Override
    public synchronized int read() {
        return pos < count ? buffer.charAt(pos++) & 0xFF : -1;
    }
    @Override
    public synchronized int read(byte[] b, int offset, int length) {
        if (pos >= count) {
            return -1;
        }
        if (b == null) {
            throw new NullPointerException(Msg.getString("K0047")); 
        }
        if (offset < 0 || offset > b.length) {
            throw new ArrayIndexOutOfBoundsException(Msg.getString("K002e", offset)); 
        }
        if (length < 0 || length > b.length - offset) {
            throw new ArrayIndexOutOfBoundsException(Msg.getString("K0031", length)); 
        }
        if (length == 0) {
            return 0;
        }
        int copylen = count - pos < length ? count - pos : length;
        for (int i = 0; i < copylen; i++) {
            b[offset + i] = (byte) buffer.charAt(pos + i);
        }
        pos += copylen;
        return copylen;
    }
    @Override
    public synchronized void reset() {
        pos = 0;
    }
    @Override
    public synchronized long skip(long n) {
        if (n <= 0) {
            return 0;
        }
        int numskipped;
        if (this.count - pos < n) {
            numskipped = this.count - pos;
            pos = this.count;
        } else {
            numskipped = (int) n;
            pos += n;
        }
        return numskipped;
    }
}
