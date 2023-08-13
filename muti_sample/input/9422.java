public class TestEnclosingClass extends JavadocTester {
    private static final String BUG_ID = "5008230";
    private static final String[] ARGS = new String[] {
        "-d", BUG_ID, "-sourcepath", SRC_DIR, "pkg"
    };
    private static final String[][] TEST = {
        {BUG_ID + FS + "pkg" + FS + "MyClass.MyInterface.html", "Enclosing class:"}
    };
    private static final String[][] NEGATED_TEST = NO_TEST;
    public static void main(String[] args) {
        TestEnclosingClass tester = new TestEnclosingClass();
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
