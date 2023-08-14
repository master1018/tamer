public class ReadOffset {
    public static void main(String[] args) throws IOException {
        ReadableByteChannel rbc = new ReadableByteChannel() {
            public int read(ByteBuffer dst) {
                dst.put((byte)0);
                return 1;
            }
            public boolean isOpen() {
                return true;
            }
            public void close() {
            }
        };
        InputStream in = Channels.newInputStream(rbc);
        byte[] b = new byte[3];
        in.read(b, 0, 1);
        in.read(b, 2, 1);       
    }
}
