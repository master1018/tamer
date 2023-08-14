public class TestDupParamWarn extends JavadocTester {
    private static final String BUG_ID = "4745855";
    private static final String[] ARGS =
        new String[] {"-d", BUG_ID, "-sourcepath",
                SRC_DIR + FS, "pkg"};
    private static final String[][] NEGATED_TEST =
        new String[][] {{WARNING_OUTPUT,
            "Parameter \"a\" is documented more than once."}};
    public static void main(String[] args) {
        run(new TestDupParamWarn(), ARGS, NO_TEST, NEGATED_TEST);
    }
    public String getBugId() {
        return BUG_ID;
    }
    public String getBugName() {
        return getClass().getName();
    }
}
