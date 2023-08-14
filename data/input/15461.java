public class MapAssertions {
    public static void main(String[] args) throws Exception {
        File blah = File.createTempFile("blah", null);
        blah.deleteOnExit();
        RandomAccessFile raf = new RandomAccessFile(blah, "r");
        FileChannel fc = raf.getChannel();
        long fileSize = fc.size();
        MappedByteBuffer mapBuf =
            fc.map(FileChannel.MapMode.READ_ONLY, 0l, fileSize);
        fc.close();
        raf.close();
    }
}
