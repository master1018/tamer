public class TestNestedInlineTag extends JavadocTester {
    public int field;
    public TestNestedInlineTag(){}
    public void method(){}
    private static final String BUG_ID = "no-bug-id";
    private static final String[][] TEST = {
        {BUG_ID + FS + "TestNestedInlineTag.html",
         "This should be green, underlined and bold (Class): <u><b><font color=\"green\">My test</font></b></u>"
        },
        {BUG_ID + FS + "TestNestedInlineTag.html",
         "This should be green, underlined and bold (Field): <u><b><font color=\"green\">My test</font></b></u>"
        },
        {BUG_ID + FS + "TestNestedInlineTag.html",
         "This should be green, underlined and bold (Constructor): <u><b><font color=\"green\">My test</font></b></u>"
        },
        {BUG_ID + FS + "TestNestedInlineTag.html",
         "This should be green, underlined and bold (Method): <u><b><font color=\"green\">My test</font></b></u>"
        }
    };
    private static final String[][] NEGATED_TEST = NO_TEST;
    private static final String[] ARGS =
        new String[] {
            "-d", BUG_ID, "-sourcepath", SRC_DIR,
            "-taglet", "testtaglets.UnderlineTaglet",
            "-taglet", "testtaglets.BoldTaglet",
            "-taglet", "testtaglets.GreenTaglet",
            SRC_DIR + FS + "TestNestedInlineTag.java"
        };
    public static void main(String[] args) {
        TestNestedInlineTag tester = new TestNestedInlineTag();
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
