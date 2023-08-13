public class TestConstructorIndent extends JavadocTester {
    private static final String BUG_ID = "4904037";
    private static final String[] ARGS = new String[] {
        "-d", BUG_ID, "-sourcepath", SRC_DIR, SRC_DIR + FS + "C.java"
    };
    private static final String[][] TEST = {
        {BUG_ID + FS + "C.html", "<div class=\"block\">" +
                 "This is just a simple constructor.</div>" + NL +
                 "<dl><dt><span class=\"strong\">Parameters:</span></dt><dd>" +
                 "<code>i</code> - a param.</dd></dl>"
        }
    };
    private static final String[][] NEGATED_TEST = NO_TEST;
    public static void main(String[] args) {
        TestConstructorIndent tester = new TestConstructorIndent();
        run(tester, ARGS, TEST, NEGATED_TEST);
        tester.printSummary();
    }
    public String getBugId() {
        return BUG_ID;
    }
    public String getBugName() {
        return getClass().getName();
    }
}
