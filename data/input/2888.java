public class TestUTF_16 {
    private static void testDecode(String charset,
                                   String expected,
                                   byte[] input)
    throws Exception
    {
        String out = new String(input, charset);
        if (!out.equals(expected)) {
            failureReport (out, expected);
            throw new Exception("UTF_16 Decoding test failed " + charset);
        }
    }
    private static void testEncode(String charset,
                                   String input,
                                   byte[] expected)
    throws Exception
    {
        byte[] testBytes = input.getBytes(charset);
        for (int i = 0; i< expected.length; i++)
            if (testBytes[i] != expected[i])
                throw new Exception("UTF_16 Encoding test failed " + charset);
    }
    private static void warn(String s) {
        System.err.println("FAILED Test 4403848 UTF-16" +
                            s) ;
    }
    private static void failureReport(String testStr,
                                      String expected) {
        System.err.println ("Expected Characters:");
        for (int i = 0; i < expected.length() ; i++) {
            warn("expected char[" + i + "] : " +
                  Integer.toHexString((int)expected.charAt(i)) +
                 "obtained char[" + i + "] : " +
                  Integer.toHexString((int)testStr.charAt(i)));
        }
    }
    private static void test() throws Exception  {
            testDecode("UTF_16BE", "\u0092\u0093",
                        new byte[] { (byte) 0x00, (byte) 0x92,
                                     (byte) 0x00, (byte) 0x93 });
            testDecode("UTF_16BE", "\ufeff\u0092\u0093",
                        new byte[] { (byte) 0xfe, (byte) 0xff,
                                     (byte) 0x00, (byte) 0x92,
                                     (byte) 0x00, (byte) 0x93 });
            testDecode("UTF_16LE", "\u9200\u9300",
                        new byte[] { (byte) 0x00, (byte) 0x92,
                                     (byte) 0x00, (byte) 0x93 });
            testDecode("UTF_16LE", "\ufeff\u9200\u9300",
                        new byte[] { (byte) 0xff, (byte) 0xfe,
                                     (byte) 0x00, (byte) 0x92,
                                     (byte) 0x00, (byte) 0x93 });
            testDecode("UTF-16", "\u9200\u9300",
                        new byte[] { (byte) 0xfe, (byte) 0xff,
                                     (byte) 0x92, (byte) 0x00,
                                     (byte) 0x93, (byte) 0x00 });
            testDecode("UTF-16", "\u9200\u9300",
                        new byte[] { (byte) 0x92, (byte) 0x00,
                                     (byte) 0x93, (byte) 0x00 });
            testEncode("UTF-16", "\u0123",
                        new byte[] { (byte) 0xfe, (byte) 0xff,
                                     (byte) 0x01, (byte) 0x23 });
            if (CoderResult.OVERFLOW !=
                Charset.forName("UTF_16")
                .newDecoder()
                .decode((ByteBuffer)(ByteBuffer.allocate(4)
                                     .put(new byte[]
                                          {(byte)0xd8,(byte)0x00,
                                           (byte)0xdc,(byte)0x01})
                                     .flip()),
                        CharBuffer.allocate(1),
                        true)) {
                throw new Exception ("REGTEST TestUTF16 Overflow test failed");
            }
            testDecode("UnicodeLittle", "Arial",
                        new byte[] { 'A', 0, 'r', 0, 'i', 0, 'a', 0, 'l', 0});
            System.err.println ("\nPASSED UTF-16 encoder test");
          System.err.println ("OVERALL PASS OF UTF-16 Test");
   }
   public static void main (String[] args) throws Exception {
     test();
   }
}
