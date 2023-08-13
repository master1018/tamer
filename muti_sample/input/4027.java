public class TestUseOption extends JavadocTester {
    private static final String BUG_ID = "4496290-4985072-7006178";
    private static final String[] TEST2 = {
        "Field in C1.",
        "Field in C2.",
        "Field in C4.",
        "Field in C5.",
        "Field in C6.",
        "Field in C7.",
        "Field in C8.",
        "Method in C1.",
        "Method in C2.",
        "Method in C4.",
        "Method in C5.",
        "Method in C6.",
        "Method in C7.",
        "Method in C8.",
    };
    private static final String[][] TEST3 = {
        {BUG_ID + "-3" + FS + "class-use" + FS + "UsedInC.html", "Uses of <a href=" +
                 "\"../UsedInC.html\" title=\"class in &lt;Unnamed&gt;\">" +
                 "UsedInC</a> in <a href=\"../package-summary.html\">&lt;Unnamed&gt;</a>"
        },
        {BUG_ID + "-3" + FS + "package-use.html", "<td class=\"colOne\">" +
                 "<a href=\"class-use/UsedInC.html#&lt;Unnamed&gt;\">UsedInC</a>&nbsp;</td>"
        }
    };
    private static final String[] ARGS = new String[] {
        "-d", BUG_ID, "-sourcepath", SRC_DIR, "-use", "pkg1", "pkg2"
    };
    private static final String[] ARGS2 = new String[] {
        "-d", BUG_ID+"-2", "-sourcepath", SRC_DIR, "-use", "pkg1", "pkg2"
    };
    private static final String[] ARGS3 = new String[] {
        "-d", BUG_ID + "-3", "-sourcepath", SRC_DIR, "-use", SRC_DIR + FS + "C.java", SRC_DIR + FS + "UsedInC.java"
    };
    public static void main(String[] args) throws Exception {
        String[][] tests = new String[11][2];
        for (int i = 0; i < 8; i++) {
            tests[i][0] = BUG_ID + FS + "pkg1" + FS + "class-use" + FS + "C1.html";
            tests[i][1] = "Test " + (i + 1) + " passes";
        }
        for (int i = 8, j = 1; i < tests.length; i++, j++) {
            tests[i][0] = BUG_ID + FS + "pkg1" + FS + "package-use.html";
            tests[i][1] = "Test " + j + " passes";
        }
        TestUseOption tester = new TestUseOption();
        run(tester, ARGS, tests, NO_TEST);
        tester.printSummary();
        run(tester, ARGS2, NO_TEST, NO_TEST);
        String usePageContents = tester.readFileToString(BUG_ID +"-2" + FS + "pkg1" + FS + "class-use" + FS + "UsedClass.html");
        int prevIndex = -1;
        int currentIndex = -1;
        for (int i = 0; i < TEST2.length; i++) {
            currentIndex = usePageContents.indexOf(TEST2[i]);
            System.err.println(TEST2[i] + " at index " + currentIndex);
            if (currentIndex < prevIndex)
                throw new Exception(TEST2[i] + " is in the wrong order.");
            prevIndex = currentIndex;
        }
        tester.printSummary();
        run(tester, ARGS3, TEST3, NO_TEST);
        tester.printSummary();
    }
    public String getBugId() {
        return BUG_ID;
    }
    public String getBugName() {
        return getClass().getName();
    }
}
