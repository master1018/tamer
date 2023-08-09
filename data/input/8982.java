public class TestLinkToSerialForm extends JavadocTester {
    private static final String BUG_ID = "4521661";
    private static final String[][] TEST = {
        {BUG_ID + FS + "serialized-form.html", "<a name=\"pkg.C\">"},
        {BUG_ID + FS + "pkg" + FS + "C.html", "<a href=\"../serialized-form.html#pkg.C\">"}
    };
    private static final String[][] NEGATED_TEST = NO_TEST;
    private static final String[] ARGS =
        new String[] {
            "-d", BUG_ID, "-sourcepath", SRC_DIR, "pkg"};
    public static void main(String[] args) {
        TestLinkToSerialForm tester = new TestLinkToSerialForm();
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
