public class BadValue {
    public static void main(String[] args) throws Exception {
        InputStream in = new ByteArrayInputStream(new byte[10]);
        byte[] bs = IOUtils.readFully(in, 4, true);
        if (bs.length != 4 || in.available() != 6) {
            throw new Exception("First read error");
        }
        bs = IOUtils.readFully(in, 10, false);
        if (bs.length != 6 || in.available() != 0) {
            throw new Exception("Second read error");
        }
        in = new ByteArrayInputStream(new byte[10]);
        bs = IOUtils.readFully(in, Integer.MAX_VALUE, true);
        if (bs.length != 10 || in.available() != 0) {
            throw new Exception("Second read error");
        }
        in = new ByteArrayInputStream(new byte[10]);
        bs = IOUtils.readFully(in, Integer.MAX_VALUE, false);
        if (bs.length != 10 || in.available() != 0) {
            throw new Exception("Second read error");
        }
        in = new ByteArrayInputStream(new byte[10]);
        try {
            bs = IOUtils.readFully(in, 20, true);
            throw new Exception("Third read error");
        } catch (EOFException e) {
        }
        int bignum = 10 * 1024 * 1024;
        bs = IOUtils.readFully(new SuperSlowStream(bignum), -1, true);
        if (bs.length != bignum) {
            throw new Exception("Fourth read error");
        }
        byte[] input = {0x04, (byte)0x84, 0x40, 0x00, 0x42, 0x46, 0x4b};
        try {
            new DerValue(new ByteArrayInputStream(input));
        } catch (IOException ioe) {
        }
    }
}
class SuperSlowStream extends InputStream {
    private int p;
    public SuperSlowStream(int capacity) {
        p = capacity;
    }
    @Override
    public int read() throws IOException {
        if (p > 0) {
            p--;
            return 0;
        } else {
            return -1;
        }
    }
    @Override
    public int read(byte b[], int off, int len) throws IOException {
        if (len == 0) return 0;
        if (p > 0) {
            p--;
            b[off] = 0;
            return 1;
        } else {
            return -1;
        }
    }
}
