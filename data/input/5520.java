public class ClosedStream {
    public static void main( String argv[] ) throws Exception {
        byte[] data = {30,40};
        int b1,b2;
        PushbackInputStream in = new PushbackInputStream(
                                           new ByteArrayInputStream(data));
        in.unread(20);
        in.close();
        try {
            in.read(); 
            throw new RuntimeException("No exception during read on closed stream");
        } catch (IOException e) {
            System.err.println("Test passed: IOException is thrown");
        }
    }
}
