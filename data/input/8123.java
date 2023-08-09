public class TestLinkTaglet extends JavadocTester {
    private static final String BUG_ID = "4732864-6280605";
    private static final String[] ARGS = new String[] {
        "-d", BUG_ID, "-sourcepath", SRC_DIR, "pkg", SRC_DIR + FS + "checkPkg" + FS + "B.java"
    };
    private static final String[][] TEST = {
        {BUG_ID + FS + "pkg" + FS + "C.html",
            "Qualified Link: <a href=\"../pkg/C.InnerC.html\" title=\"class in pkg\"><code>C.InnerC</code></a>.<br/>\n" +
            " Unqualified Link1: <a href=\"../pkg/C.InnerC.html\" title=\"class in pkg\"><code>C.InnerC</code></a>.<br/>\n" +
            " Unqualified Link2: <a href=\"../pkg/C.InnerC.html\" title=\"class in pkg\"><code>C.InnerC</code></a>.<br/>\n" +
            " Qualified Link: <a href=\"../pkg/C.html#method(pkg.C.InnerC, pkg.C.InnerC2)\"><code>method(pkg.C.InnerC, pkg.C.InnerC2)</code></a>.<br/>\n" +
            " Unqualified Link: <a href=\"../pkg/C.html#method(pkg.C.InnerC, pkg.C.InnerC2)\"><code>method(C.InnerC, C.InnerC2)</code></a>.<br/>\n" +
            " Unqualified Link: <a href=\"../pkg/C.html#method(pkg.C.InnerC, pkg.C.InnerC2)\"><code>method(InnerC, InnerC2)</code></a>.<br/>"
        },
        {BUG_ID + FS + "pkg" + FS + "C.InnerC.html",
            "Link to member in outer class: <a href=\"../pkg/C.html#MEMBER\"><code>C.MEMBER</code></a> <br/>\n" +
            " Link to member in inner class: <a href=\"../pkg/C.InnerC2.html#MEMBER2\"><code>C.InnerC2.MEMBER2</code></a> <br/>\n" +
            " Link to another inner class: <a href=\"../pkg/C.InnerC2.html\" title=\"class in pkg\"><code>C.InnerC2</code></a>"
        },
        {BUG_ID + FS + "pkg" + FS + "C.InnerC2.html",
            "<dl>" + NL + "<dt>Enclosing class:</dt>" + NL +
            "<dd><a href=\"../pkg/C.html\" title=\"class in pkg\">C</a></dd>" + NL +
            "</dl>"
        },
    };
    private static final String[][] NEGATED_TEST = {
        {WARNING_OUTPUT, "Tag @see: reference not found: A"},
    };
    public static void main(String[] args) {
        TestLinkTaglet tester = new TestLinkTaglet();
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
