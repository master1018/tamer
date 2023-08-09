public class TestLegacyTaglet extends JavadocTester {
    private static final String BUG_ID = "4638723";
    private static final String[] ARGS =
        new String[] {"-d", BUG_ID, "-sourcepath", SRC_DIR,
            "-tagletpath", SRC_DIR, "-taglet", "ToDoTaglet",
            "-taglet", "UnderlineTaglet", SRC_DIR + FS + "C.java"};
    private static final String[][] TEST = new String[][] {
            {BUG_ID + FS + "C.html", "This is an <u>underline</u>"},
            {BUG_ID + FS + "C.html",
            "<DT><B>To Do:</B><DD><table cellpadding=2 cellspacing=0><tr>" +
                "<td bgcolor=\"yellow\">Finish this class.</td></tr></table></DD>"}};
    private static final String[][] NEGATED_TEST = NO_TEST;
    public static void main(String[] args) {
        TestLegacyTaglet tester = new TestLegacyTaglet();
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
