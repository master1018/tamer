public class Write {
    public static void main(String[] args) throws Exception {
        byte[] bb = new byte[3];
        File testFile = File.createTempFile("test1", null);
        testFile.deleteOnExit();
        FileOutputStream fos = new FileOutputStream(testFile);
        FileChannel fc = fos.getChannel();
        OutputStream out = Channels.newOutputStream(fc);
        out.write(bb,0,1);
        out.write(bb,2,1);
        out.close();
        fc.close();
        fos.close();
        testFile.delete();
    }
}
