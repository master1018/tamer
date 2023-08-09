public class CheckedOutputStream extends java.io.FilterOutputStream {
    private final Checksum check;
    public CheckedOutputStream(OutputStream os, Checksum cs) {
        super(os);
        check = cs;
    }
    public Checksum getChecksum() {
        return check;
    }
    @Override
    public void write(int val) throws IOException {
        out.write(val);
        check.update(val);
    }
    @Override
    public void write(byte[] buf, int off, int nbytes) throws IOException {
        out.write(buf, off, nbytes);
        check.update(buf, off, nbytes);
    }
}
