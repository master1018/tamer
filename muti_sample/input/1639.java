public class TestHref extends JavadocTester {
    private static final String BUG_ID = "4663254";
    private static final String[] ARGS = new String[] {
        "-d", BUG_ID, "-source", "1.5", "-sourcepath", SRC_DIR, "-linkoffline",
        "http:
    };
    private static final String[][] TEST = {
        {BUG_ID + FS + "pkg" + FS + "C1.html",
            "href=\"http:
        },
        {BUG_ID + FS + "pkg" + FS + "C1.html",
            "href=\"../pkg/C1.html#method(int, int, java.util.ArrayList)\""
        },
        {BUG_ID + FS + "pkg" + FS + "C1.html",
            "<a name=\"method(int, int, java.util.ArrayList)\">" + NL +
            "<!--   -->" + NL +
            "</a>"
        },
        {BUG_ID + FS + "pkg" + FS + "C1.html",
            "<a name=\"method(int, int, java.util.ArrayList)\">" + NL +
            "<!--   -->" + NL +
            "</a>"
        },
        {BUG_ID + FS + "pkg" + FS + "C2.html",
            "Link: <a href=\"../pkg/C1.html#method(int, int, java.util.ArrayList)\">"
        },
        {BUG_ID + FS + "pkg" + FS + "C2.html",
            "See Also:</span></dt><dd><a href=\"../pkg/C1.html#method(int, int, java.util.ArrayList)\">"
        },
        {BUG_ID + FS + "pkg" + FS + "C4.html",
            "Class C4&lt;E extends C4&lt;E&gt;&gt;</h2>"
        },
        {BUG_ID + FS + "pkg" + FS + "C4.html",
            "public abstract class <span class=\"strong\">C4&lt;E extends C4&lt;E&gt;&gt;</span>"
        },
    };
    private static final String[][] NEGATED_TEST =
    {
        {WARNING_OUTPUT,  "<a> tag is malformed"}
    };
    public static void main(String[] args) {
        TestHref tester = new TestHref();
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
