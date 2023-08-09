public class TestHtmlComments extends JavadocTester {
    private static final String BUG_ID = "4904038";
    private static final String[] ARGS = new String[] {
        "-d", BUG_ID, "-sourcepath", SRC_DIR, SRC_DIR + FS + "C.java"
    };
    private static final String[][] TEST = NO_TEST;
    private static final String[][] NEGATED_TEST = {
        {BUG_ID + FS + "C.html",
            "<!-- ============ FIELD DETAIL =========== -->"}
    };
    public static void main(String[] args) {
        TestHtmlComments tester = new TestHtmlComments();
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
