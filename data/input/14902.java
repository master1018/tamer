public class TestThrowsTagInheritence extends JavadocTester {
    private static final String BUG_ID = "4684827-4633969";
    private static final String[][] TEST = {
        {BUG_ID + FS + "Foo.html", "Test 1 passes."}
    };
    private static final String[][] NEGATED_TEST = {
        {BUG_ID + FS + "C.html", "Test 1 fails."}
    };
    private static final String[] ARGS = new String[] {
        "-d", BUG_ID, "-sourcepath", SRC_DIR, SRC_DIR + FS + "C.java",
        SRC_DIR + FS + "I.java", SRC_DIR + FS + "Foo.java",
        SRC_DIR + FS + "Iface.java"
    };
    public static void main(String[] args) {
        TestThrowsTagInheritence tester = new TestThrowsTagInheritence();
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
