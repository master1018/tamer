public class TestUnnamedPackage extends JavadocTester {
    private static final String BUG_ID = "4904075-4774450-5015144";
    private static final String[] ARGS = new String[] {
        "-d", BUG_ID, "-sourcepath", SRC_DIR, SRC_DIR + FS + "C.java"
    };
    private static final String[][] TEST = {
        {BUG_ID + FS + "package-summary.html",
            "<h1 title=\"Package\" class=\"title\">Package&nbsp;&lt;Unnamed&gt;</h1>"
        },
        {BUG_ID + FS + "package-summary.html",
            "This is a package comment for the unnamed package."
        },
        {BUG_ID + FS + "package-summary.html",
            "This is a class in the unnamed package."
        },
        {BUG_ID + FS + "package-tree.html",
            "<h1 class=\"title\">Hierarchy For Package &lt;Unnamed&gt;</h1>"
        },
        {BUG_ID + FS + "index-all.html",
            "title=\"class in &lt;Unnamed&gt;\""
        },
        {BUG_ID + FS + "C.html", "<a href=\"package-summary.html\">"}
    };
    private static final String[][] NEGATED_TEST = {
        {ERROR_OUTPUT, "BadSource"},
    };
    public static void main(String[] args) {
        TestUnnamedPackage tester = new TestUnnamedPackage();
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
