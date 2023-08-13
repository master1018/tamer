public class TestWarnings extends JavadocTester {
    private static final String BUG_ID = "4515705-4804296-4702454-4697036";
    private static final String[] ARGS = new String[] {
        "-d", BUG_ID, "-sourcepath", SRC_DIR, "pkg"
    };
    private static final String[] ARGS2 = new String[] {
        "-d", BUG_ID, "-private", "-sourcepath", SRC_DIR, "pkg"
    };
    private static final String[][] TEST = {
        {WARNING_OUTPUT,
            "X.java:11: warning - Missing closing '}' character for inline tag"},
        {ERROR_OUTPUT,
            "package.html: error - Body tag missing from HTML"},
    };
    private static final String[][] NEGATED_TEST = {
        {BUG_ID + FS + "pkg" + FS + "X.html", "can't find m()"},
        {BUG_ID + FS + "pkg" + FS + "X.html", "can't find X()"},
        {BUG_ID + FS + "pkg" + FS + "X.html", "can't find f"},
    };
    private static final String[][] TEST2 = {
        {BUG_ID + FS + "pkg" + FS + "X.html", "<a href=\"../pkg/X.html#m()\"><code>m()</code></a><br/>"},
        {BUG_ID + FS + "pkg" + FS + "X.html", "<a href=\"../pkg/X.html#X()\"><code>X()</code></a><br/>"},
        {BUG_ID + FS + "pkg" + FS + "X.html", "<a href=\"../pkg/X.html#f\"><code>f</code></a><br/>"},
    };
    private static final String[][] NEGATED_TEST2 = NO_TEST;
    public static void main(String[] args) {
        TestWarnings tester = new TestWarnings();
        run(tester, ARGS, TEST, NEGATED_TEST);
        run(tester, ARGS2, TEST2, NEGATED_TEST2);
        tester.printSummary();
    }
    public String getBugId() {
        return BUG_ID;
    }
    public String getBugName() {
        return getClass().getName();
    }
}
