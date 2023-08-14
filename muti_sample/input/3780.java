public class TestLinkOption extends JavadocTester {
    private static final String BUG_ID = "4720957-5020118";
    private static final String[] ARGS1 = new String[] {
        "-d", BUG_ID + "-1", "-sourcepath", SRC_DIR,
        "-linkoffline", "http:
        SRC_DIR, "-package", "pkg", "java.lang"
    };
    private static final String[][] TEST1 = {
        {BUG_ID + "-1" + FS + "pkg" + FS + "C.html",
            "<a href=\"http:
            "title=\"class or interface in java.lang\"><code>Link to String Class</code></a>"
        },
        {BUG_ID + "-1" + FS + "pkg" + FS + "C.html",
                                "(int&nbsp;p1," + NL +
                                "      int&nbsp;p2," + NL +
                                "      int&nbsp;p3)"
        },
        {BUG_ID + "-1" + FS + "pkg" + FS + "C.html",
                                "(int&nbsp;p1," + NL +
                                "      int&nbsp;p2," + NL +
                                "      <a href=\"http:
                                "Object</a>&nbsp;p3)"
        },
        {BUG_ID + "-1" + FS + "java" + FS + "lang" + FS + "StringBuilderChild.html",
                "<pre>public abstract class <span class=\"strong\">StringBuilderChild</span>" + NL +
                "extends <a href=\"http:
                "title=\"class or interface in java.lang\">Object</a></pre>"
        },
    };
    private static final String[][] NEGATED_TEST1 = NO_TEST;
    private static final String[] ARGS2 = new String[] {
        "-d", BUG_ID + "-2", "-sourcepath", SRC_DIR,
        "-linkoffline", "../" + BUG_ID + "-1", BUG_ID + "-1", "-package", "pkg2"
    };
    private static final String[][] TEST2 = {
        {BUG_ID + "-2" + FS + "pkg2" + FS + "C2.html",
            "This is a link to <a href=\"../../" + BUG_ID + "-1/pkg/C.html?is-external=true\" " +
            "title=\"class or interface in pkg\"><code>Class C</code></a>."
        }
    };
    private static final String[][] NEGATED_TEST2 = NO_TEST;
    public static void main(String[] args) {
        TestLinkOption tester = new TestLinkOption();
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
