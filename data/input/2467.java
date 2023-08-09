public class TestGroupOption extends JavadocTester {
    private static final String BUG_ID = "4924383";
    private static final String[] ARGS1 = new String[] {
        "-d", BUG_ID, "-sourcepath", SRC_DIR,
        "-group", "Package One", "pkg1",
        "-group", "Package Two", "pkg2",
        "-group", "Package Three", "pkg3",
        "pkg1", "pkg2", "pkg3"
    };
    private static final String[] ARGS2 = new String[] {
        "-d", BUG_ID, "-sourcepath", SRC_DIR,
        "-group", "Package One", "pkg1",
        "-group", "Package One", "pkg2",
        "-group", "Package One", "pkg3",
        "pkg1", "pkg2", "pkg3"
    };
    private static final String[][] TEST1 = NO_TEST;
    private static final String[][] NEGATED_TEST1 = {{WARNING_OUTPUT, "-group"}};
    private static final String[][] TEST2 = {{WARNING_OUTPUT, "-group"}};
    private static final String[][] NEGATED_TEST2 = NO_TEST;
    public static void main(String[] args) {
        TestGroupOption tester = new TestGroupOption();
        run(tester, ARGS1, TEST1, NEGATED_TEST1);
        tester.printSummary();
        tester = new TestGroupOption();
        run(tester, ARGS2, TEST2, NEGATED_TEST2);
        tester.printSummary();
    }
    public String getBugId() {
        return BUG_ID;
    }
    public String getBugName() {
        return getClass().getName();
    }
}
