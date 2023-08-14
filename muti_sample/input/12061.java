public class IsLegalReplacement {
    static PrintStream out = System.err;
    static int errors = 0;
    static String toString(byte[] ba) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < ba.length; i++) {
            byte b = ba[i];
            if (i > 0)
                sb.append(' ');
            sb.append(Integer.toHexString((b >> 4) & 0xf));
            sb.append(Integer.toHexString((b >> 0) & 0xf));
        }
        return sb.toString();
    }
    static CoderResult ilr(String csn, byte[] repl) {
        CharsetDecoder dec = Charset.forName(csn).newDecoder();
        dec.onMalformedInput(CodingErrorAction.REPORT);
        dec.onUnmappableCharacter(CodingErrorAction.REPORT);
        ByteBuffer bb = ByteBuffer.wrap(repl);
        CharBuffer cb = CharBuffer.allocate((int)(bb.remaining()
                                                  * dec.maxCharsPerByte()));
        return dec.decode(bb, cb, true);
    }
    static void test(String csn, byte[] repl, boolean expected)
        throws Exception
    {
        CharsetEncoder enc = Charset.forName(csn).newEncoder();
        out.print(csn + ": " + toString(repl) + ": ");
        if (enc.isLegalReplacement(repl) == expected) {
            out.print("Okay");
        } else {
            out.print("Wrong: Expected " + expected);
            errors++;
        }
        out.println(" (" + ilr(csn, repl) + ")");
    }
    public static void main(String[] args) throws Exception {
        test("UTF-16", new byte [] { (byte)0xd8, 0, (byte)0xdc, 0 }, true);
        test("UTF-16", new byte [] { (byte)0xdc, 0, (byte)0xd8, 0 }, false);
        test("UTF-16", new byte [] { (byte)0xd8, 0 }, false);
        test("UTF-16BE", new byte [] { (byte)0xd8, 0, (byte)0xdc, 0 }, true);
        test("UTF-16BE", new byte [] { (byte)0xdc, 0, (byte)0xd8, 0 }, false);
        test("UTF-16BE", new byte [] { (byte)0xd8, 0 }, false);
        test("UTF-16LE", new byte [] { 0, (byte)0xd8, 0, (byte)0xdc }, true);
        test("UTF-16LE", new byte [] { 0, (byte)0xdc, 0, (byte)0xd8 }, false);
        test("UTF-16LE", new byte [] { 0, (byte)0xd8 }, false);
        if (errors > 0)
            throw new Exception(errors + " error(s) occurred");
    }
}
