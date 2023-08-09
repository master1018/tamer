public class TestMemberInheritence extends JavadocTester {
    private static final String BUG_ID = "4638588-4635809-6256068-6270645";
    private static final String[][] TEST = {
        {BUG_ID + FS + "pkg" + FS + "SubClass.html",
         "<a href=\"../pkg/BaseClass.html#pubField\">"},
        {BUG_ID + FS + "pkg" + FS + "SubClass.html",
         "<a href=\"../pkg/BaseClass.html#pubMethod()\">"},
        {BUG_ID + FS + "pkg" + FS + "SubClass.html",
         "<a href=\"../pkg/BaseClass.pubInnerClass.html\" title=\"class in pkg\">"},
        {BUG_ID + FS + "pkg" + FS + "SubClass.html",
         "<a href=\"../pkg/BaseClass.html#proField\">"},
        {BUG_ID + FS + "pkg" + FS + "SubClass.html",
         "<a href=\"../pkg/BaseClass.html#proMethod()\">"},
        {BUG_ID + FS + "pkg" + FS + "SubClass.html",
         "<a href=\"../pkg/BaseClass.proInnerClass.html\" title=\"class in pkg\">"},
        {BUG_ID + FS + "pkg" + FS + "SubClass.html",
         "Nested classes/interfaces inherited from class&nbsp;pkg." +
                 "<a href=\"../pkg/BaseClass.html\" title=\"class in pkg\">BaseClass</a>"},
        {BUG_ID + FS + "pkg" + FS + "SubClass.html",
         "Nested classes/interfaces inherited from interface&nbsp;pkg." +
                 "<a href=\"../pkg/BaseInterface.html\" title=\"interface in pkg\">BaseInterface</a>"},
                 {BUG_ID + FS + "pkg" + FS + "BaseClass.html",
         "<dl>" + NL + "<dt><strong>Specified by:</strong></dt>" + NL +
                          "<dd><code><a href=\"../pkg/BaseInterface.html#getAnnotation(java.lang.Class)\">" +
                          "getAnnotation</a></code>&nbsp;in interface&nbsp;<code>" +
                          "<a href=\"../pkg/BaseInterface.html\" title=\"interface in pkg\">" +
                          "BaseInterface</a></code></dd>" + NL + "</dl>"},
                 {BUG_ID + FS + "diamond" + FS + "Z.html",
                 "<code><a href=\"../diamond/A.html#aMethod()\">aMethod</a></code>"},
                 {BUG_ID + FS + "inheritDist" + FS + "C.html",
                 "<div class=\"block\">m1-B</div>"},
    };
    private static final String[][] NEGATED_TEST = {
        {BUG_ID + FS + "pkg" + FS + "SubClass.html",
        "<a href=\"../pkg/BaseClass.html#staticMethod()\">staticMethod</a></code>"},
    };
    private static final String[] ARGS =
        new String[] {
            "-d", BUG_ID, "-sourcepath", SRC_DIR, "pkg", "diamond", "inheritDist"};
    public static void main(String[] args) {
        TestMemberInheritence tester = new TestMemberInheritence();
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
