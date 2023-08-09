public class TestSuperClassInSerialForm extends JavadocTester {
    private static final String BUG_ID = "4671694";
    private static final String[][] TEST = {
        {BUG_ID + FS + "serialized-form.html",
         "<a href=\"pkg/SubClass.html\" title=\"class in pkg\">pkg.SubClass</a> extends <a href=\"pkg/SuperClass.html\" title=\"class in pkg\">SuperClass</a>"}
    };
    private static final String[][] NEGATED_TEST = NO_TEST;
    private static final String[] ARGS = new String[] {
        "-d", BUG_ID, "-sourcepath", SRC_DIR, "pkg"
    };
    public static void main(String[] args) {
        TestSuperClassInSerialForm tester = new TestSuperClassInSerialForm();
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
