public class TestInterface extends JavadocTester {
    private static final String BUG_ID = "4682448-4947464-5029946";
    private static final String[] ARGS = new String[] {
        "-d", BUG_ID, "-sourcepath", SRC_DIR, "pkg"
    };
    private static final String[][] TEST = {
        {BUG_ID + FS + "pkg" + FS + "Interface.html",
            "<pre>int&nbsp;method()</pre>"},
        {BUG_ID + FS + "pkg" + FS + "Interface.html",
            "<pre>static final&nbsp;int field</pre>"},
        {BUG_ID + FS + "pkg" + FS + "Interface.html",
            "<dl>" + NL + "<dt>All Known Implementing Classes:</dt>" + NL +
            "<dd><a href=\"../pkg/Child.html\" title=\"class in pkg\">Child" +
            "</a>, <a href=\"../pkg/Parent.html\" title=\"class in pkg\">Parent" +
            "</a></dd>" + NL + "</dl>"},
         {BUG_ID + FS + "pkg" + FS + "Child.html",
            "<dl>" + NL + "<dt>All Implemented Interfaces:</dt>" + NL +
            "<dd><a href=\"../pkg/Interface.html\" title=\"interface in pkg\">" +
            "Interface</a>&lt;T&gt;</dd>" + NL + "</dl>"
         },
         {BUG_ID + FS + "pkg" + FS + "Child.html",
            "<ul class=\"inheritance\">" + NL + "<li>java.lang.Object</li>" + NL +
            "<li>" + NL + "<ul class=\"inheritance\">" + NL +
            "<li><a href=\"../pkg/Parent.html\" title=\"class in pkg\">" +
            "pkg.Parent</a>&lt;T&gt;</li>" + NL + "<li>" + NL +
            "<ul class=\"inheritance\">" + NL + "<li>pkg.Child&lt;T&gt;</li>" + NL +
            "</ul>" + NL + "</li>" + NL + "</ul>" + NL + "</li>" + NL + "</ul>"
         },
        {BUG_ID + FS + "pkg" + FS + "Parent.html",
            "<dl>" + NL + "<dt>Direct Known Subclasses:</dt>" + NL +
            "<dd><a href=\"../pkg/Child.html\" title=\"class in pkg\">Child" +
            "</a></dd>" + NL + "</dl>"
        },
        {BUG_ID + FS + "pkg" + FS + "Child.html",
            "<dt><strong>Specified by:</strong></dt>" + NL +
            "<dd><code><a href=\"../pkg/Interface.html#method()\">method</a>" +
            "</code>&nbsp;in interface&nbsp;<code>" +
            "<a href=\"../pkg/Interface.html\" title=\"interface in pkg\">" +
            "Interface</a>&lt;<a href=\"../pkg/Child.html\" title=\"type parameter in Child\">" +
            "T</a>&gt;</code></dd>"
         },
        {BUG_ID + FS + "pkg" + FS + "Child.html",
            "<dt><strong>Overrides:</strong></dt>" + NL +
            "<dd><code><a href=\"../pkg/Parent.html#method()\">method</a>" +
            "</code>&nbsp;in class&nbsp;<code><a href=\"../pkg/Parent.html\" " +
            "title=\"class in pkg\">Parent</a>&lt;<a href=\"../pkg/Child.html\" " +
            "title=\"type parameter in Child\">T</a>&gt;</code></dd>"
         },
    };
    private static final String[][] NEGATED_TEST = {
        {BUG_ID + FS + "pkg" + FS + "Interface.html",
            "public int&nbsp;method()"},
        {BUG_ID + FS + "pkg" + FS + "Interface.html",
            "public static final&nbsp;int field"},
    };
    public static void main(String[] args) {
        TestInterface tester = new TestInterface();
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
