public class Pwrite {
    private static Random generator = new Random();
    private static File blah;
    public static void main(String[] args) throws Exception {
        genericTest();
        testUnwritableChannel();
    }
    private static void testUnwritableChannel() throws Exception {
        File blah = File.createTempFile("blah2", null);
        blah.deleteOnExit();
        FileOutputStream fos = new FileOutputStream(blah);
        fos.write(new byte[128]);
        fos.close();
        FileInputStream fis = new FileInputStream(blah);
        FileChannel fc = fis.getChannel();
        try {
            fc.write(ByteBuffer.allocate(256),1);
            throw new RuntimeException("Expected exception not thrown");
        } catch(NonWritableChannelException e) {
        } finally {
            fc.close();
            blah.delete();
        }
    }
    private static void genericTest() throws Exception {
        StringBuffer sb = new StringBuffer();
        sb.setLength(4);
        blah = File.createTempFile("blah", null);
        blah.deleteOnExit();
        initTestFile(blah);
        RandomAccessFile raf = new RandomAccessFile(blah, "rw");
        FileChannel c = raf.getChannel();
        for (int x=0; x<100; x++) {
            long offset = generator.nextInt(1000);
            ByteBuffer bleck = ByteBuffer.allocateDirect(4);
            for (byte i=0; i<4; i++) {
                bleck.put(i);
            }
            bleck.flip();
            long originalPosition = c.position();
            int totalWritten = 0;
            while (totalWritten < 4) {
                int written = c.write(bleck, offset);
                if (written < 0)
                    throw new Exception("Read failed");
                totalWritten += written;
            }
            long newPosition = c.position();
            if (originalPosition != newPosition)
                throw new Exception("File position modified");
            bleck = ByteBuffer.allocateDirect(4);
            originalPosition = c.position();
            int totalRead = 0;
            while (totalRead < 4) {
                int read = c.read(bleck, offset);
                if (read < 0)
                    throw new Exception("Read failed");
                totalRead += read;
            }
            newPosition = c.position();
            if (originalPosition != newPosition)
                throw new Exception("File position modified");
            for (byte i=0; i<4; i++) {
                if (bleck.get(i) != i)
                    throw new Exception("Write test failed");
            }
        }
        c.close();
        raf.close();
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
