public class TestDocErrorReporter extends JavadocTester {
    private static final String BUG_ID = "4927928";
    private static final String[] ARGS = new String[] {
        "-d", BUG_ID, "-sourcepath", SRC_DIR, "-encoding", "xyz",
            SRC_DIR + FS + "TestDocErrorReporter.java"
    };
    private static final String[][] TEST = NO_TEST;
    private static final String[][] NEGATED_TEST = NO_TEST;
    private static final int EXPECTED_EXIT_CODE = 1;
    public static void main(String[] args) {
        TestDocErrorReporter tester = new TestDocErrorReporter();
        int actualExitCode = run(tester, ARGS, TEST, NEGATED_TEST);
        tester.checkExitCode(EXPECTED_EXIT_CODE, actualExitCode);
        tester.printSummary();
    }
    public String getBugId() {
        return BUG_ID;
    }
    public String getBugName() {
        return getClass().getName();
    }
}
