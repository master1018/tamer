public class ClosedWrite {
    public static void main(String[] argv) throws Exception {
        PipedOutputStream os = new PipedOutputStream();
        PipedInputStream is = new PipedInputStream();
        os.connect(is);
        os.close();
        try {
            os.write(10);
            throw new
                RuntimeException("No IOException upon write on closed Stream");
        } catch(IOException e) {
            System.err.println("Test passed: IOException thrown");
        }
    }
}
