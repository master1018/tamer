public class HandshakeIODataStream
        extends SSLInputStream implements org.apache.harmony.xnet.provider.jsse.Appendable, DataStream {
    private static final MessageDigest md5;
    private static final MessageDigest sha;
    static {
        try {
            md5 = MessageDigest.getInstance("MD5");
            sha = MessageDigest.getInstance("SHA-1");
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(
                    "Could not initialize the Digest Algorithms.");
        }
    }
    public HandshakeIODataStream() {}
    private int buff_size = 1024;
    private int inc_buff_size = 1024;
    private byte[] buffer = new byte[buff_size];
    private int read_pos;
    private int marked_pos;
    private int read_pos_end;
    @Override
    public int available() {
        return read_pos_end - read_pos;
    }
    @Override
    public boolean markSupported() {
        return true;
    }
    @Override
    public void mark(int limit) {
        marked_pos = read_pos;
    }
    public void mark() {
        marked_pos = read_pos;
    }
    @Override
    public void reset() {
        read_pos = marked_pos;
    }
    protected void removeFromMarkedPosition() {
        System.arraycopy(buffer, read_pos, 
                buffer, marked_pos, read_pos_end - read_pos);
        read_pos_end -= (read_pos - marked_pos);
        read_pos = marked_pos;
    }
    @Override
    public int read() throws IOException {
        if (read_pos == read_pos_end) {
            throw new EndOfBufferException();
        }
        return buffer[read_pos++] & 0xFF;
    }
    @Override
    public byte[] read(int length) throws IOException {
        if (length > available()) {
            throw new EndOfBufferException();
        }
        byte[] res = new byte[length];
        System.arraycopy(buffer, read_pos, res, 0, length);
        read_pos = read_pos + length;
        return res;
    }
    @Override
    public int read(byte[] dest, int offset, int length) throws IOException {
        if (length > available()) {
            throw new EndOfBufferException();
        }
        System.arraycopy(buffer, read_pos, dest, offset, length);
        read_pos = read_pos + length;
        return length;
    }
    public void append(byte[] src) {
        append(src, 0, src.length);
    }
    private void append(byte[] src, int from, int length) {
        if (read_pos == read_pos_end) {
            if (write_pos_beg != write_pos) {
                throw new AlertException(
                    AlertProtocol.UNEXPECTED_MESSAGE,
                    new SSLHandshakeException(
                        "Handshake message has been received before "
                        + "the last oubound message had been sent."));
            }
            if (read_pos < write_pos) {
                read_pos = write_pos;
                read_pos_end = read_pos;
            }
        }
        if (read_pos_end + length > buff_size) {
            enlargeBuffer(read_pos_end+length-buff_size);
        }
        System.arraycopy(src, from, buffer, read_pos_end, length);
        read_pos_end += length;
    }
    private void enlargeBuffer(int size) {
        buff_size = (size < inc_buff_size)
            ? buff_size + inc_buff_size
            : buff_size + size;
        byte[] new_buff = new byte[buff_size];
        System.arraycopy(buffer, 0, new_buff, 0, buffer.length);
        buffer = new_buff;
    }
    protected void clearBuffer() {
        read_pos = 0;
        marked_pos = 0;
        read_pos_end = 0;
        write_pos = 0;
        write_pos_beg = 0;
        Arrays.fill(buffer, (byte) 0);
    }
    private int write_pos;
    private int write_pos_beg;
    private void check(int length) {
        if (write_pos == write_pos_beg) {
            if (read_pos != read_pos_end) {
                throw new AlertException(
                        AlertProtocol.INTERNAL_ERROR,
                        new SSLHandshakeException("Data was not fully read: "
                        + read_pos + " " + read_pos_end));
            }
            if (write_pos_beg < read_pos_end) {
                write_pos_beg = read_pos_end;
                write_pos = write_pos_beg;
            }
        }
        if (write_pos + length >= buff_size) {
            enlargeBuffer(length);
        }
    }
    public void write(byte b) {
        check(1);
        buffer[write_pos++] = b;
    }
    public void writeUint8(long n) {
        check(1);
        buffer[write_pos++] = (byte) (n & 0x00ff);
    }
    public void writeUint16(long n) {
        check(2);
        buffer[write_pos++] = (byte) ((n & 0x00ff00) >> 8);
        buffer[write_pos++] = (byte) (n & 0x00ff);
    }
    public void writeUint24(long n) {
        check(3);
        buffer[write_pos++] = (byte) ((n & 0x00ff0000) >> 16);
        buffer[write_pos++] = (byte) ((n & 0x00ff00) >> 8);
        buffer[write_pos++] = (byte) (n & 0x00ff);
    }
    public void writeUint32(long n) {
        check(4);
        buffer[write_pos++] = (byte) ((n & 0x00ff000000) >> 24);
        buffer[write_pos++] = (byte) ((n & 0x00ff0000) >> 16);
        buffer[write_pos++] = (byte) ((n & 0x00ff00) >> 8);
        buffer[write_pos++] = (byte) (n & 0x00ff);
    }
    public void writeUint64(long n) {
        check(8);
        buffer[write_pos++] = (byte) ((n & 0x00ff00000000000000L) >> 56);
        buffer[write_pos++] = (byte) ((n & 0x00ff000000000000L) >> 48);
        buffer[write_pos++] = (byte) ((n & 0x00ff0000000000L) >> 40);
        buffer[write_pos++] = (byte) ((n & 0x00ff00000000L) >> 32);
        buffer[write_pos++] = (byte) ((n & 0x00ff000000) >> 24);
        buffer[write_pos++] = (byte) ((n & 0x00ff0000) >> 16);
        buffer[write_pos++] = (byte) ((n & 0x00ff00) >> 8);
        buffer[write_pos++] = (byte) (n & 0x00ff);
    }
    public void write(byte[] vector) {
        check(vector.length);
        System.arraycopy(vector, 0, buffer, write_pos, vector.length);
        write_pos += vector.length;
    }
    public boolean hasData() {
        return (write_pos > write_pos_beg);
    }
    public byte[] getData(int length) {
        byte[] res;
        if (write_pos - write_pos_beg < length) {
            res = new byte[write_pos - write_pos_beg];
            System.arraycopy(buffer, write_pos_beg,
                    res, 0, write_pos-write_pos_beg);
            write_pos_beg = write_pos;
        } else {
            res = new byte[length];
            System.arraycopy(buffer, write_pos_beg, res, 0, length);
            write_pos_beg += length;
        }
        return res;
    }
    protected void printContent(PrintStream outstream) {
        int perLine = 20;
        String prefix = " ";
        String delimiter = "";
        for (int i=write_pos_beg; i<write_pos; i++) {
            String tail = Integer.toHexString(
                    0x00ff & buffer[i]).toUpperCase();
            if (tail.length() == 1) {
                tail = "0" + tail;
            }
            outstream.print(prefix + tail + delimiter);
            if (((i-write_pos_beg+1)%10) == 0) {
                outstream.print(" ");
            }
            if (((i-write_pos_beg+1)%perLine) == 0) {
                outstream.println();
            }
        }
        outstream.println();
    }
    protected byte[] getDigestMD5() {
        synchronized (md5) {
            int len = (read_pos_end > write_pos)
                ? read_pos_end
                : write_pos;
            md5.update(buffer, 0, len);
            return md5.digest();
        }
    }
    protected byte[] getDigestSHA() {
        synchronized (sha) {
            int len = (read_pos_end > write_pos)
                ? read_pos_end
                : write_pos;
            sha.update(buffer, 0, len);
            return sha.digest();
        }
    }
    protected byte[] getDigestMD5withoutLast() {
        synchronized (md5) {
            md5.update(buffer, 0, marked_pos);
            return md5.digest();
        }
    }
    protected byte[] getDigestSHAwithoutLast() {
        synchronized (sha) {
            sha.update(buffer, 0, marked_pos);
            return sha.digest();
        }
    }
    protected byte[] getMessages() {
        int len = (read_pos_end > write_pos) ? read_pos_end : write_pos;
        byte[] res = new byte[len];
        System.arraycopy(buffer, 0, res, 0, len);
        return res;
    }
}
