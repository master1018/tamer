public class TestEmptyClass extends JavadocTester {
    private static final String OUTPUT_DIR = "tmp";
    private static final String[][] TEST = NO_TEST;
    private static final String[][] NEGATED_TEST = {
        {OUTPUT_DIR + FS + "overview-tree.html", "<A HREF=\"TestEmptyClass.html\">"},
        {OUTPUT_DIR + FS + "index-all.html", "<A HREF=\"TestEmptyClass.html\">"},
    };
    private static final String[] ARGS =
        new String[] {
            "-classpath", SRC_DIR + FS + "src",
            "-d", OUTPUT_DIR, "-sourcepath", SRC_DIR + FS + "src",
            SRC_DIR + FS + "src" + FS + "Empty.java"
        };
    private static final String BUG_ID = "4483401 4483407 4483409 4483413 4494343";
    public static void main(String[] args) {
        TestEmptyClass tester = new TestEmptyClass();
        int exitCode = run(tester, ARGS, TEST, NEGATED_TEST);
        tester.printSummary();
        if (exitCode != 0) {
            throw new Error("Error found while executing Javadoc");
        }
    }
    public String getBugId() {
        return BUG_ID;
    }
    public String getBugName() {
        return getClass().getName();
    }
}
