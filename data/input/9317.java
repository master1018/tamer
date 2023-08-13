public class ReadZeroBytes {
    public static void main( String argv[] ) throws Exception {
        BufferedInputStream in = new BufferedInputStream(
            new ThrowingInputStream());
        in.read(new byte[0], 0, 0);
    }
}
class ThrowingInputStream extends InputStream {
    public ThrowingInputStream() {
    }
    public int read() throws IOException {
        return 0;
    }
    public int read(byte[] b, int off, int len) throws IOException {
        throw new RuntimeException("Read invoked for len == 0");
    }
}
