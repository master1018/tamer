public class TestSummaryHeading extends JavadocTester {
    private static final String BUG_ID = "4904036";
    private static final String[] ARGS = new String[] {
        "-d", BUG_ID, "-sourcepath", SRC_DIR, SRC_DIR + FS + "C.java"
    };
    private static final String[][] TEST = {
        {BUG_ID + FS + "C.html",  "<h3>Method Summary</h3>"}
    };
    private static final String[][] NEGATED_TEST = NO_TEST;
    public static void main(String[] args) {
        TestSummaryHeading tester = new TestSummaryHeading();
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
