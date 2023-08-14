public class TestNestedGenerics extends JavadocTester {
    private static final String BUG_ID = "6758050";
    private static final String[] ARGS = new String[]{
        "-d", BUG_ID, "-source", "1.5", "-sourcepath", SRC_DIR,
        "pkg"
    };
    private static final String[][] TEST = {
        {BUG_ID + FS + "pkg" + FS + "NestedGenerics.html",
            "<div class=\"block\">Contains <a " +
            "href=\"../pkg/NestedGenerics.html#foo(java.util.Map)\"><code>foo" +
            "(java.util.Map&lt;A, java.util.Map&lt;A, A&gt;&gt;)</code></a></div>"
        }
    };
    private static final String[][] NEGATED_TEST = NO_TEST;
    public static void main(String[] args) {
        TestNestedGenerics tester = new TestNestedGenerics();
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
