public class CipherOutputStream extends FilterOutputStream {
    private final Cipher cipher;
    private final byte[] arr = new byte[1];
    public CipherOutputStream(OutputStream os, Cipher c) {
        super(os);
        cipher = c;
    }
    protected CipherOutputStream(OutputStream os) {
        this(os, new NullCipher());
    }
    @Override
    public void write(int b) throws IOException {
        byte[] result;
        arr[0] = (byte) b;
        result = cipher.update(arr);
        if (result != null) {
            out.write(result);
        }
    }
    @Override
    public void write(byte[] b) throws IOException {
        write(b, 0, b.length);
    }
    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        if (len == 0) {
            return;
        }
        byte[] result = cipher.update(b, off, len);
        if (result != null) {
            out.write(result);
        }
    }
    @Override
    public void flush() throws IOException {
        out.flush();
    }
    @Override
    public void close() throws IOException {
        byte[] result;
        try {
            if (cipher != null) {
                result = cipher.doFinal();
                if (result != null) {
                    out.write(result);
                }
            }
            if (out != null) {
                out.flush();
            }
        } catch (BadPaddingException e) {
            throw new IOException(e.getMessage());
        } catch (IllegalBlockSizeException e) {
            throw new IOException(e.getMessage());
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }
}
