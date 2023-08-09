public class DigestInputStream extends FilterInputStream {
    protected MessageDigest digest;
    private boolean isOn = true;
    public DigestInputStream(InputStream stream, MessageDigest digest) {
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
    public int read() throws IOException {
        int byteRead = in.read();
        if (isOn && (byteRead != -1)) {
            digest.update((byte)byteRead);
        }
        return byteRead;
    }
    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int bytesRead = in.read(b, off, len);
        if (isOn && (bytesRead != -1)) {
            digest.update(b, off, bytesRead);
        }
        return bytesRead;
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
