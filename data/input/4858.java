public class TestReturnTag extends JavadocTester {
    private static final String BUG_ID = "4490068";
    private static final String[] ARGS = new String[] {
        "-d", BUG_ID, "-sourcepath", SRC_DIR, SRC_DIR + FS + "TestReturnTag.java"
    };
    private static final String[][] TEST = {
        {WARNING_OUTPUT,
            "warning - @return tag cannot be used in method with void return type."}
    };
    private static final String[][] NEGATED_TEST = NO_TEST;
    public void method() {}
    public static void main(String[] args) {
        TestReturnTag tester = new TestReturnTag();
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
