public class TestNavagation extends JavadocTester {
    private static final String BUG_ID = "4131628-4664607";
    private static final String[] ARGS = new String[] {
        "-d", BUG_ID, "-sourcepath", SRC_DIR, "pkg"
    };
    private static final String[][] TEST = {
        {BUG_ID + FS + "pkg" + FS + "A.html", "<li>Prev Class</li>"},
        {BUG_ID + FS + "pkg" + FS + "A.html",
            "<a href=\"../pkg/C.html\" title=\"class in pkg\"><span class=\"strong\">Next Class</span></a>"},
        {BUG_ID + FS + "pkg" + FS + "C.html",
            "<a href=\"../pkg/A.html\" title=\"annotation in pkg\"><span class=\"strong\">Prev Class</span></a>"},
        {BUG_ID + FS + "pkg" + FS + "C.html",
            "<a href=\"../pkg/E.html\" title=\"enum in pkg\"><span class=\"strong\">Next Class</span></a>"},
        {BUG_ID + FS + "pkg" + FS + "E.html",
            "<a href=\"../pkg/C.html\" title=\"class in pkg\"><span class=\"strong\">Prev Class</span></a>"},
        {BUG_ID + FS + "pkg" + FS + "E.html",
            "<a href=\"../pkg/I.html\" title=\"interface in pkg\"><span class=\"strong\">Next Class</span></a>"},
        {BUG_ID + FS + "pkg" + FS + "I.html",
            "<a href=\"../pkg/E.html\" title=\"enum in pkg\"><span class=\"strong\">Prev Class</span></a>"},
        {BUG_ID + FS + "pkg" + FS + "I.html", "<li>Next Class</li>"},
        {BUG_ID + FS + "pkg" + FS + "I.html",
            "<a href=\"#skip-navbar_top\" title=\"Skip navigation links\"></a><a name=\"navbar_top_firstrow\">" + NL +
            "<!--   -->" + NL + "</a>"}
    };
    private static final String[][] NEGATED_TEST = NO_TEST;
    public static void main(String[] args) {
        TestNavagation tester = new TestNavagation();
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
