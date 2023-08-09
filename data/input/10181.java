public class TestUnmappableForLength {
    public static void main(String[] argv) throws CharacterCodingException {
            byte[] ba = {(byte)0xa2, (byte)0xff};
            testDecode("EUC_TW", ba, 2);
            testDecode("EUC_CN", ba, 2);
    }
    static void testDecode(String csName, byte[] ba, int expected)
        throws CharacterCodingException
    {
        try {
            CoderResult cr = Charset
                .forName(csName)
                .newDecoder()
                .decode(ByteBuffer.wrap(ba), CharBuffer.allocate(4), true);
            if (cr.isUnmappable() && cr.length() != expected) {
                throw new CharacterCodingException();
            }
        } catch (IllegalArgumentException x){
            x.printStackTrace();
        }
    }
}
