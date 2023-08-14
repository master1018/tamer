public class TestNotifications extends JavadocTester {
    private static final String BUG_ID = "4657239";
    private static final String[] ARGS = new String[] {
        "-d", BUG_ID, "-sourcepath", SRC_DIR, "pkg"
    };
    private static final String[] ARGS2 = new String[] {
        "-help"
    };
    private static final String[][] TEST = {
        {NOTICE_OUTPUT, "Creating destination directory: \"4657239"}
    };
    private static final String[][] NEGATED_TEST = {
        {NOTICE_OUTPUT, "Creating destination directory: \"4657239"}
    };
    private static final String[][] NEGATED_TEST2 = {
        {NOTICE_OUTPUT, "[classnames]"}
    };
    public static void main(String[] args) {
        TestNotifications tester = new TestNotifications();
        run(tester, ARGS, TEST, NO_TEST);
        run(tester, ARGS, NO_TEST, NEGATED_TEST);
        run(tester, ARGS2, NO_TEST, NEGATED_TEST2);
        tester.printSummary();
    }
    public String getBugId() {
        return BUG_ID;
    }
    public String getBugName() {
        return getClass().getName();
    }
}
