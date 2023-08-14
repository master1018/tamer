public class TestSubTitle extends JavadocTester {
    private static final String BUG_ID = "7010342";
    private static final String[][] TEST = {
        {BUG_ID + FS + "pkg" + FS + "package-summary.html",
            "<div class=\"block\">This is the description of package pkg.</div>"
        },
        {BUG_ID + FS + "pkg" + FS + "C.html",
            "<div class=\"subTitle\">pkg</div>"
        }
    };
    private static final String[][] NEG_TEST = {
        {BUG_ID + FS + "pkg" + FS + "package-summary.html",
            "<p class=\"subTitle\">" + NL + "<div class=\"block\">This is the " +
            "description of package pkg.</div>" + NL + "</p>"
        },
        {BUG_ID + FS + "pkg" + FS + "C.html",
            "<p class=\"subTitle\">pkg</p>"
        }
    };
    private static final String[] ARGS = new String[]{
        "-d", BUG_ID, "-sourcepath", SRC_DIR, "pkg"
    };
    public static void main(String[] args) {
        TestSubTitle tester = new TestSubTitle();
        run(tester, ARGS, TEST, NEG_TEST);
        tester.printSummary();
    }
    public String getBugId() {
        return BUG_ID;
    }
    public String getBugName() {
        return getClass().getName();
    }
}
