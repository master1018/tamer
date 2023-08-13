public class TestConstantValuesDriver extends JavadocTester {
    private static final String BUG_ID = "4504730-4526070-5077317";
    private static final String[] ARGS = new String[] {
        "-d", BUG_ID, SRC_DIR + FS + "TestConstantValues.java",
        SRC_DIR + FS + "TestConstantValues2.java",
        SRC_DIR + FS + "A.java"
    };
    public static void main(String[] args) {
        String[][] tests = new String[5][2];
        for (int i = 0; i < tests.length-1; i++) {
            tests[i][0] = BUG_ID + FS + "constant-values.html";
            tests[i][1] = "TEST"+(i+1)+"PASSES";
        }
        tests[tests.length-1][0] = BUG_ID + FS + "constant-values.html";
        tests[tests.length-1][1] = "<code>\"&lt;Hello World&gt;\"</code>";
        TestConstantValuesDriver tester = new TestConstantValuesDriver();
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
