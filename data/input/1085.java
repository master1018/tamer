public class TestClassTree extends JavadocTester {
    private static final String BUG_ID = "4632553-4973607";
    private static final String[] ARGS = new String[] {
        "-d", BUG_ID, "-source",  "1.5","-sourcepath", SRC_DIR, "pkg"
    };
    private static final String[][] TEST = {
        {BUG_ID + FS + "pkg" + FS + "package-tree.html",
            "<ul>" + NL + "<li type=\"circle\">pkg.<a href=\"../pkg/ParentClass.html\" " +
            "title=\"class in pkg\"><span class=\"strong\">ParentClass</span></a>"},
        {BUG_ID + FS + "pkg" + FS + "package-tree.html",
            "<h2 title=\"Annotation Type Hierarchy\">Annotation Type Hierarchy</h2>" + NL +
            "<ul>" + NL + "<li type=\"circle\">pkg.<a href=\"../pkg/AnnotationType.html\" " +
            "title=\"annotation in pkg\"><span class=\"strong\">AnnotationType</span></a> " +
            "(implements java.lang.annotation.Annotation)</li>" + NL + "</ul>"},
        {BUG_ID + FS + "pkg" + FS + "package-tree.html",
            "<h2 title=\"Enum Hierarchy\">Enum Hierarchy</h2>" + NL + "<ul>" + NL +
            "<li type=\"circle\">java.lang.Object" + NL + "<ul>" + NL +
            "<li type=\"circle\">java.lang.Enum&lt;E&gt; (implements java.lang." +
            "Comparable&lt;T&gt;, java.io.Serializable)" + NL + "<ul>" + NL +
            "<li type=\"circle\">pkg.<a href=\"../pkg/Coin.html\" " +
            "title=\"enum in pkg\"><span class=\"strong\">Coin</span></a></li>" + NL +
            "</ul>" + NL + "</li>" + NL + "</ul>" + NL + "</li>" + NL + "</ul>"
        },
    };
    private static final String[][] NEGATED_TEST = {
        {BUG_ID + FS + "pkg" + FS + "package-tree.html",
            "<li type=\"circle\">class pkg.<a href=\"../pkg/ParentClass.html\" " +
            "title=\"class in pkg\"><span class=\"strong\">ParentClass</span></a></li>"}
        };
    public static void main(String[] args) {
        TestClassTree tester = new TestClassTree();
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
