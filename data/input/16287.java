public class TestTagMisuse extends JavadocTester {
    private static final String BUG_ID = "no-bug-id";
    private static final String[][] TEST = {
        {WARNING_OUTPUT, "warning - Tag @param cannot be used in field documentation."},
        {WARNING_OUTPUT, "warning - Tag @throws cannot be used in field documentation."},
        {WARNING_OUTPUT, "warning - Tag @return cannot be used in constructor documentation."},
        {WARNING_OUTPUT, "warning - Tag @throws cannot be used in inline documentation."},
    };
    private static final String[][] NEGATED_TEST = NO_TEST;
    private static final String[] ARGS = new String[] {
        "-d", BUG_ID, SRC_DIR + FS + "TestTagMisuse.java"
    };
    public static void main(String[] args) {
        TestTagMisuse tester = new TestTagMisuse();
        run(tester, ARGS, TEST, NEGATED_TEST);
        tester.printSummary();
    }
    public String getBugId() {
        return BUG_ID;
    }
    public String getBugName() {
        return getClass().getName();
    }
    public int field;
    public TestTagMisuse(){}
}
