public class TestModifier extends JavadocTester {
    private static final String BUG_ID = "4210388";
    private static final String[][] TEST = NO_TEST;
    private static final String[][] NEGATED_TEST = NO_TEST;
    private static final String[] ARGS =
        new String[] {
            "-sourcepath", SRC_DIR,
            "-docletpath", SRC_DIR, "-doclet", "ModifierAbstract",
            SRC_DIR + FS + "Interface.java", SRC_DIR + FS + "Test.java"};
    public static void main(String[] args) {
        TestModifier tester = new TestModifier();
        if (run(tester, ARGS, TEST, NEGATED_TEST) != 0) {
            throw new Error("Javadoc error occured during execution.");
        }
    }
    public String getBugId() {
        return BUG_ID;
    }
    public String getBugName() {
        return getClass().getName();
    }
}
