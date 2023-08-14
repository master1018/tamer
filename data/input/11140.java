public class Pread {
    private static PrintStream err = System.err;
    private static Random generator = new Random();
    private static int CHARS_PER_LINE = File.separatorChar == '/' ? 5 : 6;
    public static void main(String[] args) throws Exception {
        genericTest();
        testNegativePosition(); 
        testUnreadableChannel();
    }
    private static void testNegativePosition() throws Exception {
        File blah = File.createTempFile("blah1", null);
        blah.deleteOnExit();
        FileOutputStream fos = new FileOutputStream(blah);
        fos.write(new byte[128]);
        fos.close();
        FileChannel fc = (new FileInputStream(blah)).getChannel();
        try {
            fc.read(ByteBuffer.allocate(256), -1L);
            throw new RuntimeException("Expected exception not thrown");
        } catch(IllegalArgumentException e) {
        } finally {
            fc.close();
            blah.delete();
        }
    }
    private static void testUnreadableChannel() throws Exception {
        File blah = File.createTempFile("blah2", null);
        blah.deleteOnExit();
        FileOutputStream fos = new FileOutputStream(blah);
        try {
            fos.write(new byte[128]);
            FileChannel fc = fos.getChannel();
            try {
                fc.read(ByteBuffer.allocate(256),1);
                throw new RuntimeException("Expected exception not thrown");
            } catch(NonReadableChannelException e) {
            }
        } finally {
            fos.close();
            blah.delete();
        }
    }
    private static void genericTest() throws Exception {
        StringBuffer sb = new StringBuffer();
        sb.setLength(4);
        File blah = File.createTempFile("blah3", null);
        blah.deleteOnExit();
        initTestFile(blah);
        FileInputStream fis = new FileInputStream(blah);
        FileChannel c = fis.getChannel();
        for (int x=0; x<100; x++) {
            long offset = generator.nextInt(1000);
            long expectedResult = offset / CHARS_PER_LINE;
            offset = expectedResult * CHARS_PER_LINE;
            ByteBuffer bleck = ByteBuffer.allocateDirect(4);
            long originalPosition = c.position();
            int totalRead = 0;
            while (totalRead < 4) {
                int read = c.read(bleck, offset);
                if (read < 0)
                    throw new Exception("Read failed");
                totalRead += read;
            }
            long newPosition = c.position();
            for (int i=0; i<4; i++) {
                byte aByte = bleck.get(i);
                sb.setCharAt(i, (char)aByte);
            }
            int result = Integer.parseInt(sb.toString());
            if (result != expectedResult) {
                err.println("I expected "+ expectedResult);
                err.println("I got "+ result);
                throw new Exception("Read test failed");
            }
            if (originalPosition != newPosition)
                throw new Exception("File position modified");
        }
        c.close();
        fis.close();
        blah.delete();
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
}
