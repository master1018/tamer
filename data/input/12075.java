public class TestTaglets extends JavadocTester {
    private static final String BUG_ID = "4654308-4767038";
    private static final String OUTPUT_DIR = BUG_ID;
    private static final String[] ARGS_4654308 = new String[] {
        "-d", "4654308", "-tagletpath", SRC_DIR, "-taglet", "taglets.Foo",
        "-sourcepath", SRC_DIR, SRC_DIR + FS + "C.java"
    };
    private static final String[] ARGS_4767038 = new String[] {
        "-d", "4767038", "-sourcepath", SRC_DIR, SRC_DIR + FS + "Parent.java",
        SRC_DIR + FS + "Child.java"
    };
    private static final String[][] TEST_4654308 = new String[][] {
        {"4654308" + FS + "C.html", "<span class=\"strong\">Foo:</span></dt>" +
                 "<dd>my only method is <a href=\"C.html#method()\"><code>here" +
                 "</code></a></dd></dl>"}
    };
    private static final String[][] NEGATED_TEST_4654308 = NO_TEST;
    private static final String[][] TEST_4767038 = new String[][] {
        {"4767038" + FS + "Child.html",
            "This is the first sentence."}
    };
    private static final String[][] NEGATED_TEST_4767038 = NO_TEST;
    public static void main(String[] args) {
        TestTaglets tester = new TestTaglets();
        run(tester, ARGS_4654308, TEST_4654308, NEGATED_TEST_4654308);
        tester.printSummary();
        tester = new TestTaglets();
        run(tester, ARGS_4767038, TEST_4767038, NEGATED_TEST_4767038);
        tester.printSummary();
    }
    public String getBugId() {
        return BUG_ID;
    }
    public String getBugName() {
        return getClass().getName();
    }
}
