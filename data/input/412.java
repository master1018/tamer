public class TestTitleInHref extends JavadocTester {
    private static final String BUG_ID = "4714257";
    private static final String[][] TEST = {
        {BUG_ID + FS + "pkg" + FS + "Links.html", "<a href=\"../pkg/Class.html\" title=\"class in pkg\">"},
        {BUG_ID + FS + "pkg" + FS + "Links.html", "<a href=\"../pkg/Interface.html\" title=\"interface in pkg\">"},
        {BUG_ID + FS + "pkg" + FS + "Links.html", "<a href=\"http:
    };
    private static final String[][] NEGATED_TEST = NO_TEST;
    private static final String[] ARGS = new String[] {
        "-d", BUG_ID, "-sourcepath", SRC_DIR,
        "-linkoffline", "http:
        SRC_DIR, "pkg"
    };
    public static void main(String[] args) {
        TestTitleInHref tester = new TestTitleInHref();
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
