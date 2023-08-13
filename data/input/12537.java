class AppOutputStream extends OutputStream {
    private SSLSocketImpl c;
    OutputRecord r;
    private final byte[] oneByte = new byte[1];
    AppOutputStream(SSLSocketImpl conn) {
        r = new OutputRecord(Record.ct_application_data);
        c = conn;
    }
    synchronized public void write(byte b[], int off, int len)
            throws IOException {
        if (b == null) {
            throw new NullPointerException();
        } else if (off < 0 || len < 0 || len > b.length - off) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return;
        }
        c.checkWrite();
        try {
            do {
                int howmuch = Math.min(len, r.availableDataBytes());
                if (howmuch > 0) {
                    r.write(b, off, howmuch);
                    off += howmuch;
                    len -= howmuch;
                }
                c.writeRecord(r);
                c.checkWrite();
            } while (len > 0);
        } catch (Exception e) {
            c.handleException(e);
        }
    }
    synchronized public void write(int i) throws IOException {
        oneByte[0] = (byte)i;
        write(oneByte, 0, 1);
    }
    public void close() throws IOException {
        c.close();
    }
}
