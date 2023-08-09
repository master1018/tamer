public class TestUTF8BOM {
    private static ByteBuffer bf = ByteBuffer.allocateDirect(1000);
    private static void testDecode(String expected, byte[] input)
        throws Exception
    {
        String out = new String(input, "utf8");
        if (!out.equals(expected)) {
            failureReport (out, expected);
            throw new Exception("UTF_8 Decoding test failed");
        }
        bf.clear();
        bf.put(input).flip();
        out = Charset.forName("UTF-8")
                     .decode(bf)
                     .toString();
        if (!out.equals(expected)) {
            failureReport (out, expected);
            throw new Exception("UTF_8 Decoding test failed(directbuffer)");
        }
    }
    private static void failureReport(String testStr,
                                      String expected) {
        System.err.println ("Expected Characters:");
        for (int i = 0; i < expected.length() ; i++) {
            System.out.println("expected char[" + i + "] : " +
                              Integer.toHexString((int)expected.charAt(i)) +
                              "  obtained char[" + i + "] : " +
                              Integer.toHexString((int)testStr.charAt(i)));
        }
    }
    public static void main (String[] args) throws Exception {
            testDecode("\ufeff\u0092\u0093",
                        new byte[] { (byte) 0xef, (byte) 0xbb, (byte) 0xbf,
                                     (byte) 0xc2, (byte) 0x92,
                                     (byte) 0xc2, (byte) 0x93 });
            testDecode("\u9200\ufeff\u9300",
                        new byte[] { (byte) 0xe9, (byte) 0x88, (byte) 0x80,
                                     (byte) 0xef, (byte) 0xbb, (byte) 0xbf,
                                     (byte) 0xe9, (byte) 0x8c, (byte) 0x80 });
            testDecode("\u9200\u9300\ufeff",
                        new byte[] { (byte) 0xe9, (byte) 0x88, (byte) 0x80,
                                     (byte) 0xe9, (byte) 0x8c, (byte) 0x80,
                                     (byte) 0xef, (byte) 0xbb, (byte) 0xbf });
            System.err.println ("\nPASSED UTF-8 decode BOM test");
   }
}
