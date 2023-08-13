public class PackagesHeader extends JavadocTester {
    private static final String BUG_ID = "4766385";
    private static final String OUTPUT_DIR = "docs-" + BUG_ID;
    private static final String OUTPUT_DIR1 = "docs1-" + BUG_ID + FS;
    private static final String OUTPUT_DIR2 = "docs2-" + BUG_ID + FS;
    private static final String OUTPUT_DIR3 = "docs3-" + BUG_ID + FS;
    private static final String[][] TESTARRAY1 = {
        { OUTPUT_DIR1 + "overview-frame.html",
                 "Main Frame Header" }
    };
    private static final String[][] TESTARRAY2 = {
        {  OUTPUT_DIR2 + "overview-frame.html",
                 "Packages Frame Header" }
    };
    private static final String[][] TESTARRAY3 = {
        { OUTPUT_DIR3 + "overview-frame.html",
                 "Packages Frame Header" },
        { OUTPUT_DIR3 + "overview-summary.html",
                 "Main Frame Header" }
    };
    private static final String[] JAVADOC_ARGS1 = new String[] {
            "-d", OUTPUT_DIR1,
            "-header", "Main Frame Header",
            "-sourcepath", SRC_DIR,
            "p1", "p2"};
    private static final String[] JAVADOC_ARGS2 = new String[] {
            "-d", OUTPUT_DIR2,
            "-packagesheader", "Packages Frame Header",
            "-sourcepath", SRC_DIR,
            "p1", "p2"};
    private static final String[] JAVADOC_ARGS3 = new String[] {
            "-d", OUTPUT_DIR3,
            "-packagesheader", "Packages Frame Header",
            "-header", "Main Frame Header",
            "-sourcepath", SRC_DIR,
            "p1", "p2"};
    private static final String[][] NEGATED_TEST = NO_TEST;
    public static void main(String[] args) {
        JavadocTester tester = new PackagesHeader();
        run(tester, JAVADOC_ARGS1, TESTARRAY1, NEGATED_TEST);
        run(tester, JAVADOC_ARGS2, TESTARRAY2, NEGATED_TEST);
        run(tester, JAVADOC_ARGS3, TESTARRAY3, NEGATED_TEST);
        tester.printSummary();
    }
    public String getBugId() {
        return BUG_ID;
    }
    public String getBugName() {
        return getClass().getName();
    }
}
