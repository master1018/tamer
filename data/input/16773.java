public class TestBadSourceFile extends JavadocTester {
    private static final String BUG_ID = "4835749";
    private static final String[] ARGS = new String[] {
        "-d", BUG_ID, SRC_DIR + FS + "C2.java"
    };
    private static final String[][] TEST = NO_TEST;
    private static final String[][] NEGATED_TEST = NO_TEST;
    public static void main(String[] args) {
        TestBadSourceFile tester = new TestBadSourceFile();
        int exitCode = run(tester, ARGS, TEST, NEGATED_TEST);
        tester.checkExitCode(0, exitCode);
        tester.printSummary();
    }
    public String getBugId() {
        return BUG_ID;
    }
    public String getBugName() {
        return getClass().getName();
    }
}
