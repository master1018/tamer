public class ReadToLimit {
    public static void main(String[] args) throws Exception {
        File blah = File.createTempFile("blah", null);
        blah.deleteOnExit();
        initTestFile(blah);
        ByteBuffer[] dstBuffers = new ByteBuffer[2];
        for(int i=0; i<2; i++) {
            dstBuffers[i] = ByteBuffer.allocateDirect(10);
            dstBuffers[i].limit(5);
        }
        FileInputStream fis = new FileInputStream(blah);
        FileChannel fc = fis.getChannel();
        long bytesRead = fc.read(dstBuffers);
        for(int i=0; i<2; i++)
            if (dstBuffers[i].position() != 5)
                throw new Exception("Test failed");
        fc.close();
        fis.close();
        blah.delete();
    }
    private static void initTestFile(File blah) throws Exception {
        FileOutputStream fos = new FileOutputStream(blah);
        BufferedWriter awriter
            = new BufferedWriter(new OutputStreamWriter(fos, "8859_1"));
        for(int i=0; i<4; i++) {
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
