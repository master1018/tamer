public class ClosedReady {
    public static void main( String argv[] ) throws Exception {
        StringReader in = new StringReader("aaaaaaaaaaaaaaa");
        in.read();
        in.close();
        try {
            in.ready(); 
            throw new RuntimeException(" No exception during read on closed stream");
        }
        catch (IOException e) {
            System.err.println("Test passed: IOException is thrown");
        }
    }
}
