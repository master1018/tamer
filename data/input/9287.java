public class TestWarnBadParamNames extends JavadocTester {
    private static final String BUG_ID = "4693440";
    private static final String[][] TEST = {
        {WARNING_OUTPUT, "warning - @param argument \"int\" is not a parameter name."},
        {WARNING_OUTPUT, "warning - @param argument \"IDontExist\" is not a parameter name."},
        {WARNING_OUTPUT, "warning - Parameter \"arg\" is documented more than once."},
    };
    private static final String[][] NEGATED_TEST = NO_TEST;
    private static final String[] ARGS = new String[] {
        "-d", BUG_ID, SRC_DIR + FS + "C.java"
    };
    public static void main(String[] args) {
        TestWarnBadParamNames tester = new TestWarnBadParamNames();
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
