public class TestDeprecatedDocs extends JavadocTester {
    private static final String BUG_ID = "4927552";
    private static final String[] ARGS = new String[] {
        "-d", BUG_ID, "-source", "1.5", "-sourcepath", SRC_DIR, "pkg"
    };
    private static final String TARGET_FILE  =
        BUG_ID + FS + "deprecated-list.html";
    private static final String TARGET_FILE2  =
        BUG_ID + FS + "pkg" + FS + "DeprecatedClassByAnnotation.html";
    private static final String[][] TEST = {
        {TARGET_FILE, "annotation_test1 passes"},
        {TARGET_FILE, "annotation_test2 passes"},
        {TARGET_FILE, "annotation_test3 passes"},
        {TARGET_FILE, "class_test1 passes"},
        {TARGET_FILE, "class_test2 passes"},
        {TARGET_FILE, "class_test3 passes"},
        {TARGET_FILE, "class_test4 passes"},
        {TARGET_FILE, "enum_test1 passes"},
        {TARGET_FILE, "enum_test2 passes"},
        {TARGET_FILE, "error_test1 passes"},
        {TARGET_FILE, "error_test2 passes"},
        {TARGET_FILE, "error_test3 passes"},
        {TARGET_FILE, "error_test4 passes"},
        {TARGET_FILE, "exception_test1 passes"},
        {TARGET_FILE, "exception_test2 passes"},
        {TARGET_FILE, "exception_test3 passes"},
        {TARGET_FILE, "exception_test4 passes"},
        {TARGET_FILE, "interface_test1 passes"},
        {TARGET_FILE, "interface_test2 passes"},
        {TARGET_FILE, "interface_test3 passes"},
        {TARGET_FILE, "interface_test4 passes"},
        {TARGET_FILE, "pkg.DeprecatedClassByAnnotation"},
        {TARGET_FILE, "pkg.DeprecatedClassByAnnotation()"},
        {TARGET_FILE, "pkg.DeprecatedClassByAnnotation.method()"},
        {TARGET_FILE, "pkg.DeprecatedClassByAnnotation.field"},
        {TARGET_FILE2, "<pre>@Deprecated" + NL +
                 "public class <span class=\"strong\">DeprecatedClassByAnnotation</span>" + NL +
                 "extends java.lang.Object</pre>"},
        {TARGET_FILE2, "<pre>@Deprecated" + NL +
                 "public&nbsp;int field</pre>" + NL +
                 "<div class=\"block\"><span class=\"strong\">Deprecated.</span>&nbsp;</div>"},
        {TARGET_FILE2, "<pre>@Deprecated" + NL +
                 "public&nbsp;DeprecatedClassByAnnotation()</pre>" + NL +
                 "<div class=\"block\"><span class=\"strong\">Deprecated.</span>&nbsp;</div>"},
        {TARGET_FILE2, "<pre>@Deprecated" + NL +
                 "public&nbsp;void&nbsp;method()</pre>" + NL +
                 "<div class=\"block\"><span class=\"strong\">Deprecated.</span>&nbsp;</div>"},
    };
    private static final String[][] NEGATED_TEST = NO_TEST;
    public static void main(String[] args) {
        TestDeprecatedDocs tester = new TestDeprecatedDocs();
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
