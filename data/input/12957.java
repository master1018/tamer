public class TestPrivateClasses extends JavadocTester {
    private static final String BUG_ID = "4780441-4874845-4978816";
    private static final String[] ARGS1 = new String[] {
        "-d", BUG_ID + "-1", "-sourcepath", SRC_DIR, "-source", "1.5", "pkg", "pkg2"
    };
    private static final String[] ARGS2 = new String[] {
        "-d", BUG_ID + "-2", "-sourcepath", SRC_DIR, "-private",
            "-source", "1.5", "pkg", "pkg2"
    };
    private static final String[][] TEST1 = {
        {BUG_ID + "-1" + FS + "pkg" + FS + "PublicChild.html",
            "<a href=\"../pkg/PublicChild.html#fieldInheritedFromParent\">" +
                "fieldInheritedFromParent</a>"
        },
        {BUG_ID + "-1" + FS + "pkg" + FS + "PublicChild.html",
            "<a href=\"../pkg/PublicChild.html#methodInheritedFromParent(int)\">" +
                "methodInheritedFromParent</a>"
        },
        {BUG_ID + "-1" + FS + "pkg" + FS + "PublicInterface.html",
            "<a href=\"../pkg/PublicInterface.html#fieldInheritedFromInterface\">" +
                "fieldInheritedFromInterface</a>"
        },
        {BUG_ID + "-1" + FS + "pkg" + FS + "PublicInterface.html",
            "<a href=\"../pkg/PublicInterface.html#methodInterface(int)\">" +
                "methodInterface</a>"
        },
        {BUG_ID + "-1" + FS + "pkg" + FS + "PublicChild.html",
            "<ul class=\"inheritance\">" + NL + "<li>java.lang.Object</li>" + NL +
            "<li>" + NL + "<ul class=\"inheritance\">" + NL + "<li>pkg.PublicChild</li>" + NL +
            "</ul>" + NL + "</li>" + NL + "</ul>"
        },
        {BUG_ID + "-1" + FS + "pkg" + FS + "PublicChild.html",
            "<pre>public&nbsp;void&nbsp;methodInheritedFromParent(int&nbsp;p1)"
        },
        {BUG_ID + "-1" + FS + "pkg" + FS + "PublicInterface.html",
            "<dl>" + NL + "<dt>All Known Implementing Classes:</dt>" + NL +
            "<dd><a href=\"../pkg/PublicChild.html\" title=\"class in pkg\">" +
            "PublicChild</a></dd>" + NL + "</dl>"},
        {BUG_ID + "-1" + FS + "pkg" + FS + "PublicChild.html",
            "<dl>" + NL + "<dt>All Implemented Interfaces:</dt>" + NL +
            "<dd><a href=\"../pkg/PublicInterface.html\" title=\"interface in pkg\">" +
            "PublicInterface</a></dd>" + NL + "</dl>"},
        {BUG_ID + "-1" + FS + "pkg2" + FS + "C.html",
            "This comment should get copied to the implementing class"},
    };
    private static final String[][] NEGATED_TEST1 = {
      {BUG_ID + "-1" + FS + "pkg" + FS + "PublicChild.html",
        "<strong>Overrides:</strong>"},
      {BUG_ID + "-1" + FS + "pkg" + FS + "PublicChild.html",
        "<strong>Specified by:</strong>"},
      {BUG_ID + "-1" + FS + "pkg" + FS + "PublicInterface.html",
        "<strong>Specified by:</strong>"},
      {BUG_ID + "-1" + FS + "pkg" + FS + "PublicChild.html",
        "Description copied from"},
      {BUG_ID + "-1" + FS + "pkg" + FS + "PublicInterface.html",
        "Description copied from"},
      {BUG_ID + "-1" + FS + "pkg" + FS + "PublicChild.html",
        "PrivateParent"},
      {BUG_ID + "-1" + FS + "pkg" + FS + "PublicInterface.html",
        "PrivateInterface"},
      {BUG_ID + "-1" + FS + "pkg" + FS + "PublicChild.html",
        "PrivateInterface"},
      {BUG_ID + "-1" + FS + "pkg" + FS + "PublicInterface.html",
        "All Superinterfaces"},
      {BUG_ID + "-1" + FS + "constant-values.html",
        "PrivateInterface"},
        {BUG_ID + "-1" + FS + "pkg2" + FS + "C.html",
            "<strong><a href=\"../pkg2/I.html#hello(T)\">hello</a></strong>"},
    };
    private static final String[][] TEST2 = {
        {BUG_ID + "-2" + FS + "pkg" + FS + "PublicChild.html",
            "Fields inherited from class&nbsp;pkg." +
            "<a href=\"../pkg/PrivateParent.html\" title=\"class in pkg\">" +
            "PrivateParent</a>"
        },
        {BUG_ID + "-2" + FS + "pkg" + FS + "PublicChild.html",
            "<a href=\"../pkg/PrivateParent.html#fieldInheritedFromParent\">" +
                "fieldInheritedFromParent</a>"
        },
        {BUG_ID + "-2" + FS + "pkg" + FS + "PublicInterface.html",
            "Fields inherited from interface&nbsp;pkg." +
            "<a href=\"../pkg/PrivateInterface.html\" title=\"interface in pkg\">" +
            "PrivateInterface</a>"
        },
        {BUG_ID + "-2" + FS + "pkg" + FS + "PublicInterface.html",
            "<a href=\"../pkg/PrivateInterface.html#fieldInheritedFromInterface\">" +
                "fieldInheritedFromInterface</a>"
        },
        {BUG_ID + "-2" + FS + "pkg" + FS + "PublicChild.html",
            "Methods inherited from class&nbsp;pkg." +
            "<a href=\"../pkg/PrivateParent.html\" title=\"class in pkg\">" +
            "PrivateParent</a>"
        },
        {BUG_ID + "-2" + FS + "pkg" + FS + "PublicChild.html",
            "<a href=\"../pkg/PrivateParent.html#methodInheritedFromParent(int)\">" +
                "methodInheritedFromParent</a>"
        },
       {BUG_ID + "-2" + FS + "pkg" + FS + "PublicChild.html",
            "<dt><strong>Overrides:</strong></dt>" + NL +
            "<dd><code><a href=\"../pkg/PrivateParent.html#methodOverridenFromParent(char[], int, T, V, java.util.List)\">" +
            "methodOverridenFromParent</a></code>&nbsp;in class&nbsp;<code>" +
            "<a href=\"../pkg/PrivateParent.html\" title=\"class in pkg\">" +
            "PrivateParent</a></code></dd>"},
       {BUG_ID + "-2" + FS + "pkg" + FS + "PublicChild.html",
            "<dt><strong>Specified by:</strong></dt>" + NL +
            "<dd><code><a href=\"../pkg/PrivateInterface.html#methodInterface(int)\">" +
            "methodInterface</a></code>&nbsp;in interface&nbsp;<code>" +
            "<a href=\"../pkg/PrivateInterface.html\" title=\"interface in pkg\">" +
            "PrivateInterface</a></code></dd>"},
       {BUG_ID + "-2" + FS + "pkg" + FS + "PublicInterface.html",
            "Methods inherited from interface&nbsp;pkg." +
            "<a href=\"../pkg/PrivateInterface.html\" title=\"interface in pkg\">" +
            "PrivateInterface</a>"
        },
        {BUG_ID + "-2" + FS + "pkg" + FS + "PrivateInterface.html",
            "<a href=\"../pkg/PrivateInterface.html#methodInterface(int)\">" +
                "methodInterface</a>"
        },
      {BUG_ID + "-2" + FS + "pkg" + FS + "PublicChild.html",
        "Description copied from"},
      {BUG_ID + "-2" + FS + "pkg" + FS + "PublicChild.html",
        "extends"},
      {BUG_ID + "-2" + FS + "pkg" + FS + "PublicInterface.html",
        "extends"},
      {BUG_ID + "-2" + FS + "pkg" + FS + "PublicInterface.html",
        "All Superinterfaces"},
      {BUG_ID + "-2" + FS + "pkg" + FS + "PublicInterface.html",
        "<dl>" + NL + "<dt>All Known Implementing Classes:</dt>" + NL +
        "<dd><a href=\"../pkg/PrivateParent.html\" title=\"class in pkg\">" +
        "PrivateParent</a>, " +
        "<a href=\"../pkg/PublicChild.html\" title=\"class in pkg\">PublicChild" +
        "</a></dd>" + NL + "</dl>"},
      {BUG_ID + "-2" + FS + "pkg" + FS + "PublicChild.html",
        "<dl>" + NL + "<dt>All Implemented Interfaces:</dt>" + NL +
        "<dd><a href=\"../pkg/PrivateInterface.html\" title=\"interface in pkg\">" +
        "PrivateInterface</a>, " +
        "<a href=\"../pkg/PublicInterface.html\" title=\"interface in pkg\">" +
        "PublicInterface</a></dd>" + NL + "</dl>"},
      {BUG_ID + "-2" + FS + "pkg2" + FS + "C.html",
            "<strong>Description copied from interface:&nbsp;<code>" +
            "<a href=\"../pkg2/I.html#hello(T)\">I</a></code></strong>"},
      {BUG_ID + "-2" + FS + "pkg2" + FS + "C.html",
            "<dt><strong>Specified by:</strong></dt>" + NL +
            "<dd><code><a href=\"../pkg2/I.html#hello(T)\">hello</a></code>" +
            "&nbsp;in interface&nbsp;<code>" +
            "<a href=\"../pkg2/I.html\" title=\"interface in pkg2\">I</a>" +
            "&lt;java.lang.String&gt;</code></dd>"},
    };
    private static final String[][] NEGATED_TEST2 = NO_TEST;
    public static void main(String[] args) {
        TestPrivateClasses tester = new TestPrivateClasses();
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
