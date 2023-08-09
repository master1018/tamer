public class TestIndex extends JavadocTester {
    private static final String BUG_ID = "4852280-4517115-4973608-4994589";
    private static final String[] ARGS = new String[] {
        "-d", BUG_ID, "-sourcepath", SRC_DIR, "pkg", SRC_DIR + FS + "NoPackage.java"
    };
    private static final String[][] TEST = {
        {BUG_ID + FS + "index.html",
            "<frame src=\"overview-summary.html\" name=\"classFrame\" title=\"" +
            "Package, class and interface descriptions\" scrolling=\"yes\">"},
        {BUG_ID + FS + "index-all.html",
            "<a href=\"./pkg/C.html\" title=\"class in pkg\"><span class=\"strong\">C</span></a>" +
            " - Class in <a href=\"./pkg/package-summary.html\">pkg</a>"},
        {BUG_ID + FS + "index-all.html",
            "<a href=\"./pkg/Interface.html\" title=\"interface in pkg\">" +
            "<span class=\"strong\">Interface</span></a> - Interface in " +
            "<a href=\"./pkg/package-summary.html\">pkg</a>"},
        {BUG_ID + FS + "index-all.html",
            "<a href=\"./pkg/AnnotationType.html\" title=\"annotation in pkg\">" +
            "<span class=\"strong\">AnnotationType</span></a> - Annotation Type in " +
            "<a href=\"./pkg/package-summary.html\">pkg</a>"},
        {BUG_ID + FS + "index-all.html",
            "<a href=\"./pkg/Coin.html\" title=\"enum in pkg\">" +
            "<span class=\"strong\">Coin</span></a> - Enum in " +
            "<a href=\"./pkg/package-summary.html\">pkg</a>"},
        {BUG_ID + FS + "index-all.html",
            "Class in <a href=\"./package-summary.html\">&lt;Unnamed&gt;</a>"},
        {BUG_ID + FS + "index-all.html",
            "<dl>" + NL + "<dt><span class=\"strong\"><a href=\"./pkg/C.html#Java\">" +
            "Java</a></span> - Static variable in class pkg.<a href=\"./pkg/C.html\" " +
            "title=\"class in pkg\">C</a></dt>" + NL + "<dd>&nbsp;</dd>" + NL +
            "<dt><span class=\"strong\"><a href=\"./pkg/C.html#JDK\">JDK</a></span> " +
            "- Static variable in class pkg.<a href=\"./pkg/C.html\" title=\"class in pkg\">" +
            "C</a></dt>" + NL + "<dd>&nbsp;</dd>" + NL + "</dl>"},
    };
    private static final String[][] NEGATED_TEST = NO_TEST;
    public static void main(String[] args) {
        TestIndex tester = new TestIndex();
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
