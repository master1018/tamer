public class ToUpperCase {
    public static void main(String[] args) {
        Locale turkish = new Locale("tr", "TR");
        Locale lt = new Locale("lt"); 
        Locale az = new Locale("az"); 
        test("\u00DF", turkish, "SS");
        test("a\u00DF", turkish, "ASS");
        test("i", turkish, "\u0130");
        test("i", az, "\u0130");
        test("\u0131", turkish, "I");
        test("\u00DF", Locale.GERMANY, "SS");
        test("a\u00DF", Locale.GERMANY, "ASS");
        test("i", Locale.GERMANY, "I");
        test("abc\u00DF", Locale.US, "ABC\u0053\u0053");
        test("\u0149abc", Locale.US, "\u02BC\u004EABC");
        test("\u0149abc", turkish, "\u02BC\u004EABC");
        test("\u1F52", Locale.US, "\u03A5\u0313\u0300");
        test("\u0149\u1F52", Locale.US, "\u02BC\u004E\u03A5\u0313\u0300");
        test("\u1F54ZZZ", Locale.US, "\u03A5\u0313\u0301ZZZ");
        test("\u1F54ZZZ", turkish, "\u03A5\u0313\u0301ZZZ");
        test("a\u00DF\u1F56", Locale.US, "ASS\u03A5\u0313\u0342");
        test("\u1FAD", turkish, "\u1F6D\u0399");
        test("i\u1FC7", turkish, "\u0130\u0397\u0342\u0399");
        test("i\u1FC7", az, "\u0130\u0397\u0342\u0399");
        test("i\u1FC7", Locale.US, "I\u0397\u0342\u0399");
        test("\uFB04", Locale.US, "\u0046\u0046\u004C");
        test("\uFB17AbCdEfi", turkish, "\u0544\u053DABCDEF\u0130");
        test("\uFB17AbCdEfi", az, "\u0544\u053DABCDEF\u0130");
        test("i\u0307", lt, "I");
        test("\u0307", lt, "\u0307");
        test("\u0307i", lt, "\u0307I");
        test("j\u0307", lt, "J");
        test("abci\u0307def", lt, "ABCIDEF");
        test("a\u0307", lt, "A\u0307");
        test("abc\u0307def", lt, "ABC\u0307DEF");
        test("i\u0307", Locale.US, "I\u0307");
        test("i\u0307", turkish, "\u0130\u0307");
        test("\uD801\uDC28\uD801\uDC29\uD801\uDC2A", Locale.US, "\uD801\uDC00\uD801\uDC01\uD801\uDC02");
        test("\uD801\uDC28a\uD801\uDC29b\uD801\uDC2Ac", Locale.US, "\uD801\uDC00A\uD801\uDC01B\uD801\uDC02C");
        test("\uD800\uD800\uD801a\uDC00\uDC00\uDC00b", Locale.US, "\uD800\uD800\uD801A\uDC00\uDC00\uDC00B");
    }
    static void test(String in, Locale locale, String expected) {
        String result = in.toUpperCase(locale);
        if (!result.equals(expected)) {
            System.err.println("input: " + in + ", locale: " + locale +
                    ", expected: " + expected + ", actual: " + result);
            throw new RuntimeException();
        }
   }
}
