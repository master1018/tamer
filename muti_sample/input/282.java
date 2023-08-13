public class TestSupplementary extends JavadocTester {
    private static final String BUG_ID = "4914724";
    private static final String[][] TEST = {
        {WARNING_OUTPUT, "C.java:38: warning - Tag @see:illegal character: \"119040\" in \"C#method\ud834\udd00()"},
        {WARNING_OUTPUT, "C.java:44: warning - illegal character \ud801 in @serialField tag: field\ud801\ud801 int."},
        {WARNING_OUTPUT, "C.java:44: warning - illegal character \ud834\udd7b in @serialField tag: \ud834\udd7bfield int."},
    };
    private static final String[][] NEGATED_TEST = {
        {WARNING_OUTPUT, "C.java:14: warning - Tag @see:illegal character"},
        {WARNING_OUTPUT, "C.java:19: warning - Tag @see:illegal character"},
        {WARNING_OUTPUT, "C.java:24: warning - Tag @see:illegal character"},
        {WARNING_OUTPUT, "C.java:31: warning - illegal character"},
    };
    private static final String[] ARGS = new String[] {
        "-locale", "en_US", "-d", BUG_ID, SRC_DIR + FS + "C.java"
    };
    public static void main(String[] args) {
        Locale saveLocale = Locale.getDefault();
        try {
            TestSupplementary tester = new TestSupplementary();
            run(tester, ARGS, TEST, NEGATED_TEST);
            tester.printSummary();
        } finally {
            Locale.setDefault(saveLocale);
        }
    }
    public String getBugId() {
        return BUG_ID;
    }
    public String getBugName() {
        return getClass().getName();
    }
}
