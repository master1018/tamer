public class TestEncoding extends JavadocTester {
    private static final String BUG_ID = "4661481";
    private static final String[][] TEST = NO_TEST;
    private static final String[][] NEGATED_TEST = {
        {BUG_ID + FS + "EncodeTest.html", "???"}
    };
    private static final String[] ARGS =
        new String[] {
            "-d", BUG_ID, "-sourcepath", SRC_DIR,
            "-encoding", "SJIS", SRC_DIR + FS + "EncodeTest.java"
        };
    public static void main(String[] args) {
        TestEncoding tester = new TestEncoding();
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
