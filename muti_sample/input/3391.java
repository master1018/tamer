public class TestSerializedFormDeprecationInfo extends JavadocTester {
    private static final String BUG_ID = "6802694";
    private static final String[][] TEST_CMNT_DEPR = {
        {BUG_ID + FS + "serialized-form.html", "<dl>" +
                 "<dt><span class=\"strong\">Throws:</span></dt>" + NL + "<dd><code>" +
                 "java.io.IOException</code></dd><dt><span class=\"strong\">See Also:</span>" +
                 "</dt><dd><a href=\"pkg1/C1.html#setUndecorated(boolean)\">" +
                 "<code>C1.setUndecorated(boolean)</code></a></dd></dl>"},
        {BUG_ID + FS + "serialized-form.html", "<span class=\"strong\">Deprecated.</span>" +
                 "&nbsp;<i>As of JDK version 1.5, replaced by" + NL +
                 " <a href=\"pkg1/C1.html#setUndecorated(boolean)\">" +
                 "<code>setUndecorated(boolean)</code></a>.</i></div>" + NL +
                 "<div class=\"block\">This field indicates whether the C1 " +
                 "is undecorated.</div>" + NL + "&nbsp;" + NL +
                 "<dl><dt><span class=\"strong\">Since:</span></dt>" + NL +
                 "  <dd>1.4</dd>" + NL + "<dt><span class=\"strong\">See Also:</span>" +
                 "</dt><dd><a href=\"pkg1/C1.html#setUndecorated(boolean)\">" +
                 "<code>C1.setUndecorated(boolean)</code></a></dd></dl>"},
        {BUG_ID + FS + "serialized-form.html", "<span class=\"strong\">Deprecated.</span>" +
                 "&nbsp;<i>As of JDK version 1.5, replaced by" + NL +
                 " <a href=\"pkg1/C1.html#setUndecorated(boolean)\">" +
                 "<code>setUndecorated(boolean)</code></a>.</i></div>" + NL +
                 "<div class=\"block\">Reads the object stream.</div>" + NL +
                 "<dl><dt><span class=\"strong\">Throws:</span></dt>" + NL + "<dd><code><code>" +
                 "IOException</code></code></dd>" + NL +
                 "<dd><code>java.io.IOException</code></dd></dl>"},
        {BUG_ID + FS + "serialized-form.html", "<span class=\"strong\">Deprecated.</span>" +
                 "&nbsp;</div>" + NL + "<div class=\"block\">" +
                 "The name for this class.</div>"}};
    private static final String[][] TEST_NOCMNT = {
        {BUG_ID + FS + "serialized-form.html", "<pre>boolean undecorated</pre>" + NL +
                 "<div class=\"block\"><span class=\"strong\">Deprecated.</span>&nbsp;<i>" +
                 "As of JDK version 1.5, replaced by" + NL +
                 " <a href=\"pkg1/C1.html#setUndecorated(boolean)\"><code>" +
                 "setUndecorated(boolean)</code></a>.</i></div>" + NL + "</li>"},
        {BUG_ID + FS + "serialized-form.html", "<span class=\"strong\">" +
                 "Deprecated.</span>&nbsp;<i>As of JDK version" +
                 " 1.5, replaced by" + NL +
                 " <a href=\"pkg1/C1.html#setUndecorated(boolean)\">" +
                 "<code>setUndecorated(boolean)</code></a>.</i></div>" + NL + "</li>"}};
    private static final String[][] TEST_NODEPR = TEST_CMNT_DEPR;
    private static final String[][] TEST_NOCMNT_NODEPR = TEST_NOCMNT;
    private static final String[] ARGS1 =
        new String[] {
            "-d", BUG_ID, "-sourcepath", SRC_DIR, "pkg1"};
    private static final String[] ARGS2 =
        new String[] {
            "-d", BUG_ID, "-nocomment", "-sourcepath", SRC_DIR, "pkg1"};
    private static final String[] ARGS3 =
        new String[] {
            "-d", BUG_ID, "-nodeprecated", "-sourcepath", SRC_DIR, "pkg1"};
    private static final String[] ARGS4 =
        new String[] {
            "-d", BUG_ID, "-nocomment", "-nodeprecated", "-sourcepath", SRC_DIR, "pkg1"};
    public static void main(String[] args) {
        TestSerializedFormDeprecationInfo tester = new TestSerializedFormDeprecationInfo();
        tester.exactNewlineMatch = false;
        run(tester, ARGS1, TEST_CMNT_DEPR, TEST_NOCMNT);
        run(tester, ARGS2, TEST_NOCMNT, TEST_CMNT_DEPR);
        run(tester, ARGS3, TEST_NODEPR, TEST_NOCMNT_NODEPR);
        run(tester, ARGS4, TEST_NOCMNT_NODEPR, TEST_NODEPR);
        tester.printSummary();
    }
    public String getBugId() {
        return BUG_ID;
    }
    public String getBugName() {
        return getClass().getName();
    }
}
