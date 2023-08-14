public class TestStylesheet extends JavadocTester {
    private static final String BUG_ID = "4494033-7028815-7052425";
    private static final String[] ARGS = new String[] {
        "-d", BUG_ID, "-sourcepath", SRC_DIR, "pkg"
    };
    private static final String[][] TEST = {
        {BUG_ID + FS + "stylesheet.css",
                ""},
        {BUG_ID + FS + "stylesheet.css",
                ""},
        {BUG_ID + FS + "stylesheet.css",
                ""},
        {BUG_ID + FS + "stylesheet.css",
                ""},
        {BUG_ID + FS + "stylesheet.css",
                "body {" + NL + "    background-color:#ffffff;" + NL +
                "    color:#353833;" + NL +
                "    font-family:Arial, Helvetica, sans-serif;" + NL +
                "    font-size:76%;" + NL + "    margin:0;" + NL + "}"},
        {BUG_ID + FS + "stylesheet.css",
                "ul {" + NL + "    list-style-type:disc;" + NL + "}"},
        {BUG_ID + FS + "pkg" + FS + "A.html",
                "<link rel=\"stylesheet\" type=\"text/css\" " +
                "href=\"../stylesheet.css\" title=\"Style\">"}
    };
    private static final String[][] NEGATED_TEST = {
        {BUG_ID + FS + "stylesheet.css",
                "* {" + NL + "    margin:0;" + NL + "    padding:0;" + NL + "}"}
    };
    public static void main(String[] args) {
        TestStylesheet tester = new TestStylesheet();
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
