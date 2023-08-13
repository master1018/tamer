public class TestMultiInheritence extends JavadocTester {
    private static final String BUG_ID = "4933335";
    private static final String[] ARGS = new String[] {
        "-d", BUG_ID, "-sourcepath", SRC_DIR, "pkg3"
    };
    private static final String[][] TEST = {
       {BUG_ID + FS + "pkg3" + FS + "I1.html",
        "Methods inherited from interface&nbsp;pkg3." +
                "<a href=\"../pkg3/I2.html\" title=\"interface in pkg3\">" +
                "I2</a>"},
        {BUG_ID + FS + "pkg3" + FS +"I1.html",
        "Methods inherited from interface&nbsp;pkg3." +
                 "<a href=\"../pkg3/I3.html\" title=\"interface in pkg3\">" +
                 "I3</a>"},
        {BUG_ID + FS + "pkg3" + FS + "I0.html",
        "Methods inherited from interface&nbsp;pkg3." +
                 "<a href=\"../pkg3/I2.html\" title=\"interface in pkg3\">" +
                 "I2</a>"},
        {BUG_ID + FS + "pkg3" + FS +"I0.html",
        "Methods inherited from interface&nbsp;pkg3." +
                 "<a href=\"../pkg3/I3.html\" title=\"interface in pkg3\">" +
                 "I3</a>"},
    };
    private static final String[][] NEGATED_TEST = {
        {BUG_ID + FS + "pkg3" + FS + "I1.html",
        "Methods inherited from interface&nbsp;pkg3." +
                 "<a href=\"../pkg3/I4.html\" title=\"interface in pkg3\">" +
                 "I4</a>"},
        {BUG_ID + FS + "pkg3" + FS + "I0.html",
        "Methods inherited from interface&nbsp;pkg3." +
                 "<a href=\"../pkg3/I4.html\" title=\"interface in pkg3\">" +
                 "I4</a>"},
    };
    public static void main(String[] args) {
        TestMultiInheritence tester = new TestMultiInheritence();
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
