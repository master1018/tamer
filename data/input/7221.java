public class TestExternalOverridenMethod extends JavadocTester {
    private static final String BUG_ID = "4857717";
    private static final String[][] TEST = {
        {BUG_ID + FS + "pkg" + FS + "XReader.html",
            "<dt><strong>Overrides:</strong></dt>" + NL +
            "<dd><code><a href=\"http:
            "title=\"class or interface in java.io\">read</a></code>&nbsp;in class&nbsp;<code>" +
            "<a href=\"http:
            "title=\"class or interface in java.io\">FilterReader</a></code></dd>"},
        {BUG_ID + FS + "pkg" + FS + "XReader.html",
            "<dt><strong>Specified by:</strong></dt>" + NL +
            "<dd><code><a href=\"http:
            "title=\"class or interface in java.io\">readInt</a></code>&nbsp;in interface&nbsp;<code>" +
            "<a href=\"http:
            "title=\"class or interface in java.io\">DataInput</a></code></dd>"}};
    private static final String[][] NEGATED_TEST = NO_TEST;
    private static final String[] ARGS =
        new String[] {
            "-d", BUG_ID, "-sourcepath", SRC_DIR,
            "-linkoffline", "http:
            "pkg"
        };
    public static void main(String[] args) {
        TestExternalOverridenMethod tester = new TestExternalOverridenMethod();
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
