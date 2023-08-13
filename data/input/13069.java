public class TestThrowsHead extends JavadocTester {
    private static final String BUG_ID = "4530727";
    private static final String[][] TEST = {
        {BUG_ID + FS + "C.html", "<dt><span class=\"strong\">Throws:</span>"}
    };
    private static final String[][] NEGATED_TEST = NO_TEST;
    private static final String[] ARGS = new String[] {
        "-d", BUG_ID, SRC_DIR + FS + "C.java"
    };
    public static void main(String[] args) {
        TestThrowsHead tester = new TestThrowsHead();
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
