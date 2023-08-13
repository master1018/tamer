public class MapOverEnd {
    public static void main (String [] args) throws Exception {
        File blah = File.createTempFile("blah", null);
        blah.deleteOnExit();
        RandomAccessFile raf = new RandomAccessFile (blah, "rw");
        FileChannel fc = raf.getChannel();
        MappedByteBuffer map = fc.map(FileChannel.MapMode.READ_WRITE, 0, 2048);
        fc.close();
        double current = map.getDouble (50);
        map.putDouble (50, current+0.1d);
        map.force();
    }
}
