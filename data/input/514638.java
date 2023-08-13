public class PasswordProtectedInputStream extends FilterInputStream {
    private byte[] password; 
    private int pwdIndex; 
    public PasswordProtectedInputStream(InputStream in, byte[] password) {
        super(in);
        this.password = password.clone();
    }
    @Override
    public int read() throws IOException {
        int read = in.read();
        if (read >= 0) {
            read ^= password[pwdIndex];
            pwdIndex = (pwdIndex + 1) % password.length;
        }
        return read;
    }
    @Override
    public int read(byte b[], int off, int len) throws IOException {
        int read = in.read(b, off, len);
        if (read > 0) {
            int lastIndex = off + read;
            for (int i = off; i < lastIndex; i++) {
                b[i] ^= password[pwdIndex];
                pwdIndex = (pwdIndex + 1) % password.length;
            }
        }
        return read;
    }
    @Override
    public long skip(long n) throws IOException {
        long skip = super.skip(n);
        pwdIndex += skip;
        return skip;
    }
}
