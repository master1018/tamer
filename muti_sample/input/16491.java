public class CloseStream {
    public static void main( String argv[] ) throws Exception {
        BufferedInputStream in = new BufferedInputStream(new MyInputStream());
        in.read();
        in.close();
        try {
            in.read(); 
            throw new RuntimeException("No exception during read on closed stream");
        }
        catch (IOException e) {
            System.err.println("Test passed: IOException is thrown");
        }
    }
}
class MyInputStream extends InputStream {
    public MyInputStream() {
    }
    public void close() throws IOException {
        if (status == OPEN) {
            status = CLOSED;
        } else throw new IOException();
    }
    public int read() throws IOException {
        if (status == CLOSED)
            throw new IOException();
        return (byte)'a';
    }
    private final int OPEN = 1;
    private final int CLOSED = 2;
    private int status = OPEN;
}
