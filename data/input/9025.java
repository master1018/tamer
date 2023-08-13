public class TestNonFrameWarning extends JavadocTester {
    private static final String BUG_ID = "7001086";
    private static final String[][] TEST = {
        {BUG_ID + FS + "index.html",
            "<p>This document is designed to be viewed using the frames feature. " +
            "If you see this message, you are using a non-frame-capable web client. " +
            "Link to <a href=\"pkg/package-summary.html\">Non-frame version</a>.</p>"
        }
    };
    private static final String[] ARGS = new String[]{
        "-d", BUG_ID, "-sourcepath", SRC_DIR, "pkg"
    };
    public static void main(String[] args) {
        TestNonFrameWarning tester = new TestNonFrameWarning();
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
