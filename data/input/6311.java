public class SkipEOL {
    public static void main( String argv[] ) throws Exception {
        byte[] data = {12, 13, 10, 23, 11, 13, 12, 10, 13};
        byte[] expected = {12, 10, 23, 11, 10, 12, 10, 10};
        LineNumberInputStream in =
            new LineNumberInputStream(new ByteArrayInputStream(data));
        long skipped = in.skip(3); 
        if ((skipped == 3) && ((in.read()) != 11)) {
            throw new
                RuntimeException("LineNumberInputStream.skip - " +
                                 "unexpected results!");
        }
        in = new LineNumberInputStream(new ByteArrayInputStream(data));
        for (int i = 0; i < 8; i++) {
            if (in.read() != expected[i]) {
                 throw new
                     RuntimeException("LineNumberInputStream.read - " +
                                      "unexpected results!");
            }
        }
        System.err.println("Test completed successfully");
    }
}
