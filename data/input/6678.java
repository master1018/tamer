public class TestJavascript extends JavadocTester {
    private static final String BUG_ID = "4665566-4855876";
    private static final String[] ARGS = new String[] {
        "-d", BUG_ID, "-sourcepath", SRC_DIR, "pkg", SRC_DIR + FS + "TestJavascript.java"
    };
    private static final String[][] TEST = {
        {BUG_ID + FS + "pkg" + FS + "C.html",
            "<a href=\"../index.html?pkg/C.html\" target=\"_top\">Frames</a>"},
        {BUG_ID + FS + "TestJavascript.html",
            "<a href=\"index.html?TestJavascript.html\" target=\"_top\">Frames</a>"},
        {BUG_ID + FS + "index.html",
            "<script type=\"text/javascript\">" + NL +
                        "    targetPage = \"\" + window.location.search;" + NL +
            "    if (targetPage != \"\" && targetPage != \"undefined\")" + NL +
            "        targetPage = targetPage.substring(1);" + NL +
            "    if (targetPage.indexOf(\":\") != -1)" + NL +
            "        targetPage = \"undefined\";" + NL +
            "    function loadFrames() {" + NL +
            "        if (targetPage != \"\" && targetPage != \"undefined\")" + NL +
            "             top.classFrame.location = top.targetPage;" + NL +
            "    }" + NL +
            "</script>"},
        {BUG_ID + FS + "pkg" + FS + "C.html",
                "    if (location.href.indexOf('is-external=true') == -1) {" + NL +
                "        parent.document.title=\"C\";" + NL +
                        "    }"},
    };
    private static final String[][] NEGATED_TEST = NO_TEST;
    public static void main(String[] args) {
        TestJavascript tester = new TestJavascript();
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
