public class TestParamTaglet extends JavadocTester {
    private static final String BUG_ID = "4802275-4967243";
    private static final String[] ARGS = new String[] {
        "-d", BUG_ID, "-sourcepath", SRC_DIR, "pkg"
    };
    private static final String[][] TEST = {
        {BUG_ID + FS + "pkg" + FS + "C.html",
            "<span class=\"strong\">Parameters:</span></dt><dd><code>param1</code> - testing 1 2 3.</dd>" +
                "<dd><code>param2</code> - testing 1 2 3."
        },
        {BUG_ID + FS + "pkg" + FS + "C.html",
            "<span class=\"strong\">Parameters:</span></dt><dd><code><I>p1</I></code> - testing 1 2 3.</dd>" +
                "<dd><code><I>p2</I></code> - testing 1 2 3."
        },
        {BUG_ID + FS + "pkg" + FS + "C.html",
            "<code><I>inheritBug</I></code> -"
        },
    };
    private static final String[][] NEGATED_TEST = NO_TEST;
    public static void main(String[] args) {
        TestParamTaglet tester = new TestParamTaglet();
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
