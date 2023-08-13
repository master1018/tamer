public class TestISO2022JPEncoder {
    static char[] inputChars = {'\u0020', '\u0020', '\u0020', '\u0020',
                                '\u0020', '\u0020', '\u0020', '\u0020',
                                '\u0020', '\u4e00'};
    static byte[] expectedBytes1 = {0x20, 0x20, 0x20, 0x20, 0x20,
                                    0x20, 0x20, 0x20, 0x20};
    static byte[] expectedBytes2 = {0x1b, 0x24, 0x42, 0x30, 0x6c,
                                    0x1b, 0x28, 0x42};
    static byte[] outputBuff = new byte[10];
    public static void main(String args[]) throws Exception {
        CharsetEncoder enc = Charset.forName("ISO2022JP").newEncoder();
        CharBuffer cb = CharBuffer.wrap(inputChars);
        ByteBuffer bb = ByteBuffer.wrap(outputBuff);
        CoderResult cr = enc.encode(cb, bb, false);
        if (!cr.isOverflow())
            throw new Exception("Expected CodeResult.OVERFLOW was not returned");
        for (int i = 0; i < expectedBytes1.length; ++i) {
            if (expectedBytes1[i] != outputBuff[i]) {
                throw new Exception("Output bytes does not match at first conversion");
            }
        }
        int nci = cb.position();
        if (nci != expectedBytes1.length)
            throw new Exception("Output length does not match at first conversion");
        bb.clear();
        cr = enc.encode(cb, bb, true);
        enc.flush(bb);
        bb.flip();
        int len = bb.remaining();
        if (len != expectedBytes2.length)
            throw new Exception("Output length does not match at second conversion");
        for (int i = 0; i < expectedBytes2.length; ++i) {
            if (expectedBytes2[i] != outputBuff[i]) {
                throw new Exception("Output bytes does not match at second conversion");
            }
        }
    }
}
