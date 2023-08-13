public abstract class SSLInputStream extends InputStream {
    @Override
    public abstract int available() throws IOException;
    @Override
    public abstract int read() throws IOException;
    @Override
    public long skip(long n) throws IOException {
        long skept = n;
        while (n > 0) {
            read();
            n--;
        }
        return skept;
    }
    public int readUint8() throws IOException {
        return read() & 0x00FF;
    }
    public int readUint16() throws IOException {
        return (read() << 8) | (read() & 0x00FF);
    }
    public int readUint24() throws IOException {
        return (read() << 16) | (read() << 8) | (read() & 0x00FF);
    }
    public long readUint32() throws IOException {
        return (read() << 24) | (read() << 16)
              | (read() << 8) | (read() & 0x00FF);
    }
    public long readUint64() throws IOException {
        long hi = readUint32();
        long lo = readUint32();
        return (hi << 32) | lo;
    }
    public byte[] read(int length) throws IOException {
        byte[] res = new byte[length];
        for (int i=0; i<length; i++) {
            res[i] = (byte) read();
        }
        return res;
    }
    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int read_b;
        int i = 0;
        do {
            if ((read_b = read()) == -1) {
                return (i == 0) ? -1 : i;
            }
            b[off+i] = (byte) read_b;
            i++;
        } while ((available() != 0) && (i<len));
        return i;
    }
}
