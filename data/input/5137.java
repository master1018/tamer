public class MultipleConnect {
    public static void main(String[] argv) throws Exception {
        PipedOutputStream os = new PipedOutputStream();
        PipedOutputStream os2 = new PipedOutputStream();
        PipedInputStream is = new PipedInputStream();
        os.connect(is);
        try {
            is.connect(os2);
            throw new Exception("Test failed: IOException expected");
        } catch(IOException e) {
        }
    }
}
