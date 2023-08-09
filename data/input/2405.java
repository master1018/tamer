public class TestPackagePage extends JavadocTester {
    private static final String BUG_ID = "4492643-4689286";
    private static final String[][] TEST1 = {
        {BUG_ID + "-1" + FS + "com" + FS + "pkg" + FS + "package-summary.html",
            "This is a package page."
        },
        {BUG_ID + "-1" + FS + "com" + FS + "pkg" + FS + "C.html",
            "<a href=\"../../com/pkg/package-summary.html\">Package</a>"
        },
        {BUG_ID + "-1" + FS + "com" + FS + "pkg" + FS + "package-tree.html",
            "<li><a href=\"../../com/pkg/package-summary.html\">Package</a></li>"
        },
        {BUG_ID + "-1" + FS + "deprecated-list.html",
            "<li><a href=\"com/pkg/package-summary.html\">Package</a></li>"
        },
        {BUG_ID + "-1" + FS + "index-all.html",
            "<li><a href=\"./com/pkg/package-summary.html\">Package</a></li>"
        },
        {BUG_ID + "-1" + FS + "help-doc.html",
            "<li><a href=\"com/pkg/package-summary.html\">Package</a></li>"
        },
    };
    private static final String[][] TEST2 = {
        {BUG_ID + "-2" + FS + "deprecated-list.html",
            "<li>Package</li>"
        },
        {BUG_ID + "-2" + FS + "index-all.html",
            "<li>Package</li>"
        },
        {BUG_ID + "-2" + FS + "help-doc.html",
            "<li>Package</li>"
        },
    };
    private static final String[] ARGS1 =
        new String[] {
            "-d", BUG_ID + "-1", "-sourcepath", SRC_DIR,
            SRC_DIR + FS + "com" + FS + "pkg" + FS + "C.java"
        };
    private static final String[] ARGS2 =
        new String[] {
            "-d", BUG_ID + "-2", "-sourcepath", SRC_DIR,
            "com.pkg", "pkg2"
        };
    public static void main(String[] args) {
        TestPackagePage tester = new TestPackagePage();
        run(tester, ARGS1, TEST1, NO_TEST);
        run(tester, ARGS2, TEST2, NO_TEST);
        tester.printSummary();
    }
    public String getBugId() {
        return BUG_ID;
    }
    public String getBugName() {
        return getClass().getName();
    }
}
