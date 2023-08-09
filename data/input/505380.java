public class ClosedOutputStream extends OutputStream {
    public static final ClosedOutputStream CLOSED_OUTPUT_STREAM = new ClosedOutputStream();
    public void write(int b) throws IOException {
        throw new IOException("write(" + b + ") failed: stream is closed");
    }
}
