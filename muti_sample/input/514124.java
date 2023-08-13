public class DigestOutputStream extends FilterOutputStream {
    protected MessageDigest digest;
    private boolean isOn = true;
    public DigestOutputStream(OutputStream stream, MessageDigest digest) {
        super(stream);
        this.digest = digest;
    }
    public MessageDigest getMessageDigest() {
        return digest;
    }
    public void setMessageDigest(MessageDigest digest) {
        this.digest = digest;
    }
    @Override
    public void write(int b) throws IOException {
        if (isOn) {
            digest.update((byte)b);
        }
        out.write(b);
    }
    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        if (isOn) {
            digest.update(b, off, len);
        }
        out.write(b, off, len);
    }
    public void on(boolean on) {
        isOn = on;
    }
    @Override
    public String toString() {
        return super.toString() + ", " + digest.toString() + 
            (isOn ? ", is on" : ", is off"); 
    }
}
