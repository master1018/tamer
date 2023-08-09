public class LeadingSpaces extends JavadocTester {
    private static final String BUG_ID = "4232882";
    private static final String[][] TEST = {
        {BUG_ID + FS + "LeadingSpaces.html",
"        1\n" +
"          2\n" +
"            3\n" +
"              4\n" +
"                5\n" +
"                  6\n" +
"                    7"}
    };
    private static final String[][] NEGATED_TEST = NO_TEST;
    private static final String[] ARGS =
        new String[] {
            "-d", BUG_ID, "-sourcepath", SRC_DIR,
        SRC_DIR + FS + "LeadingSpaces.java"};
    public static void main(String[] args) {
        LeadingSpaces tester = new LeadingSpaces();
        run(tester, ARGS, TEST, NEGATED_TEST);
        tester.printSummary();
    }
    public String getBugId() {
        return BUG_ID;
    }
    public String getBugName() {
        return getClass().getName();
    }
    public void method(){}
}
