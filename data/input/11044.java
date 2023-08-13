public class TestJIS0212Decoder {
    static String outputString = "\u4e02\u4e04\u4e05\u4e0c\u4e12\u4e1f\u4e23";
    static char [] outputChars = new char[8];
    static byte [] inputBytes = new byte[] {(byte)0x30, (byte)0x21, (byte)0x30, (byte)0x22,
                                            (byte)0x30, (byte)0x23, (byte)0x30, (byte)0x24,
                                            (byte)0x30, (byte)0x25, (byte)0x30, (byte)0x26,
                                            (byte)0x30, (byte)0x27};
    public static void main(String args[])
        throws Exception
    {
        test();
    }
    private static void test()
        throws Exception
    {
        CharsetDecoder dec = Charset.forName("JIS0212").newDecoder();
        try {
            String ret = dec.decode(ByteBuffer.wrap(inputBytes)).toString();
            if (ret.length() != outputString.length()
                || ! outputString.equals(ret)){
                throw new Exception("ByteToCharJIS0212 does not work correctly");
            }
        }
        catch (Exception e){
            throw new Exception("ByteToCharJIS0212 does not work correctly");
        }
    }
}
