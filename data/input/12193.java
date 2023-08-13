public class TestDupThrowsTags extends JavadocTester {
    private static final String BUG_ID = "4525364";
    private static final String[] ARGS = new String[] {
        "-d", BUG_ID, SRC_DIR + FS + "TestDupThrowsTags.java"
    };
    public static void main(String[] args) {
        String[][] tests = new String[4][2];
        for (int i = 0; i < tests.length; i++) {
            tests[i][0] = BUG_ID + FS + "TestDupThrowsTags.html";
            tests[i][1] = "Test "+(i+1)+" passes";
        }
        TestDupThrowsTags tester = new TestDupThrowsTags();
        run(tester, ARGS, tests, NO_TEST);
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
