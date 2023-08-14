public class TestDocRootInlineTag extends JavadocTester {
    private static final String BUG_ID = "4369014-4851991";
    private static final String[][] TEST = {
        {BUG_ID + FS + "TestDocRootTag.html",
            "<a href=\"http:
            "title=\"class or interface in java.io\"><code>File</code></a>"},
        {BUG_ID + FS + "TestDocRootTag.html",
            "<a href=\"./glossary.html\">glossary</a>"},
        {BUG_ID + FS + "TestDocRootTag.html",
            "<a href=\"http:
            "title=\"class or interface in java.io\"><code>Second File Link</code></a>"},
        {BUG_ID + FS + "TestDocRootTag.html", "The value of @docRoot is \"./\""},
        {BUG_ID + FS + "index-all.html", "My package page is " +
            "<a href=\"./pkg/package-summary.html\">here</a>"}
    };
    private static final String[][] NEGATED_TEST = NO_TEST;
    private static final String[] ARGS =
        new String[] {
            "-bottom", "The value of @docRoot is \"{@docRoot}\"",
            "-d", BUG_ID, "-sourcepath", SRC_DIR,
            "-linkoffline", "http:
            SRC_DIR, SRC_DIR + FS + "TestDocRootTag.java", "pkg"
        };
    public static void main(String[] args) {
        TestDocRootInlineTag tester = new TestDocRootInlineTag();
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
