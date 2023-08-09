public class TestNewLineInLink extends JavadocTester {
    private static final String BUG_ID = "4739870";
    private static final String[][] NEGATED_TEST =
        new String[][] {
            {ERROR_OUTPUT,
                "illegal character"}
        };
    private static final String[] ARGS = new String[] {
            "-d", BUG_ID, "-sourcepath", SRC_DIR,
                "-linkoffline", "http:
                SRC_DIR, "testNewLineInLink"};
    public static void main(String[] args) {
        TestNewLineInLink tester = new TestNewLineInLink();
        run(tester, ARGS, new String[][] {}, NEGATED_TEST);
        tester.printSummary();
    }
    public String getBugId() {
        return BUG_ID;
    }
    public String getBugName() {
        return getClass().getName();
    }
}
