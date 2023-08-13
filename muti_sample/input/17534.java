public class ReadAfterClose {
    static void testRead(InputStream in) throws Exception {
        in.close();
        byte[] buf = new byte[2];
        try {
            in.read(buf, 0, 1);
            throw new Exception("Should not allow read on a closed stream");
        } catch (IOException e) {
        }
        try {
            in.read(buf, 0, 0);
            throw new Exception("Should not allow read on a closed stream");
        } catch (IOException e) {
        }
    }
    public static void main(String argv[]) throws Exception {
        BufferedInputStream bis = new BufferedInputStream
            (new ByteArrayInputStream(new byte[32]));
        testRead(bis);
    }
}
