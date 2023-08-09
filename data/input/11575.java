public class MetaTag extends JavadocTester {
    private static final String BUG_ID = "4034096-4764726-6235799";
    private static final String OUTPUT_DIR = "docs-" + BUG_ID;
    private static final SimpleDateFormat m_dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private static final String[] ARGS = new String[] {
        "-d", OUTPUT_DIR,
        "-sourcepath", SRC_DIR,
        "-keywords",
        "-doctitle", "Sample Packages",
        "p1", "p2"
    };
    private static final String[] ARGS_NO_TIMESTAMP_NO_KEYWORDS = new String[] {
        "-d", OUTPUT_DIR + "-2",
        "-sourcepath", SRC_DIR,
        "-notimestamp",
        "-doctitle", "Sample Packages",
        "p1", "p2"
    };
    private static final String[][] TEST = {
        { OUTPUT_DIR + FS + "p1" + FS + "C1.html",
           "<meta name=\"keywords\" content=\"p1.C1 class\">" },
        { OUTPUT_DIR + FS + "p1" + FS + "C1.html",
           "<meta name=\"keywords\" content=\"field1\">" },
        { OUTPUT_DIR + FS + "p1" + FS + "C1.html",
           "<meta name=\"keywords\" content=\"field2\">" },
        { OUTPUT_DIR + FS + "p1" + FS + "C1.html",
           "<meta name=\"keywords\" content=\"method1()\">" },
        { OUTPUT_DIR + FS + "p1" + FS + "C1.html",
           "<meta name=\"keywords\" content=\"method2()\">" },
        { OUTPUT_DIR + FS + "p1" + FS + "package-summary.html",
           "<meta name=\"keywords\" content=\"p1 package\">" },
        { OUTPUT_DIR + FS + "overview-summary.html",
           "<meta name=\"keywords\" content=\"Overview, Sample Packages\">" },
        {OUTPUT_DIR + FS + "overview-summary.html",
           "<meta name=\"date\" "
                            + "content=\"" + m_dateFormat.format(new Date()) + "\">"},
    };
    private static final String[][] NEGATED_TEST = NO_TEST;
    private static final String[][] TEST2 = NO_TEST;
    private static final String[][] NEGATED_TEST2 = {
        { OUTPUT_DIR + "-2" + FS + "p1" + FS + "C1.html",
           "<META NAME=\"keywords\" CONTENT=\"p1.C1 class\">" },
        { OUTPUT_DIR + "-2" + FS + "p1" + FS + "C1.html",
           "<META NAME=\"keywords\" CONTENT=\"field1\">" },
        { OUTPUT_DIR + "-2" + FS + "p1" + FS + "C1.html",
           "<META NAME=\"keywords\" CONTENT=\"field2\">" },
        { OUTPUT_DIR + "-2" + FS + "p1" + FS + "C1.html",
           "<META NAME=\"keywords\" CONTENT=\"method1()\">" },
        { OUTPUT_DIR + "-2" + FS + "p1" + FS + "C1.html",
           "<META NAME=\"keywords\" CONTENT=\"method2()\">" },
        { OUTPUT_DIR + "-2" + FS + "p1" + FS + "package-summary.html",
           "<META NAME=\"keywords\" CONTENT=\"p1 package\">" },
        { OUTPUT_DIR + "-2" + FS + "overview-summary.html",
           "<META NAME=\"keywords\" CONTENT=\"Overview Summary, Sample Packages\">" },
        {OUTPUT_DIR + "-2" + FS + "overview-summary.html",
           "<META NAME=\"date\" "
                            + "CONTENT=\"" + m_dateFormat.format(new Date()) + "\">"},
    };
    public static void main(String[] args) {
        MetaTag tester = new MetaTag();
        Configuration config = ConfigurationImpl.getInstance();
        boolean defaultKeywordsSetting = config.keywords;
        boolean defaultTimestampSetting = config.notimestamp;
        run(tester, ARGS, TEST, NEGATED_TEST);
        config.keywords = defaultKeywordsSetting;
        config.notimestamp = defaultTimestampSetting;
        run(tester, ARGS_NO_TIMESTAMP_NO_KEYWORDS, TEST2, NEGATED_TEST2);
        tester.printSummary();
    }
    public String getBugId() {
        return BUG_ID;
    }
    public String getBugName() {
        return getClass().getName();
    }
}
