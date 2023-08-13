public class TestHrefInDocComment extends JavadocTester {
    private static final String BUG_ID = "4638015";
    private static final String[][] TEST = NO_TEST;
    private static final String[][] NEGATED_TEST = NO_TEST;
    private static final String[] ARGS =
        new String[] {
            "-d", BUG_ID, "-sourcepath", SRC_DIR, "pkg"};
    public static void main(String[] args) {
        TestHrefInDocComment tester = new TestHrefInDocComment();
        if (run(tester, ARGS, TEST, NEGATED_TEST) != 0) {
            throw new Error("Javadoc failed to execute properly with given source.");
        }
    }
    public String getBugId() {
        return BUG_ID;
    }
    public String getBugName() {
        return getClass().getName();
    }
}
