public class NotConnected {
    public static void main( String[] argv ) throws Exception {
        PipedInputStream i = new PipedInputStream();
        PipedOutputStream o = new PipedOutputStream();
        boolean readPassed = false;
        boolean writePassed = false;
        try {
            i.read();
        } catch (IOException e) {
            readPassed = true;
        }
        try {
            o.write(10);
        } catch (IOException e) {
            writePassed = true;
        }
        if (!readPassed || !writePassed) {
            throw new Exception("Test failed: IOException not thrown" );
        }
    }
}
