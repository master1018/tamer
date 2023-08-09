class BlobR extends InputStream {
    private Blob blob;
    private int pos;
    BlobR(Blob blob) {
    this.blob = blob;
    this.pos = 0;
    }
    @Override
    public int available() throws IOException {
    int ret = blob.size - pos;
    return (ret < 0) ? 0 : ret;
    }
    public void mark(int limit) {
    }
    public void reset() throws IOException {
    }
    public boolean markSupported() {
    return false;
    }
    public void close() throws IOException {
        blob.close();
    blob = null;
    pos = 0;
    }
    public long skip(long n) throws IOException {
    long ret = pos + n;
    if (ret < 0) {
        ret = 0;
        pos = 0;
    } else if (ret > blob.size) {
        ret = blob.size;
        pos = blob.size;
    } else {
        pos = (int) ret;
    }
    return ret;
    }
    public int read() throws IOException {
    byte b[] = new byte[1];
    int n = blob.read(b, 0, pos, b.length);
    if (n > 0) {
        pos += n;
        return b[0];
    }
    return -1;
    }
    public int read(byte b[]) throws IOException {
    int n = blob.read(b, 0, pos, b.length);
    if (n > 0) {
        pos += n;
        return n;
    }
    return -1;
    }
    public int read(byte b[], int off, int len) throws IOException {
    if (off + len > b.length) {
        len = b.length - off;
    }
    if (len < 0) {
        return -1;
    }
    if (len == 0) {
        return 0;
    }
    int n = blob.read(b, off, pos, len);
    if (n > 0) {
        pos += n;
        return n;
    }
    return -1;
    }
}
class BlobW extends OutputStream {
    private Blob blob;
    private int pos;
    BlobW(Blob blob) {
    this.blob = blob;
    this.pos = 0;
    }
    public void flush() throws IOException {
    }
    public void close() throws IOException {
        blob.close();
    blob = null;
    pos = 0;
    }
    public void write(int v) throws IOException {
    byte b[] = new byte[1];
    b[0] = (byte) v;
    pos += blob.write(b, 0, pos, 1);
    }
    public void write(byte[] b) throws IOException {
    if (b != null && b.length > 0) {
        pos += blob.write(b, 0, pos, b.length);
    }
    }
    public void write(byte[] b, int off, int len) throws IOException {
    if (b != null) {
        if (off + len > b.length) {
        len = b.length - off;
        }
        if (len <= 0) {
        return;
        }
        pos += blob.write(b, off, pos, len);
    }
    }
}
public class Blob {
    private long handle = 0;
    protected int size = 0;
    public InputStream getInputStream() {
    return (InputStream) new BlobR(this);
    }
    public OutputStream getOutputStream() {
    return (OutputStream) new BlobW(this);
    }
    public native void close();
    native int write(byte[] b, int off, int pos, int len) throws IOException;
    native int read(byte[] b, int off, int pos, int len) throws IOException;
    protected native void finalize();
    private static native void internal_init();
    static {
    internal_init();
    }
}
