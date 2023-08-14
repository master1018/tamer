public class ReadFull {
    public static void main(String[] args) throws Exception {
        File blah = File.createTempFile("blah", null);
        blah.deleteOnExit();
        ByteBuffer[] dstBuffers = new ByteBuffer[10];
        for(int i=0; i<10; i++) {
            dstBuffers[i] = ByteBuffer.allocateDirect(10);
            dstBuffers[i].position(10);
        }
        FileInputStream fis = new FileInputStream(blah);
        FileChannel fc = fis.getChannel();
        long bytesRead = fc.read(dstBuffers) ;
        if (bytesRead != 0)
            throw new RuntimeException("Nonzero return from read");
        fc.close();
        fis.close();
        blah.delete();
    }
}
