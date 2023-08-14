public class TestSerialVersionUID extends JavadocTester {
    private static final String BUG_ID = "4525039";
    private static final String OUTPUT_DIR = "docs-" + BUG_ID;
    private static final String[] ARGS = new String[] {
        "-d", OUTPUT_DIR,
        SRC_DIR + FS + "C.java"
    };
    private static final String[][] TEST = {
        {OUTPUT_DIR + FS + "serialized-form.html", "-111111111111111L"}
    };
    private static final String[][] NEGATED_TEST = NO_TEST;
    public static void main(String[] args) {
        TestSerialVersionUID tester = new TestSerialVersionUID();
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
