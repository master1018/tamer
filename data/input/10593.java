public class Force {
    public static void main(String[] args) throws Exception {
        writeAfterForce();
        forceReadableOnly();
    }
    private static void writeAfterForce() throws Exception {
        byte[] srcData = new byte[20];
        File blah = File.createTempFile("blah", null);
        blah.deleteOnExit();
        FileOutputStream fis = new FileOutputStream(blah);
        FileChannel fc = fis.getChannel();
        fc.write(ByteBuffer.wrap(srcData));
        fc.force(false);
        fc.write(ByteBuffer.wrap(srcData));
        fc.close();
    }
    private static void forceReadableOnly() throws Exception {
        File f = File.createTempFile("blah", null);
        f.deleteOnExit();
        FileInputStream fis = new FileInputStream(f);
        FileChannel fc = fis.getChannel();
        fc.force(true);
        fc.close();
        fis.close();
    }
}
