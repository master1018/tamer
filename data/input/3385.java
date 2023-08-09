public class TestRelativeLinks extends JavadocTester {
    private static final String BUG_ID = "4460354";
    private static final String[] ARGS = new String[] {
        "-d", BUG_ID, "-use", "-sourcepath", SRC_DIR, "pkg", "pkg2"
    };
    private static final String[][] TEST = {
        {BUG_ID + FS + "pkg" + FS + "C.html",
            "<a href=\"relative-class-link.html\">relative class link</a>"},
        {BUG_ID + FS + "pkg" + FS + "C.html",
            "<a href=\"relative-field-link.html\">relative field link</a>"},
        {BUG_ID + FS + "pkg" + FS + "C.html",
            "<a href=\"relative-method-link.html\">relative method link</a>"},
        {BUG_ID + FS + "pkg" + FS + "package-summary.html",
            "<a href=\"relative-package-link.html\">relative package link</a>"},
        {BUG_ID + FS + "pkg" + FS + "C.html",
            " <a\n" +
            " href=\"relative-multi-line-link.html\">relative-multi-line-link</a>."},
        {BUG_ID + FS + "index-all.html",
            "<a href=\"./pkg/relative-class-link.html\">relative class link</a>"},
        {BUG_ID + FS + "index-all.html",
            "<a href=\"./pkg/relative-field-link.html\">relative field link</a>"},
        {BUG_ID + FS + "index-all.html",
            "<a href=\"./pkg/relative-method-link.html\">relative method link</a>"},
        {BUG_ID + FS + "index-all.html",
            "<a href=\"./pkg/relative-package-link.html\">relative package link</a>"},
        {BUG_ID + FS + "index-all.html",
            " <a\n" +
            " href=\"./pkg/relative-multi-line-link.html\">relative-multi-line-link</a>."},
        {BUG_ID + FS + "pkg" + FS + "package-use.html",
            "<a href=\"../pkg/relative-package-link.html\">relative package link</a>."},
        {BUG_ID + FS + "pkg" + FS + "package-use.html",
            "<a href=\"../pkg/relative-class-link.html\">relative class link</a>"},
        {BUG_ID + FS + "pkg" + FS + "class-use" + FS + "C.html",
            "<a href=\"../../pkg/relative-field-link.html\">relative field link</a>"},
        {BUG_ID + FS + "pkg" + FS + "class-use" + FS + "C.html",
            "<a href=\"../../pkg/relative-method-link.html\">relative method link</a>"},
        {BUG_ID + FS + "pkg" + FS + "class-use" + FS + "C.html",
            "<a href=\"../../pkg/relative-package-link.html\">relative package link</a>"},
        {BUG_ID + FS + "pkg" + FS + "class-use" + FS + "C.html",
            " <a\n" +
            " href=\"../../pkg/relative-multi-line-link.html\">relative-multi-line-link</a>."},
        {BUG_ID + FS + "overview-summary.html",
            "<a href=\"./pkg/relative-package-link.html\">relative package link</a>"},
    };
    private static final String[][] NEGATED_TEST = NO_TEST;
    public static void main(String[] args) {
        TestRelativeLinks tester = new TestRelativeLinks();
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
