public class TestHtmlStrongTag extends JavadocTester {
    private static final String BUG_ID = "6786028";
    private static final String[][] TEST1 = {
        {BUG_ID + FS + "pkg1" + FS + "C1.html", "<span class=\"strong\">See Also:</span>"}};
    private static final String[][] NEGATED_TEST1 = {
        {BUG_ID + FS + "pkg1" + FS + "C1.html", "<STRONG>Method Summary</STRONG>"},
        {BUG_ID + FS + "pkg1" + FS + "C1.html", "<B>"},
        {BUG_ID + FS + "pkg1" + FS + "package-summary.html", "<STRONG>Class Summary</STRONG>"}};
    private static final String[][] TEST2 = {
        {BUG_ID + FS + "pkg2" + FS + "C2.html", "<B>Comments:</B>"}};
    private static final String[][] NEGATED_TEST2 = {
        {BUG_ID + FS + "pkg2" + FS + "C2.html", "<STRONG>Method Summary</STRONG>"},
        {BUG_ID + FS + "pkg1" + FS + "package-summary.html", "<STRONG>Class Summary</STRONG>"}};
    private static final String[] ARGS1 =
        new String[] {
            "-d", BUG_ID, "-sourcepath", SRC_DIR, "pkg1"};
    private static final String[] ARGS2 =
        new String[] {
            "-d", BUG_ID, "-sourcepath", SRC_DIR, "pkg2"};
    public static void main(String[] args) {
        TestHtmlStrongTag tester = new TestHtmlStrongTag();
        run(tester, ARGS1, TEST1, NEGATED_TEST1);
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
