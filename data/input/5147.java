public class TestThrowsTag extends JavadocTester {
    private static final String BUG_ID = "4985072";
    private static final String[] ARGS = new String[] {
        "-d", BUG_ID, "-sourcepath", SRC_DIR, "pkg"
    };
    private static final String[][] TEST = {
        {BUG_ID + FS + "pkg" + FS + "C.html",
            "<dd><code><a href=\"../pkg/T1.html\" title=\"class in pkg\">T1</a></code> - the first throws tag.</dd>" + NL +
            "<dd><code><a href=\"../pkg/T2.html\" title=\"class in pkg\">T2</a></code> - the second throws tag.</dd>" + NL +
            "<dd><code><a href=\"../pkg/T3.html\" title=\"class in pkg\">T3</a></code> - the third throws tag.</dd>" + NL +
            "<dd><code><a href=\"../pkg/T4.html\" title=\"class in pkg\">T4</a></code> - the fourth throws tag.</dd>" + NL +
            "<dd><code><a href=\"../pkg/T5.html\" title=\"class in pkg\">T5</a></code> - the first inherited throws tag.</dd>" + NL +
            "<dd><code><a href=\"../pkg/T6.html\" title=\"class in pkg\">T6</a></code> - the second inherited throws tag.</dd>" + NL +
            "<dd><code><a href=\"../pkg/T7.html\" title=\"class in pkg\">T7</a></code> - the third inherited throws tag.</dd>" + NL +
            "<dd><code><a href=\"../pkg/T8.html\" title=\"class in pkg\">T8</a></code> - the fourth inherited throws tag.</dd>"
        },
    };
    private static final String[][] NEGATED_TEST = NO_TEST;
    public static void main(String[] args) {
        TestThrowsTag tester = new TestThrowsTag();
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
