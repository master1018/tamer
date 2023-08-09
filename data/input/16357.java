public class AccessSummary extends JavadocTester {
    private static final String BUG_ID = "4637604-4775148";
    private static final String OUTPUT_DIR1 = "docs1-" + BUG_ID + FS;
    private static final String[][] TESTARRAY1 = {
        { OUTPUT_DIR1 + "overview-summary.html",
                 "summary=\"Packages table, listing packages, and an explanation\"" },
        { OUTPUT_DIR1 + "p1" + FS + "C1.html",
                 "summary=\"Constructor Summary table, listing constructors, and an explanation\"" },
        { OUTPUT_DIR1 + "constant-values.html",
                 "summary=\"Constant Field Values table, listing constant fields, and values\"" }
    };
    private static final String[] JAVADOC_ARGS = new String[] {
            "-d", OUTPUT_DIR1,
            "-sourcepath", SRC_DIR,
            "p1", "p2"};
    public static void main(String[] args) {
        JavadocTester tester = new AccessSummary();
        run(tester, JAVADOC_ARGS,  TESTARRAY1, new String[][] {});
        tester.printSummary();       
    }
    public String getBugId() {
        return BUG_ID;
    }
    public String getBugName() {
        return getClass().getName();
    }
}
