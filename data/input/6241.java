public class TestDocFileDir extends JavadocTester {
    private static final String BUG_ID = "4258405-4973606";
    private static final String[][] TEST1 = {
        {BUG_ID + "-1" + FS + "pkg" + FS + "doc-files" + FS + "testfile.txt",
            "This doc file did not get trashed."}
        };
    private static final String[][] NEGATED_TEST1 = NO_TEST;
    private static final String[][] TEST2 = {
        {BUG_ID + "-2" + FS + "pkg" + FS + "doc-files" + FS + "subdir-used1" +
            FS + "testfile.txt",
            "passed"
        },
        {BUG_ID + "-2" + FS + "pkg" + FS + "doc-files" + FS + "subdir-used2" +
            FS + "testfile.txt",
            "passed"
        },
    };
    private static final String[][] NEGATED_TEST2 = {
        {BUG_ID + "-2" + FS + "pkg" + FS + "doc-files" + FS + "subdir-excluded1" +
            FS + "testfile.txt",
            "passed"
        },
        {BUG_ID + "-2" + FS + "pkg" + FS + "doc-files" + FS + "subdir-excluded2" +
            FS + "testfile.txt",
            "passed"
        },
    };
    private static final String[][] TEST0 = {
        {"pkg" + FS + "doc-files" + FS + "testfile.txt",
            "This doc file did not get trashed."}
        };
    private static final String[][] NEGATED_TEST0 = {};
    private static final String[] ARGS1 =
        new String[] {
            "-d", BUG_ID + "-1", "-sourcepath",
            "blah" + String.valueOf(File.pathSeparatorChar) +
                BUG_ID + "-1" + String.valueOf(File.pathSeparatorChar) +
                "blah", "pkg"};
    private static final String[] ARGS2 =
        new String[] {
            "-d", BUG_ID + "-2", "-sourcepath", SRC_DIR,
            "-docfilessubdirs", "-excludedocfilessubdir",
            "subdir-excluded1:subdir-excluded2", "pkg"};
    private static final String[] ARGS0 =
        new String[] {"pkg" + FS + "C.java"};
    public static void main(String[] args) {
        TestDocFileDir tester = new TestDocFileDir();
        copyDir(SRC_DIR + FS + "pkg", ".");
        run(tester, ARGS0, TEST0, NEGATED_TEST0);
        copyDir(SRC_DIR + FS + "pkg", BUG_ID + "-1");
        run(tester, ARGS1, TEST1, NEGATED_TEST1);
        run(tester, ARGS2, TEST2, NEGATED_TEST2);
        tester.printSummary();
    }
    public String getBugId() {
        return BUG_ID;
    }
    public String getBugName() {
        return getClass().getName();
    }
}
