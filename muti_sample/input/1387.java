public class Size {
    private static Random generator = new Random();
    private static File blah;
    public static void main(String[] args) throws Exception {
        test1();
        test2();
    }
    private static void test1() throws Exception {
        blah = File.createTempFile("blah", null);
        blah.deleteOnExit();
        for(int i=0; i<100; i++) {
            long testSize = generator.nextInt(1000);
            initTestFile(blah, testSize);
            FileInputStream fis = new FileInputStream(blah);
            FileChannel c = fis.getChannel();
            if (c.size() != testSize)
                throw new RuntimeException("Size failed");
            c.close();
            fis.close();
        }
        blah.delete();
    }
    private static void test2() throws Exception {
        String osName = System.getProperty("os.name");
        if (osName.startsWith("SunOS")) {
            blah = File.createTempFile("blah", null);
            long testSize = ((long)Integer.MAX_VALUE) * 2;
            initTestFile(blah, 10);
            RandomAccessFile raf = new RandomAccessFile(blah, "rw");
            FileChannel fc = raf.getChannel();
            fc.size();
            fc.map(FileChannel.MapMode.READ_WRITE, testSize, 10);
            if (fc.size() != testSize + 10)
                throw new RuntimeException("Size failed " + fc.size());
            fc.close();
            raf.close();
            blah.delete();
        }
    }
    private static void initTestFile(File blah, long size) throws Exception {
        if (blah.exists())
            blah.delete();
        FileOutputStream fos = new FileOutputStream(blah);
        BufferedWriter awriter
            = new BufferedWriter(new OutputStreamWriter(fos, "8859_1"));
        for(int i=0; i<size; i++) {
            awriter.write("e");
        }
        awriter.flush();
        awriter.close();
    }
}
