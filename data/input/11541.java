public class TestTopOption extends JavadocTester {
    private static final String BUG_ID = "6227616";
    private static final String[] ARGS = new String[] {
        "-overview", "SRC_DIR + FS + overview.html", "-use", "-top", "TOP TEXT", "-d", BUG_ID, "-sourcepath",
        SRC_DIR, "pkg"
    };
    private static final String[][] TEST = {
        {BUG_ID + FS + "pkg" + FS + "AnnotationType.html",
            "TOP TEXT"},
        {BUG_ID + FS + "pkg" + FS + "class-use" + FS + "AnnotationType.html",
            "TOP TEXT"},
        {BUG_ID + FS + "pkg" + FS + "Cl.html",
            "TOP TEXT"},
        {BUG_ID + FS + "pkg" + FS + "class-use" + FS + "Cl.html",
            "TOP TEXT"},
        {BUG_ID + FS + "pkg" + FS + "package-summary.html",
            "TOP TEXT"},
        {BUG_ID + FS + "pkg" + FS + "package-use.html",
           "TOP TEXT"},
        {BUG_ID + FS + "overview-summary.html",
            "TOP TEXT"},
        {BUG_ID + FS + "overview-tree.html",
            "TOP TEXT"},
        {BUG_ID + FS + "constant-values.html",
            "TOP TEXT"},
        {BUG_ID + FS + "help-doc.html",
            "TOP TEXT"},
    };
    private static final String[][] NEGATED_TEST = NO_TEST;
    public static void main(String[] args) {
        TestTopOption tester = new TestTopOption();
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
