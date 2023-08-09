public class TestRecurseSubPackages extends JavadocTester {
    private static final String BUG_ID = "4074234";
    private static final String[] ARGS =
        new String[] {
            "-d", BUG_ID, "-sourcepath", SRC_DIR,
            "-subpackages", "pkg1", "-exclude", "pkg1.pkg2.packageToExclude"
        };
    public static void main(String[] args) {
        String[][] tests = new String[6][2];
        for (int i = 0; i < tests.length; i++) {
            tests[i][0] = BUG_ID + FS + "allclasses-frame.html";
            tests[i][1] = "C" + (i+1) + ".html";
        }
        String[][] negatedTests = new String[][] {
            {BUG_ID + FS + "allclasses-frame.html", "DummyClass.html"}
        };
        TestRecurseSubPackages tester = new TestRecurseSubPackages();
        run(tester, ARGS, tests, negatedTests);
        tester.printSummary();
    }
    public String getBugId() {
        return BUG_ID;
    }
    public String getBugName() {
        return getClass().getName();
    }
}
