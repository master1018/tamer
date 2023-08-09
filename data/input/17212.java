public class TestInlineLinkLabel extends JavadocTester {
    private static final String BUG_ID = "4524136";
    private static final String[][] TEST = {
        {BUG_ID + FS + "pkg" + FS + "C1.html" , "<a href=\"../pkg/package-summary.html\"><code>Here is a link to a package</code></a>"},
        {BUG_ID + FS + "pkg" + FS + "C1.html" , "<a href=\"../pkg/C2.html\" title=\"class in pkg\"><code>Here is a link to a class</code></a>"}
    };
    private static final String[][] NEGATED_TEST = NO_TEST;
    private static final String[] ARGS =
        new String[] {
            "-d", BUG_ID, "-sourcepath", SRC_DIR, "pkg"};
    public static void main(String[] args) {
        TestInlineLinkLabel tester = new TestInlineLinkLabel();
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
