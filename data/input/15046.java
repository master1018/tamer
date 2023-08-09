public class MapReadOnly {
    public static void main (String args[]) throws Exception {
        File testFile = File.createTempFile("test2", null);
        testFile.deleteOnExit();
        RandomAccessFile raf = new RandomAccessFile(testFile, "rw");
        FileChannel fc = raf.getChannel();
        MappedByteBuffer mbb = fc.map(FileChannel.MapMode.READ_ONLY, 0L,
                                      (int) fc.size());
        mbb.load();
        try {
            mbb.put((byte)3);
            throw new Exception("Test failed");
        } catch (ReadOnlyBufferException robe) {
        }
        fc.close();
        raf.close();
    }
}
