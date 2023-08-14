public class TestAbsLinkPath extends JavadocTester {
    private static final String BUG_ID = "4640745";
    private static final String[][] TEST = {
        {"tmp" + FS + "pkg1" + FS + "C1.html", "C2.html"}};
    private static final String[][] NEGATED_TEST = NO_TEST;
    private static final String[] ARGS1 =
        new String[] {
            "-d", "tmp2", "-sourcepath", SRC_DIR, "pkg2"};
    private static final String[] ARGS2 =
        new String[] {
            "-d", "tmp", "-sourcepath", SRC_DIR,
            "-link", ".." + FS + "tmp2", "pkg1"};
    public static void main(String[] args) {
        TestAbsLinkPath tester = new TestAbsLinkPath();
        run(tester, ARGS1, NO_TEST, NO_TEST);
        run(tester, ARGS2,  TEST, NEGATED_TEST);
        tester.printSummary();
    }
    public String getBugId() {
        return BUG_ID;
    }
    public String getBugName() {
        return getClass().getName();
    }
}
