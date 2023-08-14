public class TestBaseClass extends JavadocTester {
    private static final String BUG_ID = "4197513";
    private static final String[][] TEST = NO_TEST;
    private static final String[][] NEGATED_TEST = NO_TEST;
    private static final String[] ARGS =
        new String[] {
            "-sourcepath", SRC_DIR,
            "-docletpath", SRC_DIR, "-doclet", "BaseClass",
            SRC_DIR + FS + "Bar.java", "baz"};
    public static void main(String[] args) {
        TestBaseClass tester = new TestBaseClass();
        if (run(tester, ARGS, TEST, NEGATED_TEST) != 0) {
            throw new Error("Javadoc failed to execute.");
        }
    }
    public String getBugId() {
        return BUG_ID;
    }
    public String getBugName() {
        return getClass().getName();
    }
}
