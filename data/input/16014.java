public class TestOverridenPrivateMethodsWithPrivateFlag extends JavadocTester {
    private static final String BUG_ID = "4634891";
    private static final String[][] TEST = {
        {BUG_ID + FS + "pkg1" + FS + "SubClass.html",
         "<dt><strong>Overrides:</strong></dt>" + NL +
                 "<dd><code><a href=\"../pkg1/BaseClass.html#publicMethod"},
        {BUG_ID + FS + "pkg1" + FS + "SubClass.html",
         "<dt><strong>Overrides:</strong></dt>" + NL +
                 "<dd><code><a href=\"../pkg1/BaseClass.html#packagePrivateMethod"},
        {BUG_ID + FS + "pkg2" + FS + "SubClass.html",
         "<dt><strong>Overrides:</strong></dt>" + NL +
                 "<dd><code><a href=\"../pkg1/BaseClass.html#publicMethod"},
    };
    private static final String[][] NEGATED_TEST = {
        {BUG_ID + FS + "pkg1" + FS + "SubClass.html",
         "<dt><strong>Overrides:</strong></dt>" + NL +
                 "<dd><code><a href=\"../pkg1/BaseClass.html#privateMethod"},
        {BUG_ID + FS + "pkg2" + FS + "SubClass.html",
         "<dt><strong>Overrides:</strong></dt>" + NL +
                 "<dd><code><a href=\"../pkg1/BaseClass.html#privateMethod"},
        {BUG_ID + FS + "pkg2" + FS + "SubClass.html",
         "<dt><strong>Overrides:</strong></dt>" + NL +
                 "<dd><code><a href=\"../pkg1/BaseClass.html#packagePrivateMethod"}
    };
    private static final String[] ARGS =
        new String[] {
            "-d", BUG_ID, "-sourcepath", SRC_DIR, "-private", "pkg1", "pkg2"};
    public static void main(String[] args) {
        TestOverridenPrivateMethodsWithPrivateFlag tester = new TestOverridenPrivateMethodsWithPrivateFlag();
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
