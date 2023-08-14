public class CanEncode {
    private static int errors = 0;
    private static PrintStream out = System.err;
    private static void wrong(CharsetEncoder ce, boolean can, String what) {
        out.println(ce.charset().name()
                    + ": Wrong answer for " + what
                    + ": " + !can);
        errors++;
    }
    private static void ck(CharsetEncoder ce, char c, boolean can)
        throws Exception
    {
        if (ce.canEncode(c) != can)
            wrong(ce, can,
                  ("'" + c + "' (0x"
                   + Integer.toHexString(c & 0xffff) + ")"));
    }
    private static void ck(CharsetEncoder ce, String s, boolean can)
        throws Exception
    {
        if (ce.canEncode(CharBuffer.wrap(s.toCharArray())) != can)
            wrong(ce, can, "array \"" + s + "\"");
        if (ce.canEncode(CharBuffer.wrap(s)) != can)
            wrong(ce, can, "buffer  \"" + s + "\"");
    }
    private static void test(String csn) throws Exception {
        Charset cs = Charset.forName(csn);
        CharsetEncoder ce = cs.newEncoder();
        if (cs.name().equals("US-ASCII")) {
            ck(ce, 'x', true);
            ck(ce, '\u00B6', false);
            ck(ce, "x", true);
            ck(ce, "\u00B6", false);
            ck(ce, "xyzzy", true);
            ck(ce, "xy\u00B6", false);
        }
        ck(ce, '\ud800', false);
        ck(ce, '\ud801', false);
        ck(ce, '\udffe', false);
        ck(ce, '\udfff', false);
        ck(ce, "\ud800", false);
        ck(ce, "\ud801", false);
        ck(ce, "\udffe", false);
        ck(ce, "\udfff", false);
    }
    public static void main(String[] args) throws Exception {
        test("US-ASCII");
        test("UTF-8");
    }
}
