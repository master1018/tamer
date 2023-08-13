public class EasternArabicTest {
    static NumericShaper ns_old, ns_new;
    static boolean err = false;
    static String[][] testData = {
        {"\u0623\u0643\u062a\u0648\u0628\u0631 10",
         "\u0623\u0643\u062a\u0648\u0628\u0631 \u06f1\u06f0"}, 
        {"\u0b86\u0ba3\u0bcd\u0b9f\u0bc1 2009",
         "\u0b86\u0ba3\u0bcd\u0b9f\u0bc1 \u0be8\u0be6\u0be6\u0bef"},
        {"\u1200 2009",
         "\u1200 \u136a00\u1371"},
    };
    public static void main(String[] args) {
        ns_old = getContextualShaper(TAMIL|ETHIOPIC|EASTERN_ARABIC|ARABIC|THAI|LAO,
                                     EUROPEAN);
        ns_new = getContextualShaper(EnumSet.of(Range.THAI,
                                                Range.TAMIL,
                                                Range.ETHIOPIC,
                                                Range.EASTERN_ARABIC,
                                                Range.ARABIC,
                                                Range.LAO),
                                     Range.EUROPEAN);
        StringBuilder cData = new StringBuilder();
        StringBuilder cExpected = new StringBuilder();
        for (int i = 0; i < testData.length; i++) {
            String data = testData[i][0];
            String expected = testData[i][1];
            test(data, expected);
            cData.append(data).append(' ');
            cExpected.append(expected).append(' ');
        }
        test(cData.toString(), cExpected.toString());
        if (err) {
            throw new RuntimeException("shape() returned unexpected value.");
        }
    }
    private static void test(String data, String expected) {
        char[] text = data.toCharArray();
        ns_old.shape(text, 0, text.length);
        String got = new String(text);
        if (!expected.equals(got)) {
            err = true;
            System.err.println("Error with traditional range.");
            System.err.println("  text = " + data);
            System.err.println("  got = " + got);
            System.err.println("  expected = " + expected);
        } else {
            System.err.println("OK with traditional range.");
            System.err.println("  text = " + data);
            System.err.println("  got = " + got);
            System.err.println("  expected = " + expected);
        }
        text = data.toCharArray();
        ns_new.shape(text, 0, text.length);
        got = new String(text);
        if (!expected.equals(got)) {
            err = true;
            System.err.println("Error with new Enum range.");
            System.err.println("  text = " + data);
            System.err.println("  got = " + got);
            System.err.println("  expected = " + expected);
        } else {
            System.err.println("OK with new Enum range.");
            System.err.println("  text = " + data);
            System.err.println("  got = " + got);
            System.err.println("  expected = " + expected);
        }
    }
}
