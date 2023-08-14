public class TestPackageDeprecation extends JavadocTester {
    private static final String BUG_ID = "6492694";
    private static final String[] ARGS1 = new String[]{
        "-d", BUG_ID + "-1", "-source", "1.5", "-sourcepath", SRC_DIR, "-use", "pkg", "pkg1",
        SRC_DIR + FS + "C2.java", SRC_DIR + FS + "FooDepr.java"
    };
    private static final String[] ARGS2 = new String[]{
        "-d", BUG_ID + "-2", "-source", "1.5", "-sourcepath", SRC_DIR, "-use", "-nodeprecated",
        "pkg", "pkg1", SRC_DIR + FS + "C2.java", SRC_DIR + FS + "FooDepr.java"
    };
    private static final String[][] TEST1 = {
        {BUG_ID + "-1" + FS + "pkg1" + FS + "package-summary.html",
            "<div class=\"deprecatedContent\"><span class=\"strong\">Deprecated.</span>" + NL +
            "<div class=\"block\"><i>This package is Deprecated.</i></div>"
        },
        {BUG_ID + "-1" + FS + "deprecated-list.html",
            "<li><a href=\"#package\">Deprecated Packages</a></li>"
        }
    };
    private static final String[][] TEST2 = NO_TEST;
    private static final String[][] NEGATED_TEST1 = NO_TEST;
    private static final String[][] NEGATED_TEST2 = {
        {BUG_ID + "-2" + FS + "overview-summary.html", "pkg1"},
        {BUG_ID + "-2" + FS + "allclasses-frame.html", "FooDepr"}
    };
    public static void main(String[] args) {
        TestPackageDeprecation tester = new TestPackageDeprecation();
        run(tester, ARGS1, TEST1, NEGATED_TEST1);
        run(tester, ARGS2, TEST2, NEGATED_TEST2);
        if ((new java.io.File(BUG_ID + "-2" + FS + "pkg1" + FS +
                "package-summary.html")).exists()) {
            throw new Error("Test Fails: packages summary should not be" +
                    "generated for deprecated package.");
        } else {
            System.out.println("Test passes:  package-summary.html not found.");
        }
        if ((new java.io.File(BUG_ID + "-2" + FS + "FooDepr.html")).exists()) {
            throw new Error("Test Fails: FooDepr should not be" +
                    "generated as it is deprecated.");
        } else {
            System.out.println("Test passes:  FooDepr.html not found.");
        }
        tester.printSummary();
    }
    public String getBugId() {
        return BUG_ID;
    }
    public String getBugName() {
        return getClass().getName();
    }
}
