public class TestHtmlTag extends JavadocTester {
    private static final String BUG_ID = "6786682";
    private static final String[][] TEST1 = {
        {BUG_ID + FS + "pkg1" + FS + "C1.html", "<html lang=\"" + Locale.getDefault().getLanguage() + "\">"},
        {BUG_ID + FS + "pkg1" + FS + "package-summary.html", "<html lang=\"" + Locale.getDefault().getLanguage() + "\">"}};
    private static final String[][] NEGATED_TEST1 = {
        {BUG_ID + FS + "pkg1" + FS + "C1.html", "<html>"}};
    private static final String[][] TEST2 = {
        {BUG_ID + FS + "pkg2" + FS + "C2.html", "<html lang=\"ja\">"},
        {BUG_ID + FS + "pkg2" + FS + "package-summary.html", "<html lang=\"ja\">"}};
    private static final String[][] NEGATED_TEST2 = {
        {BUG_ID + FS + "pkg2" + FS + "C2.html", "<html>"}};
    private static final String[][] TEST3 = {
        {BUG_ID + FS + "pkg1" + FS + "C1.html", "<html lang=\"en\">"},
        {BUG_ID + FS + "pkg1" + FS + "package-summary.html", "<html lang=\"en\">"}};
    private static final String[][] NEGATED_TEST3 = {
        {BUG_ID + FS + "pkg1" + FS + "C1.html", "<html>"}};
    private static final String[] ARGS1 =
        new String[] {
            "-d", BUG_ID, "-sourcepath", SRC_DIR, "pkg1"};
    private static final String[] ARGS2 =
        new String[] {
            "-locale", "ja", "-d", BUG_ID, "-sourcepath", SRC_DIR, "pkg2"};
    private static final String[] ARGS3 =
        new String[] {
            "-locale", "en_US", "-d", BUG_ID, "-sourcepath", SRC_DIR, "pkg1"};
    public static void main(String[] args) {
        TestHtmlTag tester = new TestHtmlTag();
        run(tester, ARGS1, TEST1, NEGATED_TEST1);
        run(tester, ARGS2, TEST2, NEGATED_TEST2);
        run(tester, ARGS3, TEST3, NEGATED_TEST3);
        tester.printSummary();
    }
    public String getBugId() {
        return BUG_ID;
    }
    public String getBugName() {
        return getClass().getName();
    }
}
