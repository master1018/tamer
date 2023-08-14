public class BigReadWrite {
    static int testSize = 15;
    public static void main(String[] args) throws Exception {
        String osName = System.getProperty("os.name");
        if (!osName.startsWith("SunOS"))
            return;
        FileOutputStream fos = new FileOutputStream("/dev/zero");
        FileChannel fc = fos.getChannel();
        ByteBuffer buf = ByteBuffer.allocate(900);
        fc.write(buf);
        buf = ByteBuffer.allocate(950);
        fc.write(buf);
        buf = ByteBuffer.allocate(975);
        fc.write(buf);
        buf = ByteBuffer.allocate(4419000);
        long iterations = 0;
        while (iterations < 50) {
            fc.write(buf);
            buf.rewind();
            iterations++;
        }
        fc.close();
    }
}
