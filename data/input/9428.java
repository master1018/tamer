public class TestOverridenMethodDocCopy extends JavadocTester {
    private static final String BUG_ID = "4368820";
    private static final String[] ARGS = new String[] {
        "-d", BUG_ID, "-sourcepath", SRC_DIR, "pkg1", "pkg2"
    };
    private static final String[][] TEST = {
        {BUG_ID + FS + "pkg1" + FS + "SubClass.html",
            "<strong>Description copied from class:&nbsp;<code>" +
            "<a href=\"../pkg1/BaseClass.html#overridenMethodWithDocsToCopy()\">" +
            "BaseClass</a></code></strong>"
        }
    };
    private static final String[][] NEGATED_TEST = NO_TEST;
    public static void main(String[] args) {
        TestOverridenMethodDocCopy tester = new TestOverridenMethodDocCopy();
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
