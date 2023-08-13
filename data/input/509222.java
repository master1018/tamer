public class EOLConvertingInputStream extends InputStream {
    public static final int CONVERT_CR   = 1;
    public static final int CONVERT_LF   = 2;
    public static final int CONVERT_BOTH = 3;
    private PushbackInputStream in = null;
    private int previous = 0;
    private int flags = CONVERT_BOTH;
    public EOLConvertingInputStream(InputStream in) {
        this(in, CONVERT_BOTH);
    }
    public EOLConvertingInputStream(InputStream in, int flags) {
        super();
        this.in = new PushbackInputStream(in, 2);
        this.flags = flags;
    }
    public void close() throws IOException {
        in.close();
    }
    public int read() throws IOException {
        int b = in.read();
        if (b == -1) {
            return -1;
        }
        if ((flags & CONVERT_CR) != 0 && b == '\r') {
            int c = in.read();
            if (c != -1) {
                in.unread(c);
            }
            if (c != '\n') {
                in.unread('\n');
            }
        } else if ((flags & CONVERT_LF) != 0 && b == '\n' && previous != '\r') {
            b = '\r';
            in.unread('\n');
        }
        previous = b;
        return b;
    }
}
