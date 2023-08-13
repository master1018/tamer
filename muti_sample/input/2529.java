public class TestDocRootLink extends JavadocTester {
    private static final String BUG_ID = "6553182";
    private static final String[][] TEST1 = {
        {BUG_ID + FS + "pkg1" + FS + "C1.html",
            "<a href=\"../../technotes/guides/index.html\">"
        },
        {BUG_ID + FS + "pkg1" + FS + "package-summary.html",
            "<a href=\"../../technotes/guides/index.html\">"
        }
    };
    private static final String[][] NEGATED_TEST1 = {
        {BUG_ID + FS + "pkg1" + FS + "C1.html",
            "<a href=\"http:
        },
        {BUG_ID + FS + "pkg1" + FS + "package-summary.html",
            "<a href=\"http:
        }
    };
    private static final String[][] TEST2 = {
        {BUG_ID + FS + "pkg2" + FS + "C2.html",
            "<a href=\"http:
        },
        {BUG_ID + FS + "pkg2" + FS + "package-summary.html",
            "<a href=\"http:
        }
    };
    private static final String[][] NEGATED_TEST2 = {
        {BUG_ID + FS + "pkg2" + FS + "C2.html",
            "<a href=\"../../technotes/guides/index.html\">"
        },
        {BUG_ID + FS + "pkg2" + FS + "package-summary.html",
            "<a href=\"../../technotes/guides/index.html\">"
        }
    };
    private static final String[] ARGS1 =
            new String[]{
        "-d", BUG_ID, "-sourcepath", SRC_DIR, "pkg1"
    };
    private static final String[] ARGS2 =
            new String[]{
        "-d", BUG_ID, "-Xdocrootparent", "http:
    };
    public static void main(String[] args) {
        TestDocRootLink tester = new TestDocRootLink();
        run(tester, ARGS1, TEST1, NEGATED_TEST1);
        run(tester, ARGS2, TEST2, NEGATED_TEST2);
        tester.printSummary();
    }
    public String getBugId() {
        return BUG_ID;
    }
    public String getBugName() {
        return getClass().getName();
    }
}
