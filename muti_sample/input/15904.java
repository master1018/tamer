public class TestCmndLineClass extends JavadocTester {
    private static final String OUTPUT_DIR1 = "4506980-tmp1";
    private static final String OUTPUT_DIR2 = "4506980-tmp2";
    private static final String[][] TEST = NO_TEST;
    private static final String[][] NEGATED_TEST = NO_TEST;
    private static final String[] ARGS1 =
        new String[] {
            "-d", OUTPUT_DIR1, "-sourcepath", SRC_DIR,
            "-notimestamp", SRC_DIR + FS + "C5.java", "pkg1", "pkg2"
        };
    private static final String[] ARGS2 =
        new String[] {
            "-d", OUTPUT_DIR2, "-sourcepath", SRC_DIR,
            "-notimestamp", SRC_DIR + FS + "C5.java",
            SRC_DIR + FS + "pkg1" + FS + "C1.java",
            SRC_DIR + FS + "pkg1" + FS + "C2.java",
            SRC_DIR + FS + "pkg2" + FS + "C3.java",
            SRC_DIR + FS + "pkg2" + FS + "C4.java"
        };
    private static final String[][] FILES_TO_DIFF = {
        {OUTPUT_DIR1 + FS + "C5.html", OUTPUT_DIR2 + FS + "C5.html"},
        {OUTPUT_DIR2 + FS + "pkg1" + FS + "C1.html", OUTPUT_DIR2 + FS + "pkg1" + FS + "C1.html"},
        {OUTPUT_DIR1 + FS + "pkg1" + FS + "C2.html", OUTPUT_DIR2 + FS + "pkg1" + FS + "C2.html"},
        {OUTPUT_DIR1 + FS + "pkg2" + FS + "C3.html", OUTPUT_DIR2 + FS + "pkg2" + FS + "C3.html"},
        {OUTPUT_DIR1 + FS + "pkg2" + FS + "C4.html", OUTPUT_DIR2 + FS + "pkg2" + FS + "C4.html"}
    };
    private static final String BUG_ID = "4506980";
    public static void main(String[] args) {
        TestCmndLineClass tester = new TestCmndLineClass();
        run(tester, ARGS1, TEST, NEGATED_TEST);
        run(tester, ARGS2, TEST, NEGATED_TEST);
        tester.runDiffs(FILES_TO_DIFF);
    }
    public String getBugId() {
        return BUG_ID;
    }
    public String getBugName() {
        return getClass().getName();
    }
}
