public class Mode {
   private static File testFile;
   public static void main(String[] args) throws Exception {
        testFile = File.createTempFile("testFile", null);
        testFile.deleteOnExit();
        testReadable();
        testWritable();
   }
    private static void testReadable() throws IOException {
        FileInputStream is = new FileInputStream(testFile);
        FileChannel channel = is.getChannel();
        try {
            MappedByteBuffer buff = channel.map(FileChannel.MapMode.READ_WRITE,
                                                0, 8);
            throw new RuntimeException("Exception expected, none thrown");
        } catch (NonWritableChannelException e) {
        }
        is.close();
    }
    private static void testWritable() throws IOException {
        FileOutputStream is = new FileOutputStream(testFile);
        FileChannel channel = is.getChannel();
        try {
            MappedByteBuffer buff = channel.map(FileChannel.MapMode.READ_ONLY,
                                                0, 8);
            throw new RuntimeException("Exception expected, none thrown");
        } catch (NonReadableChannelException e) {
        }
        is.close();
    }
}
