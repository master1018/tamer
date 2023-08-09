public class TestHelpOption extends JavadocTester {
    private static final String BUG_ID = "4934778-4777599-6553182";
    private static final String[] ARGS = new String[] {
        "-d", BUG_ID, "-sourcepath", SRC_DIR, "-help",
            SRC_DIR + FS + "TestHelpOption.java"
    };
    private static final String[] ARGS2 = new String[] {
        "-d", BUG_ID, "-sourcepath", SRC_DIR,
            SRC_DIR + FS + "TestHelpOption.java"
    };
    private static final String[][] TEST = {
        {STANDARD_OUTPUT, "-d "},
        {STANDARD_OUTPUT, "-use "},
        {STANDARD_OUTPUT, "-version "},
        {STANDARD_OUTPUT, "-author "},
        {STANDARD_OUTPUT, "-docfilessubdirs "},
        {STANDARD_OUTPUT, "-splitindex "},
        {STANDARD_OUTPUT, "-windowtitle "},
        {STANDARD_OUTPUT, "-doctitle "},
        {STANDARD_OUTPUT, "-header "},
        {STANDARD_OUTPUT, "-footer "},
        {STANDARD_OUTPUT, "-bottom "},
        {STANDARD_OUTPUT, "-link "},
        {STANDARD_OUTPUT, "-linkoffline "},
        {STANDARD_OUTPUT, "-excludedocfilessubdir "},
        {STANDARD_OUTPUT, "-group "},
        {STANDARD_OUTPUT, "-nocomment "},
        {STANDARD_OUTPUT, "-nodeprecated "},
        {STANDARD_OUTPUT, "-noqualifier "},
        {STANDARD_OUTPUT, "-nosince "},
        {STANDARD_OUTPUT, "-notimestamp "},
        {STANDARD_OUTPUT, "-nodeprecatedlist "},
        {STANDARD_OUTPUT, "-notree "},
        {STANDARD_OUTPUT, "-noindex "},
        {STANDARD_OUTPUT, "-nohelp "},
        {STANDARD_OUTPUT, "-nonavbar "},
        {STANDARD_OUTPUT, "-serialwarn "},
        {STANDARD_OUTPUT, "-tag "},
        {STANDARD_OUTPUT, "-taglet "},
        {STANDARD_OUTPUT, "-tagletpath "},
        {STANDARD_OUTPUT, "-Xdocrootparent "},
        {STANDARD_OUTPUT, "-charset "},
        {STANDARD_OUTPUT, "-helpfile "},
        {STANDARD_OUTPUT, "-linksource "},
        {STANDARD_OUTPUT, "-sourcetab "},
        {STANDARD_OUTPUT, "-keywords "},
        {STANDARD_OUTPUT, "-stylesheetfile "},
        {STANDARD_OUTPUT, "-docencoding "},
    };
    private static final String[][] NEGATED_TEST = NO_TEST;
    private static final String[][] TEST2 = {
        {BUG_ID + FS + "TestHelpOption.html",
            "<li><a href=\"help-doc.html\">Help</a></li>"
        },
    };
    private static final String[][] NEGATED_TEST2 = NO_TEST;
    private static final int EXPECTED_EXIT_CODE = 0;
    public static void main(String[] args) {
        TestHelpOption tester = new TestHelpOption();
        int actualExitCode = run(tester, ARGS, TEST, NEGATED_TEST);
        tester.checkExitCode(EXPECTED_EXIT_CODE, actualExitCode);
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
