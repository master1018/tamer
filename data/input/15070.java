public class Template extends JavadocTester {
    private static final String BUG_ID = "<BUG ID>";
    private static final String OUTPUT_DIR = "docs-" + BUG_ID;
    private static final String[] ARGS = new String[] {
        "-d", OUTPUT_DIR, "-sourcepath", SRC_DIR
    };
    private static final String[][] TEST = NO_TEST;
    private static final String[][] NEGATED_TEST = NO_TEST;
    public static void main(String[] args) {
        Template tester = new Template();
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
