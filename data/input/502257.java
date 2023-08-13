@TestTargetClass(SimpleFormatter.class)
public class SimpleFormatterTest extends TestCase {
    SimpleFormatter sf;
    LogRecord lr;
    private static String MSG = "test msg. pls. ignore it\nadaadasfdasfd\nadfafdadfsa";
    protected void setUp() throws Exception {
        super.setUp();
        sf = new SimpleFormatter();
        lr = new LogRecord(Level.FINE, MSG);
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "SimpleFormatter",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "getHead",
            args = {java.util.logging.Handler.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "getTail",
            args = {java.util.logging.Handler.class}
        )
    })
    public void testSimpleFormatter() {
        assertEquals("Head for this SimpleFormatter should be empty", "", sf
                .getHead(null));
        assertEquals("Tail for this SimpleFormatter should be empty", "", sf
                .getTail(null));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "format",
        args = {java.util.logging.LogRecord.class}
    )
    public void testFormatNull() {
        try {
            sf.format(null);
            fail("should throw nullpointer exception");
        } catch (NullPointerException e) {
        }
        sf.format(new LogRecord(Level.SEVERE, null));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "format",
        args = {java.util.logging.LogRecord.class}
    )
    public void testLocalizedFormat() {
        ResourceBundle rb = ResourceBundle
                .getBundle("bundles/java/util/logging/res");
        lr.setResourceBundle(rb);
        lr.setMessage("msg");
        String localeMsg = rb.getString("msg");
        String str = sf.format(lr);
        assertTrue(str.indexOf(localeMsg) > 0);
        lr.setResourceBundle(null);
        lr.setResourceBundleName("bundles/java/util/logging/res");
        lr.setMessage("msg");
        str = sf.format(lr);
        localeMsg = rb.getString("msg");
        assertTrue(str.indexOf(localeMsg) < 0);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "format",
        args = {java.util.logging.LogRecord.class}
    )
    public void testFormat() {
        String str = sf.format(lr);
        Throwable t;
        lr.setMessage(MSG + " {0,number}");
        lr.setLoggerName("logger");
        lr.setResourceBundleName("rb name");
        lr.setSourceClassName("class");
        lr.setSourceMethodName("method");
        lr.setParameters(new Object[] { new Integer(100), new Object() });
        lr.setThreadID(1000);
        lr.setThrown(t = new Exception("exception") {
            private static final long serialVersionUID = 1L;
            public String getLocalizedMessage() {
                return "locale";
            }
        });
        lr.setSequenceNumber(12321312);
        lr.setMillis(0);
        str = sf.format(lr);
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(12321312);
        assertTrue(str.indexOf(String.valueOf(cal.get(Calendar.YEAR))) >= 0);
        assertTrue(str.indexOf("class") > 0);
        assertTrue(str.indexOf("method") > 0);
        assertTrue(str.indexOf("100") > 0);
        assertTrue(str.indexOf(t.toString()) > 0);
        assertTrue(str.indexOf(Level.FINE.getLocalizedName()) > 0);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "getHead",
        args = {java.util.logging.Handler.class}
    )
    public void testGetHead() {
        assertEquals("", sf.getHead(null));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "getTail",
        args = {java.util.logging.Handler.class}
    )
    public void testGetTail() {
        assertEquals("", sf.getTail(null));
    }
}
