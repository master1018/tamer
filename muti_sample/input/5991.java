public class TestConstantValuesPage extends JavadocTester {
    private static final String BUG_ID = "4681599";
    private static final String[][] TEST = NO_TEST;
    private static final String[][] NEGATED_TEST = {
        {NOTICE_OUTPUT, "constant-values.html..."}
        };
    private static final String[] ARGS =
        new String[] {
            "-d", BUG_ID, "-sourcepath", SRC_DIR, "foo"};
    public static void main(String[] args) {
        TestConstantValuesPage tester = new TestConstantValuesPage();
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
