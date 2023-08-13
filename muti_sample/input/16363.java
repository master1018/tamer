public class MapTest {
    private static PrintStream err = System.err;
    private static Random generator = new Random();
    private static int CHARS_PER_LINE = File.separatorChar == '/' ? 5 : 6;
    private static File blah;
    public static void main(String[] args) throws Exception {
        blah = File.createTempFile("blah", null);
        blah.deleteOnExit();
        initTestFile(blah);
        err.println("Test file " + blah + " initialized");
        testZero();
        err.println("Zero size: OK");
        testRead();
        err.println("Read: OK");
        testWrite();
        err.println("Write: OK");
        testHighOffset();
        err.println("High offset: OK");
    }
    private static void initTestFile(File blah) throws Exception {
        FileOutputStream fos = new FileOutputStream(blah);
        BufferedWriter awriter
            = new BufferedWriter(new OutputStreamWriter(fos, "8859_1"));
        for(int i=0; i<4000; i++) {
            String number = new Integer(i).toString();
            for (int h=0; h<4-number.length(); h++)
                awriter.write("0");
            awriter.write(""+i);
            awriter.newLine();
        }
       awriter.flush();
       awriter.close();
    }
    private static void testZero() throws Exception {
        FileInputStream fis = new FileInputStream(blah);
        FileChannel c = fis.getChannel();
        MappedByteBuffer b = c.map(FileChannel.MapMode.READ_ONLY, 0, 0);
        c.close();
        fis.close();
    }
    private static void testRead() throws Exception {
        StringBuffer sb = new StringBuffer();
        sb.setLength(4);
        for (int x=0; x<1000; x++) {
            FileInputStream fis = new FileInputStream(blah);
            FileChannel c = fis.getChannel();
            long offset = generator.nextInt(10000);
            long expectedResult = offset / CHARS_PER_LINE;
            offset = expectedResult * CHARS_PER_LINE;
            MappedByteBuffer b = c.map(FileChannel.MapMode.READ_ONLY,
                                       offset, 100);
            for (int i=0; i<4; i++) {
                byte aByte = b.get(i);
                sb.setCharAt(i, (char)aByte);
            }
            int result = Integer.parseInt(sb.toString());
            if (result != expectedResult) {
                err.println("I expected "+expectedResult);
                err.println("I got "+result);
                throw new Exception("Read test failed");
            }
            c.close();
            fis.close();
        }
    }
    private static void testWrite() throws Exception {
        StringBuffer sb = new StringBuffer();
        sb.setLength(4);
        for (int x=0; x<1000; x++) {
            RandomAccessFile raf = new RandomAccessFile(blah, "rw");
            FileChannel c = raf.getChannel();
            long offset = generator.nextInt(1000);
            MappedByteBuffer b = c.map(FileChannel.MapMode.READ_WRITE,
                                       offset, 100);
            for (int i=0; i<4; i++) {
                b.put(i, (byte)('0' + i));
            }
            for (int i=0; i<4; i++) {
                byte aByte = b.get(i);
                sb.setCharAt(i, (char)aByte);
            }
            if (!sb.toString().equals("0123"))
                throw new Exception("Write test failed");
            c.close();
            raf.close();
        }
    }
    private static void testHighOffset() throws Exception {
        StringBuffer sb = new StringBuffer();
        sb.setLength(4);
        for (int x=0; x<1000; x++) {
            RandomAccessFile raf = new RandomAccessFile(blah, "rw");
            FileChannel fc = raf.getChannel();
            long offset = 66000;
            MappedByteBuffer b = fc.map(FileChannel.MapMode.READ_WRITE,
                                        offset, 100);
            fc.close();
            raf.close();
        }
    }
}
