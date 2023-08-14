public class TemplateComplete extends JavadocTester {
    private static final String BUG_ID = "<BUG ID>";
    private static final String OUTPUT_DIR = "docs-" + BUG_ID;
    private static final String[] ARGS = new String[] {
        "-d", OUTPUT_DIR, "-sourcepath", SRC_DIR
    };
    private static final String[][] TEST = NO_TEST;
    private static final String[][] NEGATED_TEST = NO_TEST;
    private static final int EXPECTED_EXIT_CODE = 0;
    private static final String[][] FILES_TO_DIFF = {};
    public static void main(String[] args) {
        TemplateComplete tester = new TemplateComplete();
        int actualExitCode = run(tester, ARGS, TEST, NEGATED_TEST);
        tester.checkExitCode(EXPECTED_EXIT_CODE, actualExitCode);
        tester.runDiffs(FILES_TO_DIFF, false);
        tester.printSummary();
    }
    public String getBugId() {
        return BUG_ID;
    }
    public String getBugName() {
        return getClass().getName();
    }
}
