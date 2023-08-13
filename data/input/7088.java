public class TestClassCrossReferences extends JavadocTester {
    private static final String BUG_ID = "4652655-4857717";
    private static final String[][] TEST = {
        {BUG_ID + FS + "C.html",
            "<a href=\"http:
        {BUG_ID + FS + "C.html",
            "<a href=\"http:
            "title=\"class or interface in javax.swing.text\"><code>Link to AttributeContext innerclass</code></a>"},
        {BUG_ID + FS + "C.html",
            "<a href=\"http:
                "title=\"class or interface in java.math\"><code>Link to external class BigDecimal</code></a>"},
        {BUG_ID + FS + "C.html",
            "<a href=\"http:
                "title=\"class or interface in java.math\"><code>Link to external member gcd</code></a>"},
        {BUG_ID + FS + "C.html",
            "<dl>" + NL + "<dt><strong>Overrides:</strong></dt>" + NL +
            "<dd><code>toString</code>&nbsp;in class&nbsp;<code>java.lang.Object</code></dd>" + NL +
            "</dl>"}
    };
    private static final String[][] NEGATED_TEST = NO_TEST;
    private static final String[] ARGS =
        new String[] {
            "-d", BUG_ID, "-sourcepath", SRC_DIR,
            "-linkoffline", "http:
            SRC_DIR, SRC_DIR + FS + "C.java"};
    public static void main(String[] args) {
        TestClassCrossReferences tester = new TestClassCrossReferences();
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
