public class ShapingTest {
    private static boolean err = false;
    public static void main(String[] args) {
        test6842557();
        test6943963();
        test6903266();
        if (err) {
            throw new RuntimeException("shape() returned unexpected value.");
        }
    }
    private static void test6842557() {
        NumericShaper ns_old = getContextualShaper(ARABIC | TAMIL | ETHIOPIC,
                                   EUROPEAN);
        NumericShaper ns_new = getContextualShaper(EnumSet.of(
                                   Range.ARABIC, Range.TAMIL, Range.ETHIOPIC),
                                   Range.EUROPEAN);
        String[][] data = {
          {"\u0623\u0643\u062a\u0648\u0628\u0631 10",
           "\u0623\u0643\u062a\u0648\u0628\u0631 \u0661\u0660"},
          {"\u0b86\u0ba3\u0bcd\u0b9f\u0bc1 2009",
           "\u0b86\u0ba3\u0bcd\u0b9f\u0bc1 \u0be8\u0be6\u0be6\u0bef"},
          {"\u1200 2009",
           "\u1200 \u136a00\u1371"},
        };
        for (int i = 0; i < data.length; i++) {
            checkResult("ARABIC | TAMIL | ETHIOPIC",
                        ns_old, data[i][0], data[i][1]);
            checkResult("Range.ARABIC, Range.TAMIL, Range.ETHIOPIC",
                        ns_new, data[i][0], data[i][1]);
        }
    }
    private static void test6943963() {
        NumericShaper ns_dummy = getContextualShaper(ARABIC | TAMIL | ETHIOPIC,
                                   EUROPEAN);
        char[] c = "\u1200 1".toCharArray();
        ns_dummy.shape(c, 0, c.length);
        String given = "\u0627\u0628 456";
        String expected_ARABIC = "\u0627\u0628 \u0664\u0665\u0666";
        String expected_EASTERN_ARABIC = "\u0627\u0628 \u06f4\u06f5\u06f6";
        NumericShaper ns = getContextualShaper(ARABIC);
        checkResult("ARABIC", ns, given, expected_ARABIC);
        ns = getContextualShaper(EnumSet.of(Range.ARABIC));
        checkResult("Range.ARABIC", ns, given, expected_ARABIC);
        ns = getContextualShaper(EASTERN_ARABIC);
        checkResult("EASTERN_ARABIC", ns, given, expected_EASTERN_ARABIC);
        ns = getContextualShaper(EnumSet.of(Range.EASTERN_ARABIC));
        checkResult("Range.EASTERN_ARABIC", ns, given, expected_EASTERN_ARABIC);
        ns = getContextualShaper(ARABIC | EASTERN_ARABIC);
        checkResult("ARABIC | EASTERN_ARABIC", ns, given, expected_EASTERN_ARABIC);
        ns = getContextualShaper(EnumSet.of(Range.ARABIC, Range.EASTERN_ARABIC));
        checkResult("Range.ARABIC, Range.EASTERN_ARABIC", ns, given, expected_EASTERN_ARABIC);
    }
    private static void test6903266() {
        NumericShaper ns = getContextualShaper(EnumSet.of(Range.TAI_THAM_HORA));
        String given = "\u1a20 012";
        String expected = "\u1a20 \u1a80\u1a81\u1a82";
        checkResult("Range.TAI_THAM_HORA", ns, given, expected);
        ns = getContextualShaper(EnumSet.of(Range.TAI_THAM_HORA,
                                            Range.TAI_THAM_THAM));
        given = "\u1a20 012";
        expected = "\u1a20 \u1a90\u1a91\u1a92"; 
        checkResult("Range.TAI_THAM_HORA, Range.TAI_THAM_THAM", ns, given, expected);
        ns = getContextualShaper(EnumSet.of(Range.JAVANESE));
        given = "\ua984 012";
        expected = "\ua984 \ua9d0\ua9d1\ua9d2";
        checkResult("Range.JAVANESE", ns, given, expected);
        ns = getContextualShaper(EnumSet.of(Range.TAI_THAM_THAM));
        given = "\u1a20 012";
        expected = "\u1a20 \u1a90\u1a91\u1a92";
        checkResult("Range.TAI_THAM_THAM", ns, given, expected);
        ns = getContextualShaper(EnumSet.of(Range.MEETEI_MAYEK));
        given = "\uabc0 012";
        expected = "\uabc0 \uabf0\uabf1\uabf2";
        checkResult("Range.MEETEI_MAYEK", ns, given, expected);
    }
    private static void checkResult(String ranges, NumericShaper ns,
                                    String given, String expected) {
        char[] text = given.toCharArray();
        ns.shape(text, 0, text.length);
        String got = new String(text);
        if (!expected.equals(got)) {
            err = true;
            System.err.println("Error with range(s) <" + ranges + ">.");
            System.err.println("  text     = " + given);
            System.err.println("  got      = " + got);
            System.err.println("  expected = " + expected);
        } else {
            System.out.println("OK with range(s) <" + ranges + ">.");
            System.out.println("  text     = " + given);
            System.out.println("  got      = " + got);
            System.out.println("  expected = " + expected);
        }
    }
}
