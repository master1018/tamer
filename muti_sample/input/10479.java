public class ShortWrite {
    static Random rand = new Random();
    static int bytesWritten = 0;
    public static void main(String[] args) throws IOException {
        WritableByteChannel wbc = new WritableByteChannel() {
            public int write(ByteBuffer src) {
                int rem = src.remaining();
                if (rem > 0) {
                    int n = rand.nextInt(rem) + 1;
                    src.position(src.position() + n);
                    bytesWritten += n;
                    return n;
                } else {
                    return 0;
                }
            }
            public void close() throws IOException {
                throw new RuntimeException("not implemented");
            }
            public boolean isOpen() {
                throw new RuntimeException("not implemented");
            }
        };
        OutputStream out = Channels.newOutputStream(wbc);
        int expected = 0;
        byte[] buf = new byte[100];
        for (int i=0; i<buf.length; i++) {
            int len = buf.length-i;
            out.write(buf, i, len);
            expected += len;
        }
        System.out.format("Bytes written: %d, expected: %d\n", bytesWritten,
            expected);
        if (bytesWritten != expected)
            throw new RuntimeException("incorrect number of bytes written");
    }
}
