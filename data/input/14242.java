public class Truncate {
    private static final Random generator = new Random();
    public static void main(String[] args) throws Exception {
        File blah = File.createTempFile("blah", null);
        blah.deleteOnExit();
        try {
            basicTest(blah);
            appendTest(blah);
        } finally {
            blah.delete();
        }
    }
    static void basicTest(File blah) throws Exception {
        for(int i=0; i<100; i++) {
            long testSize = generator.nextInt(1000) + 10;
            initTestFile(blah, testSize);
            try (FileChannel fc = (i < 50) ?
                 new RandomAccessFile(blah, "rw").getChannel() :
                 FileChannel.open(blah.toPath(), READ, WRITE))
                {
                    if (fc.size() != testSize)
                        throw new RuntimeException("Size failed");
                    long position = generator.nextInt((int)testSize);
                    fc.position(position);
                    long newSize = generator.nextInt((int)testSize);
                    fc.truncate(newSize);
                    if (fc.size() != newSize)
                        throw new RuntimeException("Truncate failed");
                    if (position > newSize) {
                        if (fc.position() != newSize)
                            throw new RuntimeException("Position greater than size");
                    } else {
                        if (fc.position() != position)
                            throw new RuntimeException("Truncate changed position");
                    };
                }
        }
    }
    static void appendTest(File blah) throws Exception {
        for (int i=0; i<10; i++) {
            long testSize = generator.nextInt(1000) + 10;
            initTestFile(blah, testSize);
            try (FileChannel fc = (i < 5) ?
                 new FileOutputStream(blah, true).getChannel() :
                 FileChannel.open(blah.toPath(), APPEND))
                {
                    long newSize = generator.nextInt((int)testSize);
                    fc.truncate(newSize);
                    if (fc.size() != newSize)
                        throw new RuntimeException("Truncate failed");
                    ByteBuffer buf = ByteBuffer.allocate(1);
                    buf.put((byte)'x');
                    buf.flip();
                    fc.write(buf);
                    if (fc.size() != (newSize+1))
                        throw new RuntimeException("Unexpected size");
                }
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
