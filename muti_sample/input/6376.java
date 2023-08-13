public class Release {
    public static void main(String[] args) throws Exception {
        FileOutputStream fos = new FileOutputStream("testFile.tmp");
        fos.write(new byte[128]);
        FileChannel ch = fos.getChannel();
        FileLock fl = ch.lock();
        ch.close();
        try {
            fl.release();
            throw new RuntimeException("Expected exception not thrown");
        } catch (ClosedChannelException cce) {
        }
    }
}
