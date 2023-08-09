public class CipherInputStream extends FilterInputStream {
    private Cipher cipher;
    private InputStream input;
    private byte[] ibuffer = new byte[512];
    private boolean done = false;
    private byte[] obuffer;
    private int ostart = 0;
    private int ofinish = 0;
    private int getMoreData() throws IOException {
        if (done) return -1;
        int readin = input.read(ibuffer);
        if (readin == -1) {
            done = true;
            try {
                obuffer = cipher.doFinal();
            }
            catch (IllegalBlockSizeException e) {obuffer = null;}
            catch (BadPaddingException e) {obuffer = null;}
            if (obuffer == null)
                return -1;
            else {
                ostart = 0;
                ofinish = obuffer.length;
                return ofinish;
            }
        }
        try {
            obuffer = cipher.update(ibuffer, 0, readin);
        } catch (IllegalStateException e) {obuffer = null;};
        ostart = 0;
        if (obuffer == null)
            ofinish = 0;
        else ofinish = obuffer.length;
        return ofinish;
    }
    public CipherInputStream(InputStream is, Cipher c) {
        super(is);
        input = is;
        cipher = c;
    }
    protected CipherInputStream(InputStream is) {
        super(is);
        input = is;
        cipher = new NullCipher();
    }
    public int read() throws IOException {
        if (ostart >= ofinish) {
            int i = 0;
            while (i == 0) i = getMoreData();
            if (i == -1) return -1;
        }
        return ((int) obuffer[ostart++] & 0xff);
    };
    public int read(byte b[]) throws IOException {
        return read(b, 0, b.length);
    }
    public int read(byte b[], int off, int len) throws IOException {
        if (ostart >= ofinish) {
            int i = 0;
            while (i == 0) i = getMoreData();
            if (i == -1) return -1;
        }
        if (len <= 0) {
            return 0;
        }
        int available = ofinish - ostart;
        if (len < available) available = len;
        if (b != null) {
            System.arraycopy(obuffer, ostart, b, off, available);
        }
        ostart = ostart + available;
        return available;
    }
    public long skip(long n) throws IOException {
        int available = ofinish - ostart;
        if (n > available) {
            n = available;
        }
        if (n < 0) {
            return 0;
        }
        ostart += n;
        return n;
    }
    public int available() throws IOException {
        return (ofinish - ostart);
    }
    public void close() throws IOException {
        input.close();
        try {
            cipher.doFinal();
        }
        catch (BadPaddingException ex) {
        }
        catch (IllegalBlockSizeException ex) {
        }
        ostart = 0;
        ofinish = 0;
    }
    public boolean markSupported() {
        return false;
    }
}
