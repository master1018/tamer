public class CipherInputStream extends FilterInputStream {
    private final Cipher cipher;
    private final int I_BUFFER_SIZE = 20;
    private final byte[] i_buffer = new byte[I_BUFFER_SIZE];
    private int index; 
    private byte[] o_buffer;
    private boolean finished;
    public CipherInputStream(InputStream is, Cipher c) {
        super(is);
        this.cipher = c;
    }
    protected CipherInputStream(InputStream is) {
        this(is, new NullCipher());
    }
    @Override
    public int read() throws IOException {
        if (finished) {
            return ((o_buffer == null) || (index == o_buffer.length)) 
                            ? -1 
                            : o_buffer[index++] & 0xFF;
        }
        if ((o_buffer != null) && (index < o_buffer.length)) {
            return o_buffer[index++] & 0xFF;
        }
        index = 0;
        o_buffer = null;
        int num_read;
        while (o_buffer == null) {
            if ((num_read = in.read(i_buffer)) == -1) {
                try {
                    o_buffer = cipher.doFinal();
                } catch (Exception e) {
                    throw new IOException(e.getMessage());
                }
                finished = true;
                break;
            }
            o_buffer = cipher.update(i_buffer, 0, num_read);
        }
        return read();
    }
    @Override
    public int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }
    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (in == null) {
            throw new NullPointerException("Underlying input stream is null");
        }
        int read_b;
        int i;
        for (i=0; i<len; i++) {
            if ((read_b = read()) == -1) {
                return (i == 0) ? -1 : i; 
            }
            if (b != null) {
                b[off+i] = (byte) read_b;
            }
        }
        return i;
    }
    @Override
    public long skip(long n) throws IOException {
        long i = 0;
        int available = available();
        if (available < n) {
            n = available;
        }
        while ((i < n) && (read() != -1)) {
            i++;
        }
        return i;
    }
    @Override
    public int available() throws IOException {
        return 0;
    }
    @Override
    public void close() throws IOException {
        in.close();
        try {
            cipher.doFinal();
        } catch (GeneralSecurityException ignore) {
        }
    }
    @Override
    public boolean markSupported() {
        return false;
    }
}
