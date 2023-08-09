public class TestBadPackageFileInJar extends JavadocTester {
    private static final String BUG_ID = "4691095";
    private static final String[][] TEST =
        new String[][] {
            {ERROR_OUTPUT,
                "badPackageFileInJar.jar" +FS+"pkg/package.html: error - Body tag missing from HTML"}
        };
    private static final String[] ARGS =
        new String[] {
            "-d", BUG_ID, "-sourcepath", SRC_DIR, "-classpath",
            SRC_DIR + FS + "badPackageFileInJar.jar", "pkg"};
    public static void main(String[] args) {
        TestBadPackageFileInJar tester = new TestBadPackageFileInJar();
        run(tester, ARGS, TEST, NO_TEST);
        tester.printSummary();
    }
    public String getBugId() {
        return BUG_ID;
    }
    public String getBugName() {
        return getClass().getName();
    }
}
