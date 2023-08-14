public class TestSimpleTag extends JavadocTester {
    private static final String BUG_ID = "4695326-4750173-4920381";
    private static final String[][] TEST =
        new String[][] {
            {"./" + BUG_ID + "/C.html",
                "<span class=\"strong\">Todo:</span>"},
            {"./" + BUG_ID + "/C.html",
                "<span class=\"strong\">EJB Beans:</span>"},
            {"./" + BUG_ID + "/C.html",
                "<span class=\"strong\">Regular Tag:</span>"},
            {"./" + BUG_ID + "/C.html",
                "<span class=\"strong\">Back-Slash-Tag:</span>"},
        };
    private static final String[] ARGS = new String[] {
        "-d", BUG_ID, "-sourcepath", SRC_DIR,
        "-tag", "todo",
        "-tag", "ejb\\:bean:a:EJB Beans:",
        "-tag", "regular:a:Regular Tag:",
        "-tag", "back-slash\\:tag\\\\:a:Back-Slash-Tag:",
        SRC_DIR + FS + "C.java"
    };
    public static void main(String[] args) {
        TestSimpleTag tester = new TestSimpleTag();
        run(tester, ARGS, TEST, NO_TEST);
        tester.printSummary();
    }
    public String getBugId() {
        return BUG_ID;
    }
    public String getBugName() {
        return getClass().getName();
    }
}
