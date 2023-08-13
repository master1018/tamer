public class TestBadLinkOption extends JavadocTester {
    private static final String BUG_ID = "4720957";
    private static final String[] ARGS = new String[] {
        "-d", BUG_ID, "-sourcepath", SRC_DIR,
        "-link", BUG_ID, "pkg"
    };
    private static final String[][] TEST = {
        {WARNING_OUTPUT, "Error reading file:"}
    };
    private static final String[][] NEG_TEST = {
        {ERROR_OUTPUT, "Error reading file:"}
    };
    public static void main(String[] args) {
        TestBadLinkOption tester = new TestBadLinkOption();
        run(tester, ARGS, TEST, NEG_TEST);
        tester.printSummary();
    }
    public String getBugId() {
        return BUG_ID;
    }
    public String getBugName() {
        return getClass().getName();
    }
}
