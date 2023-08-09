public class TestSimpleTagExclude extends JavadocTester {
    private static final String BUG_ID = "4628181";
    private static final String[][] TEST = NO_TEST;
    private static final String[][] NEGATED_TEST = {
        {BUG_ID + FS + "DummyClass.html", "todo"}
    };
    private static final String[] ARGS = new String[] {
        "-d", BUG_ID, "-sourcepath", SRC_DIR, "-tag", "todo:X",
        SRC_DIR + FS + "DummyClass.java"
    };
    public static void main(String[] args) {
        TestSimpleTagExclude tester = new TestSimpleTagExclude();
        if (run(tester, ARGS, TEST, NEGATED_TEST) != 0) {
            throw new Error("Javadoc failed to execute.");
        }
        tester.printSummary();
    }
    public String getBugId() {
        return BUG_ID;
    }
    public String getBugName() {
        return getClass().getName();
    }
}
