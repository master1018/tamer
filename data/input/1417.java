public class ScatteringRead {
    private static final int NUM_BUFFERS = 3;
    private static final int BUFFER_CAP = 3;
    private static final int BIG_BUFFER_CAP = Integer.MAX_VALUE / 3 + 10;
    public static void main(String[] args) throws Exception {
        test1(); 
        test2(); 
        System.gc();
    }
    private static void test1() throws Exception {
        ByteBuffer dstBuffers[] = new ByteBuffer[NUM_BUFFERS];
        for (int i=0; i<NUM_BUFFERS; i++)
            dstBuffers[i] = ByteBuffer.allocateDirect(BUFFER_CAP);
        File blah = File.createTempFile("blah1", null);
        blah.deleteOnExit();
        createTestFile(blah);
        FileInputStream fis = new FileInputStream(blah);
        FileChannel fc = fis.getChannel();
        byte expectedResult = -128;
        for (int k=0; k<20; k++) {
            long bytesRead = fc.read(dstBuffers);
            for (int i=0; i<NUM_BUFFERS; i++) {
                for (int j=0; j<BUFFER_CAP; j++) {
                    byte b = dstBuffers[i].get(j);
                    if (b != expectedResult++)
                        throw new RuntimeException("Test failed");
                }
                dstBuffers[i].flip();
            }
        }
        fis.close();
    }
    private static void createTestFile(File blah) throws Exception {
        FileOutputStream fos = new FileOutputStream(blah);
        for(int i=-128; i<128; i++)
            fos.write((byte)i);
        fos.flush();
        fos.close();
    }
    private static void test2() throws Exception {
        ByteBuffer dstBuffers[] = new ByteBuffer[2];
        for (int i=0; i<2; i++)
            dstBuffers[i] = ByteBuffer.allocateDirect(10);
        File blah = File.createTempFile("blah2", null);
        blah.deleteOnExit();
        FileOutputStream fos = new FileOutputStream(blah);
        for(int i=0; i<15; i++)
            fos.write((byte)92);
        fos.flush();
        fos.close();
        FileInputStream fis = new FileInputStream(blah);
        FileChannel fc = fis.getChannel();
        long bytesRead = fc.read(dstBuffers);
        if (dstBuffers[1].limit() != 10)
            throw new Exception("Scattering read changed buf limit.");
        fis.close();
    }
    private static void test3() throws Exception {
        String osName = System.getProperty("os.name");
        if (!osName.startsWith("SunOS"))
            return;
        String dataModel = System.getProperty("sun.arch.data.model");
        if (!dataModel.startsWith("64"))
            return;
        ByteBuffer dstBuffers[] = new ByteBuffer[NUM_BUFFERS];
        File f = File.createTempFile("test3", null);
        f.deleteOnExit();
        prepTest3File(f, (long)BIG_BUFFER_CAP);
        RandomAccessFile raf = new RandomAccessFile(f, "rw");
        FileChannel fc = raf.getChannel();
        MappedByteBuffer mbb = fc.map(FileChannel.MapMode.READ_WRITE, 0,
                                      BIG_BUFFER_CAP);
        for (int i=0; i<NUM_BUFFERS; i++) {
            dstBuffers[i] = mbb;
        }
        fc.close();
        raf.close();
        FileInputStream fis = new FileInputStream("/dev/zero");
        fc = fis.getChannel();
        long bytesRead = fc.read(dstBuffers);
        if (bytesRead <= Integer.MAX_VALUE)
            throw new RuntimeException("Test 3 failed "+bytesRead+" < "+Integer.MAX_VALUE);
        fc.close();
        fis.close();
    }
    static void prepTest3File(File blah, long testSize) throws Exception {
        RandomAccessFile raf = new RandomAccessFile(blah, "rw");
        FileChannel fc = raf.getChannel();
        fc.write(ByteBuffer.wrap("Use the source!".getBytes()), testSize - 40);
        fc.close();
        raf.close();
    }
}
