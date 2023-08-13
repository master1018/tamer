public class TestJIS0208Decoder {
    static String outputString = "\u65e5\u672c\u8a9e\u30c6\u30ad\u30b9\u30c8";
    static byte [] inputBytes = new byte[] {(byte)'F', (byte)'|', (byte)'K', (byte)'\\',
                                     (byte)'8', (byte)'l', (byte)'%', (byte)'F',
                                     (byte)'%', (byte)'-', (byte)'%', (byte)'9',
                                     (byte)'%', (byte)'H'};
    public static void main(String args[])
        throws Exception
    {
        test();
    }
    private static void test()
        throws Exception
    {
        CharsetDecoder dec = Charset.forName("JIS0208").newDecoder();
        try {
            String ret = dec.decode(ByteBuffer.wrap(inputBytes)).toString();
            if (ret.length() != outputString.length()
                || ! outputString.equals(ret)){
                throw new Exception("ByteToCharJIS0208 does not work correctly");
            }
        }
        catch (Exception e){
            throw new Exception("ByteToCharJIS0208 does not work correctly");
        }
    }
}
