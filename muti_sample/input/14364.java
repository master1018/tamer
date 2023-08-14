public class TestHtmlDefinitionListTag extends JavadocTester {
    private static final String BUG_ID = "6786690-6820360";
    private static final String[][] TEST_ALL = {
        {BUG_ID + FS + "pkg1" + FS + "C1.html", "<pre>public class " +
                 "<span class=\"strong\">C1</span>" + NL +
                 "extends java.lang.Object" + NL + "implements java.io.Serializable</pre>"},
        {BUG_ID + FS + "pkg1" + FS + "C4.html", "<dl>" + NL +
                 "<dt>Default:</dt>" + NL + "<dd>true</dd>" + NL +
                 "</dl>"}};
    private static final String[][] TEST_CMNT_DEPR = {
        {BUG_ID + FS + "pkg1" + FS + "package-summary.html", "<dl>" +
                 "<dt><span class=\"strong\">Since:</span></dt>" + NL +
                 "  <dd>JDK1.0</dd></dl>"},
        {BUG_ID + FS + "pkg1" + FS + "C1.html", "<dl><dt><span class=\"strong\">Since:</span></dt>" + NL +
                 "  <dd>JDK1.0</dd>" + NL + "<dt><span class=\"strong\">See Also:</span></dt>" +
                 "<dd><a href=\"../pkg1/C2.html\" title=\"class in pkg1\"><code>" +
                 "C2</code></a>, " + NL + "<a href=\"../serialized-form.html#pkg1.C1\">" +
                 "Serialized Form</a></dd></dl>"},
        {BUG_ID + FS + "pkg1" + FS + "C1.html", "<dl><dt><span class=\"strong\">Since:</span></dt>" + NL +
                 "  <dd>1.4</dd>" + NL +
                 "<dt><span class=\"strong\">See Also:</span></dt><dd>" +
                 "<a href=\"../pkg1/C1.html#setUndecorated(boolean)\">" +
                 "<code>setUndecorated(boolean)</code></a></dd></dl>"},
        {BUG_ID + FS + "pkg1" + FS + "C1.html", "<dl><dt><span class=\"strong\">Parameters:</span></dt><dd><code>title" +
                 "</code> - the title</dd><dd><code>test</code> - boolean value" +
                 "</dd>" + NL + "<dt><span class=\"strong\">Throws:</span></dt>" + NL +
                 "<dd><code>java.lang.IllegalArgumentException</code> - if the " +
                 "<code>owner</code>'s" + NL +
                 "     <code>GraphicsConfiguration</code> is not from a screen " +
                 "device</dd>" + NL + "<dd><code>HeadlessException</code></dd></dl>"},
        {BUG_ID + FS + "pkg1" + FS + "C1.html", "<dl><dt><span class=\"strong\">Parameters:</span></dt><dd><code>undecorated" +
                 "</code> - <code>true</code> if no decorations are" + NL +
                 "         to be enabled;" + NL + "         <code>false</code> " +
                 "if decorations are to be enabled.</dd><dt><span class=\"strong\">Since:" +
                 "</span></dt>" + NL + "  <dd>1.4</dd>" + NL +
                 "<dt><span class=\"strong\">See Also:</span></dt><dd>" +
                 "<a href=\"../pkg1/C1.html#readObject()\"><code>readObject()" +
                 "</code></a></dd></dl>"},
        {BUG_ID + FS + "pkg1" + FS + "C1.html", "<dl><dt><span class=\"strong\">Throws:</span></dt>" + NL +
                 "<dd><code>java.io.IOException</code></dd><dt><span class=\"strong\">See Also:" +
                 "</span></dt><dd><a href=\"../pkg1/C1.html#setUndecorated(boolean)\">" +
                 "<code>setUndecorated(boolean)</code></a></dd></dl>"},
        {BUG_ID + FS + "pkg1" + FS + "C2.html", "<dl><dt><span class=\"strong\">Parameters:" +
                 "</span></dt><dd><code>set</code> - boolean</dd><dt><span class=\"strong\">" +
                 "Since:</span></dt>" + NL + "  <dd>1.4</dd></dl>"},
        {BUG_ID + FS + "serialized-form.html", "<dl><dt><span class=\"strong\">Throws:</span>" +
                 "</dt>" + NL + "<dd><code>" +
                 "java.io.IOException</code></dd><dt><span class=\"strong\">See Also:</span>" +
                 "</dt><dd><a href=\"pkg1/C1.html#setUndecorated(boolean)\">" +
                 "<code>C1.setUndecorated(boolean)</code></a></dd></dl>"},
        {BUG_ID + FS + "serialized-form.html", "<span class=\"strong\">Deprecated.</span>" +
                 "&nbsp;<i>As of JDK version 1.5, replaced by" + NL +
                 " <a href=\"pkg1/C1.html#setUndecorated(boolean)\">" +
                 "<code>setUndecorated(boolean)</code></a>.</i></div>" + NL +
                 "<div class=\"block\">This field indicates whether the C1 is " +
                 "undecorated.</div>" + NL + "&nbsp;" + NL + "<dl><dt><span class=\"strong\">Since:</span></dt>" + NL +
                 "  <dd>1.4</dd>" + NL + "<dt><span class=\"strong\">See Also:</span>" +
                 "</dt><dd><a href=\"pkg1/C1.html#setUndecorated(boolean)\">" +
                 "<code>C1.setUndecorated(boolean)</code></a></dd></dl>"},
        {BUG_ID + FS + "serialized-form.html", "<span class=\"strong\">Deprecated.</span>" +
                 "&nbsp;<i>As of JDK version 1.5, replaced by" + NL +
                 " <a href=\"pkg1/C1.html#setUndecorated(boolean)\">" +
                 "<code>setUndecorated(boolean)</code></a>.</i></div>" + NL +
                 "<div class=\"block\">Reads the object stream.</div>" + NL +
                 "<dl><dt><span class=\"strong\">Throws:" +
                 "</span></dt>" + NL + "<dd><code><code>" +
                 "IOException</code></code></dd>" + NL +
                 "<dd><code>java.io.IOException</code></dd></dl>"},
        {BUG_ID + FS + "serialized-form.html", "<span class=\"strong\">Deprecated.</span>" +
                 "&nbsp;</div>" + NL +
                 "<div class=\"block\">The name for this class.</div>"}};
    private static final String[][] TEST_NODEPR = {
        {BUG_ID + FS + "pkg1" + FS + "package-summary.html", "<dl>" +
                 "<dt><span class=\"strong\">Since:</span></dt>" + NL +
                 "  <dd>JDK1.0</dd></dl>"},
        {BUG_ID + FS + "pkg1" + FS + "C1.html", "<dl><dt><span class=\"strong\">Since:</span>" +
                 "</dt>" + NL + "  <dd>JDK1.0</dd>" + NL + "<dt><span class=\"strong\">See Also:" +
                 "</span></dt><dd><a href=\"../pkg1/C2.html\" title=\"class in pkg1\">" +
                 "<code>C2</code></a>, " + NL + "<a href=\"../serialized-form.html#pkg1.C1\">" +
                 "Serialized Form</a></dd></dl>"},
        {BUG_ID + FS + "pkg1" + FS + "C1.html", "<dl><dt><span class=\"strong\">Parameters:" +
                 "</span></dt><dd><code>title</code> - the title</dd><dd><code>" +
                 "test</code> - boolean value</dd>" + NL + "<dt><span class=\"strong\">Throws:" +
                 "</span></dt>" + NL + "<dd><code>java.lang.IllegalArgumentException" +
                 "</code> - if the <code>owner</code>'s" + NL + "     <code>GraphicsConfiguration" +
                 "</code> is not from a screen device</dd>" + NL + "<dd><code>" +
                 "HeadlessException</code></dd></dl>"},
        {BUG_ID + FS + "pkg1" + FS + "C1.html", "<dl><dt><span class=\"strong\">Parameters:" +
                 "</span></dt><dd><code>undecorated</code> - <code>true</code>" +
                 " if no decorations are" + NL + "         to be enabled;" + NL +
                 "         <code>false</code> if decorations are to be enabled." +
                 "</dd><dt><span class=\"strong\">Since:</span></dt>" + NL + "  <dd>1.4</dd>" + NL +
                 "<dt><span class=\"strong\">See Also:</span></dt><dd><a href=\"../pkg1/C1.html#readObject()\">" +
                 "<code>readObject()</code></a></dd></dl>"},
        {BUG_ID + FS + "pkg1" + FS + "C1.html", "<dl><dt><span class=\"strong\">Throws:</span>" +
                 "</dt>" + NL + "<dd><code>java.io.IOException</code></dd><dt>" +
                 "<span class=\"strong\">See Also:</span></dt><dd><a href=\"../pkg1/C1.html#setUndecorated(boolean)\">" +
                 "<code>setUndecorated(boolean)</code></a></dd></dl>"},
        {BUG_ID + FS + "serialized-form.html", "<dl><dt><span class=\"strong\">Throws:</span>" +
                 "</dt>" + NL + "<dd><code>" +
                 "java.io.IOException</code></dd><dt><span class=\"strong\">See Also:</span>" +
                 "</dt><dd><a href=\"pkg1/C1.html#setUndecorated(boolean)\">" +
                 "<code>C1.setUndecorated(boolean)</code></a></dd></dl>"},
        {BUG_ID + FS + "serialized-form.html", "<span class=\"strong\">Deprecated.</span>" +
                 "&nbsp;<i>As of JDK version 1.5, replaced by" + NL +
                 " <a href=\"pkg1/C1.html#setUndecorated(boolean)\">" +
                 "<code>setUndecorated(boolean)</code></a>.</i></div>" + NL +
                 "<div class=\"block\">This field indicates whether the C1 is " +
                 "undecorated.</div>" + NL + "&nbsp;" + NL + "<dl><dt><span class=\"strong\">Since:</span></dt>" + NL +
                 "  <dd>1.4</dd>" + NL + "<dt><span class=\"strong\">See Also:</span>" +
                 "</dt><dd><a href=\"pkg1/C1.html#setUndecorated(boolean)\">" +
                 "<code>C1.setUndecorated(boolean)</code></a></dd></dl>"},
        {BUG_ID + FS + "serialized-form.html", "<span class=\"strong\">Deprecated.</span>" +
                 "&nbsp;<i>As of JDK version 1.5, replaced by" + NL +
                 " <a href=\"pkg1/C1.html#setUndecorated(boolean)\">" +
                 "<code>setUndecorated(boolean)</code></a>.</i></div>" + NL +
                 "<div class=\"block\">Reads the object stream.</div>" + NL +
                 "<dl><dt><span class=\"strong\">Throws:" +
                 "</span></dt>" + NL + "<dd><code><code>" +
                 "IOException</code></code></dd>" + NL +
                 "<dd><code>java.io.IOException</code></dd></dl>"},
        {BUG_ID + FS + "serialized-form.html", "<span class=\"strong\">Deprecated.</span>" +
                 "&nbsp;</div>" + NL + "<div class=\"block\">" +
                 "The name for this class.</div>"}};
    private static final String[][] TEST_NOCMNT_NODEPR = {
        {BUG_ID + FS + "pkg1" + FS + "C1.html", "<pre>public&nbsp;void&nbsp;readObject()" + NL +
                 "                throws java.io.IOException</pre>" + NL + "</li>"},
        {BUG_ID + FS + "pkg1" + FS + "C2.html", "<pre>public&nbsp;C2()</pre>" + NL +
                 "</li>"},
        {BUG_ID + FS + "pkg1" + FS + "C1.ModalExclusionType.html", "<pre>public " +
                 "static final&nbsp;<a href=\"../pkg1/C1.ModalExclusionType.html\" " +
                 "title=\"enum in pkg1\">C1.ModalExclusionType</a> " +
                 "APPLICATION_EXCLUDE</pre>" + NL + "</li>"},
        {BUG_ID + FS + "serialized-form.html", "<pre>boolean " +
                 "undecorated</pre>" + NL + "<div class=\"block\"><span class=\"strong\">" +
                 "Deprecated.</span>&nbsp;<i>As of JDK version 1.5, replaced by" + NL +
                 " <a href=\"pkg1/C1.html#setUndecorated(boolean)\"><code>" +
                 "setUndecorated(boolean)</code></a>.</i></div>" + NL + "</li>"},
        {BUG_ID + FS + "serialized-form.html", "<span class=\"strong\">" +
                 "Deprecated.</span>&nbsp;<i>As of JDK version" +
                 " 1.5, replaced by" + NL +
                 " <a href=\"pkg1/C1.html#setUndecorated(boolean)\">" +
                 "<code>setUndecorated(boolean)</code></a>.</i></div>" + NL + "</li>"}};
    private static final String[][] NEGATED_TEST = {
        {BUG_ID + FS + "pkg1" + FS + "package-summary.html", "<dl></dl>"},
        {BUG_ID + FS + "pkg1" + FS + "package-summary.html", "<dl>" + NL + "</dl>"},
        {BUG_ID + FS + "pkg1" + FS + "C1.html", "<dl></dl>"},
        {BUG_ID + FS + "pkg1" + FS + "C1.html", "<dl>" + NL + "</dl>"},
        {BUG_ID + FS + "pkg1" + FS + "C1.ModalExclusionType.html", "<dl></dl>"},
        {BUG_ID + FS + "pkg1" + FS + "C1.ModalExclusionType.html", "<dl>" + NL + "</dl>"},
        {BUG_ID + FS + "pkg1" + FS + "C2.html", "<dl></dl>"},
        {BUG_ID + FS + "pkg1" + FS + "C2.html", "<dl>" + NL + "</dl>"},
        {BUG_ID + FS + "pkg1" + FS + "C2.ModalType.html", "<dl></dl>"},
        {BUG_ID + FS + "pkg1" + FS + "C2.ModalType.html", "<dl>" + NL + "</dl>"},
        {BUG_ID + FS + "pkg1" + FS + "C3.html", "<dl></dl>"},
        {BUG_ID + FS + "pkg1" + FS + "C3.html", "<dl>" + NL + "</dl>"},
        {BUG_ID + FS + "pkg1" + FS + "C4.html", "<dl></dl>"},
        {BUG_ID + FS + "pkg1" + FS + "C4.html", "<dl>" + NL + "</dl>"},
        {BUG_ID + FS + "pkg1" + FS + "C5.html", "<dl></dl>"},
        {BUG_ID + FS + "pkg1" + FS + "C5.html", "<dl>" + NL + "</dl>"},
        {BUG_ID + FS + "overview-tree.html", "<dl></dl>"},
        {BUG_ID + FS + "overview-tree.html", "<dl>" + NL + "</dl>"},
        {BUG_ID + FS + "serialized-form.html", "<dl></dl>"},
        {BUG_ID + FS + "serialized-form.html", "<dl>" + NL + "</dl>"}};
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
        TestHtmlDefinitionListTag tester = new TestHtmlDefinitionListTag();
        tester.exactNewlineMatch = false;
        run(tester, ARGS1, TEST_ALL, NEGATED_TEST);
        run(tester, ARGS1, TEST_CMNT_DEPR, NEGATED_TEST);
        run(tester, ARGS2, TEST_ALL, NEGATED_TEST);
        run(tester, ARGS2, NO_TEST, TEST_CMNT_DEPR);
        run(tester, ARGS3, TEST_ALL, NEGATED_TEST);
        run(tester, ARGS3, TEST_NODEPR, TEST_NOCMNT_NODEPR);
        run(tester, ARGS4, TEST_ALL, NEGATED_TEST);
        run(tester, ARGS4, TEST_NOCMNT_NODEPR, TEST_CMNT_DEPR);
        tester.printSummary();
    }
    public String getBugId() {
        return BUG_ID;
    }
    public String getBugName() {
        return getClass().getName();
    }
}
