public class CountUpdate {
    public static void main(String[] args) throws Exception {
        BufferBreaker breaker = new BufferBreaker();
        BufferedInputStream in = new BufferedInputStream(breaker, 1000);
        byte b[] = new byte[100];
        int total = 0;
        for (int i=0; i<5; i++) {
            if (i>0) breaker.breakIt = true;
            try {
                int n = in.read(b);
                total += n;
            }
            catch (IOException e) {
            }
        }
        if (total>7)
            throw new RuntimeException(
                            "BufferedInputStream did not reset count.");
    }
}
class BufferBreaker extends InputStream {
    public boolean breakIt = false;
    public int read() {
        return 'x';
    }
    public static final byte[] buffer = {(byte)'a',
                                         (byte)'b',
                                         (byte)'c',
                                         (byte)'d',
                                         (byte)'e',
                                         (byte)'f',
                                         (byte)'g'};
    public int read(byte b[]) throws IOException {
        return read(b, 0, b.length);
    }
    public int read(byte b[], int off, int len) throws IOException {
        if (breakIt) throw new IOException("BREAK");
        if (len > buffer.length) len = buffer.length;
        System.arraycopy(buffer, 0, b, off, len);
        return len;
    }
    public long skip(long n) {
        return 0;
    }
    public int available() {
        return 0;
    }
}
