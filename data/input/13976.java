public class TestHiddenMembers extends JavadocTester {
    private static final String BUG_ID = "4492178";
    private static final String[][] TEST = NO_TEST;
    private static final String[][] NEGATED_TEST = {
        {BUG_ID + FS + "pkg" + FS + "SubClass.html",
            "inherited from class pkg.<A HREF=\"../pkg/BaseClass.html\">BaseClass</A>"}
        };
    private static final String[] ARGS =
        new String[] {
            "-d", BUG_ID, "-sourcepath", SRC_DIR,
            "pkg"
        };
    public static void main(String[] args) {
        TestHiddenMembers tester = new TestHiddenMembers();
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
