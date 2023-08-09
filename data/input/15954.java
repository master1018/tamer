public class TestMemberSummary extends JavadocTester {
    private static final String BUG_ID = "4951228-6290760";
    private static final String[] ARGS = new String[] {
        "-d", BUG_ID, "-sourcepath", SRC_DIR, "pkg","pkg2"
    };
    private static final String[][] TEST = {
        {BUG_ID + FS + "pkg" + FS + "PublicChild.html",
            "<code><a href=\"../pkg/PublicChild.html\" title=\"class in pkg\">PublicChild</a></code></td>" + NL +
            "<td class=\"colLast\"><code><strong><a href=\"../pkg/PublicChild.html#returnTypeTest()\">" +
            "returnTypeTest</a></strong>()</code>"
        },
        {BUG_ID + FS + "pkg" + FS + "PublicChild.html",
            "<pre>public&nbsp;<a href=\"../pkg/PublicChild.html\" title=\"class in pkg\">" +
            "PublicChild</a>&nbsp;returnTypeTest()</pre>"
        },
        {BUG_ID + FS + "pkg2" + FS + "A.html",
            "<a name=\"f(java.lang.Object[])\">" + NL +
            "<!--   -->" + NL +
            "</a><a name=\"f(T[])\">" + NL +
            "<!--   -->" + NL +
            "</a>"
        },
    };
    private static final String[][] NEGATED_TEST = NO_TEST;
    public static void main(String[] args) {
        TestMemberSummary tester = new TestMemberSummary();
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
