public class CheckedInputStream extends java.io.FilterInputStream {
    private final Checksum check;
    public CheckedInputStream(InputStream is, Checksum csum) {
        super(is);
        check = csum;
    }
    @Override
    public int read() throws IOException {
        int x = in.read();
        if (x != -1) {
            check.update(x);
        }
        return x;
    }
    @Override
    public int read(byte[] buf, int off, int nbytes) throws IOException {
        int x = in.read(buf, off, nbytes);
        if (x != -1) {
            check.update(buf, off, x);
        }
        return x;
    }
    public Checksum getChecksum() {
        return check;
    }
    @Override
    public long skip(long nbytes) throws IOException {
        if (nbytes < 1) {
            return 0;
        }
        long skipped = 0;
        byte[] b = new byte[(int)Math.min(nbytes, 2048L)];
        int x, v;
        while (skipped != nbytes) {
            x = in.read(b, 0,
                    (v = (int) (nbytes - skipped)) > b.length ? b.length : v);
            if (x == -1) {
                return skipped;
            }
            check.update(b, 0, x);
            skipped += x;
        }
        return skipped;
    }
}
