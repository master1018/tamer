public class HandshakeInStream extends InputStream {
    InputRecord r;
    HandshakeInStream(HandshakeHash handshakeHash) {
        r = new InputRecord();
        r.setHandshakeHash(handshakeHash);
    }
    public int available() {
        return r.available();
    }
    public int read() throws IOException {
        int n = r.read();
        if (n == -1) {
            throw new SSLException("Unexpected end of handshake data");
        }
        return n;
    }
    public int read(byte b [], int off, int len) throws IOException {
        int n = r.read(b, off, len);
        if (n != len) {
            throw new SSLException("Unexpected end of handshake data");
        }
        return n;
    }
    public long skip(long n) throws IOException {
        return r.skip(n);
    }
    public void mark(int readlimit) {
        r.mark(readlimit);
    }
    public void reset() {
        r.reset();
    }
    public boolean markSupported() {
        return true;
    }
    void incomingRecord(InputRecord in) throws IOException {
        r.queueHandshake(in);
    }
    void digestNow() {
        r.doHashes();
    }
    void ignore(int n) {
        r.ignore(n);
    }
    int getInt8() throws IOException {
        return read();
    }
    int getInt16() throws IOException {
        return (getInt8() << 8) | getInt8();
    }
    int getInt24() throws IOException {
        return (getInt8() << 16) | (getInt8() << 8) | getInt8();
    }
    int getInt32() throws IOException {
        return (getInt8() << 24) | (getInt8() << 16)
             | (getInt8() << 8) | getInt8();
    }
    byte[] getBytes8() throws IOException {
        int len = getInt8();
        byte b[] = new byte[len];
        read(b, 0, len);
        return b;
    }
    public byte[] getBytes16() throws IOException {
        int len = getInt16();
        byte b[] = new byte[len];
        read(b, 0, len);
        return b;
    }
    byte[] getBytes24() throws IOException {
        int len = getInt24();
        byte b[] = new byte[len];
        read(b, 0, len);
        return b;
    }
}
