public class TestBreakIterator extends JavadocTester {
    private static final String BUG_ID = "4165985";
    private static final String[][] TEST = {
        {BUG_ID + FS + "pkg" + FS + "BreakIteratorTest.html",
            "The class is empty (i.e. it has no members)."}};
    private static final String[][] NEGATED_TEST = NO_TEST;
    private static final String[] ARGS =
        new String[] {
            "-d", BUG_ID, "-sourcepath", SRC_DIR,
            "-breakiterator", "pkg"};
    public static void main(String[] args) {
        TestBreakIterator tester = new TestBreakIterator();
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
