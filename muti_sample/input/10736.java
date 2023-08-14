public class ZeroMap {
    public static void main(String[] args) throws Exception {
        Random random = new Random();
        long filesize = random.nextInt(1024*1024);
        int cut = random.nextInt((int)filesize);
        File file = File.createTempFile("Blah", null);
        file.deleteOnExit();
        try (RandomAccessFile raf = new RandomAccessFile(file, "rw")) {
            raf.setLength(filesize);
            FileChannel fc = raf.getChannel();
            MappedByteBuffer mbb = fc.map(FileChannel.MapMode.READ_WRITE, cut, 0);
            mbb.force();
            mbb.load();
            mbb.isLoaded();
       }
        System.gc();
        Thread.sleep(500);
    }
}
