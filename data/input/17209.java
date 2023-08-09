public class CipherOutputStream extends FilterOutputStream {
    private Cipher cipher;
    private OutputStream output;
    private byte[] ibuffer = new byte[1];
    private byte[] obuffer;
    public CipherOutputStream(OutputStream os, Cipher c) {
        super(os);
        output = os;
        cipher = c;
    };
    protected CipherOutputStream(OutputStream os) {
        super(os);
        output = os;
        cipher = new NullCipher();
    }
    public void write(int b) throws IOException {
        ibuffer[0] = (byte) b;
        obuffer = cipher.update(ibuffer, 0, 1);
        if (obuffer != null) {
            output.write(obuffer);
            obuffer = null;
        }
    };
    public void write(byte b[]) throws IOException {
        write(b, 0, b.length);
    }
    public void write(byte b[], int off, int len) throws IOException {
        obuffer = cipher.update(b, off, len);
        if (obuffer != null) {
            output.write(obuffer);
            obuffer = null;
        }
    }
    public void flush() throws IOException {
        if (obuffer != null) {
            output.write(obuffer);
            obuffer = null;
        }
        output.flush();
    }
    public void close() throws IOException {
        try {
            obuffer = cipher.doFinal();
        } catch (IllegalBlockSizeException e) {
            obuffer = null;
        } catch (BadPaddingException e) {
            obuffer = null;
        }
        try {
            flush();
        } catch (IOException ignored) {}
        out.close();
    }
}
