@TestTargetClass(XMLFormatter.class)
public class XMLFormatterTest extends TestCase {
    XMLFormatter formatter = null;
    MockHandler handler = null;
    LogRecord lr = null;
    protected void setUp() throws Exception {
        super.setUp();
        formatter = new XMLFormatter();
        handler = new MockHandler();
        lr = new LogRecord(Level.SEVERE, "pattern");
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "XMLFormatter",
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
    public void testXMLFormatter() throws SecurityException,
            UnsupportedEncodingException {
        handler.setEncoding("UTF-8");
        String result = formatter.getHead(handler);
        int headPos = result
                .indexOf("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>");
        int dtdPos = result.indexOf("<!DOCTYPE log SYSTEM \"logger.dtd\">");
        int rootPos = result.indexOf("<log>");
        assertTrue("head string position should be more or equal zero",
                headPos >= 0);
        assertTrue("dtd string position should be more head string position",
                dtdPos > headPos);
        assertTrue("root string position should be more dtd string position",
                rootPos > dtdPos);
        assertTrue("Tail string position should be more zero", formatter
                .getTail(handler).indexOf("/log>") > 0);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "format",
        args = {java.util.logging.LogRecord.class}
    )
    public void testLocalFormat() {
        ResourceBundle rb = ResourceBundle
                .getBundle("bundles/java/util/logging/res");
        lr.setResourceBundle(rb);
        lr.setMessage("pattern");
        String result = formatter.format(lr);
        assertTrue(result.indexOf("<message>" + rb.getString("pattern")
                + "</message>") > 0);
        assertTrue(result.indexOf("<key>pattern</key>") > 0);
        lr.setMessage("msg");
        result = formatter.format(lr);
        assertTrue(result.indexOf("<message>" + rb.getString("msg")
                + "</message>") > 0);
        assertTrue(result.indexOf("<key>msg</key>") > 0);
        lr.setMessage("pattern {0, number}");
        result = formatter.format(lr);
        assertTrue(result.indexOf("<message>pattern {0, number}</message>") > 0);
        assertTrue(result.indexOf("<key>") < 0);
        lr.setMessage("bad key");
        result = formatter.format(lr);
        assertTrue(result.indexOf("<message>bad key</message>") > 0);
        assertTrue(result.indexOf("<key>") < 0);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "format",
        args = {java.util.logging.LogRecord.class}
    )
    public void testFullFormat() {
        lr.setSourceClassName("source class");
        lr.setSourceMethodName("source method");
        lr.setLoggerName("logger name");
        lr.setMillis(0);
        lr.setThrown(new Throwable("message"));
        lr.setParameters(new Object[] { "100", "200" });
        lr.setSequenceNumber(1);
        ResourceBundle rb = ResourceBundle
                .getBundle("bundles/java/util/logging/res");
        lr.setResourceBundle(rb);
        lr.setResourceBundleName("rbname");
        String output = formatter.format(lr);
        assertTrue(output.indexOf("<record>") >= 0);
        assertTrue(output.indexOf("<date>") >= 0);
        assertTrue(output.indexOf("<millis>0</millis>") >= 0);
        assertTrue(output.indexOf("<sequence>") >= 0);
        assertTrue(output.indexOf("<level>SEVERE</level>") >= 0);
        assertTrue(output.indexOf("<thread>") >= 0);
        assertTrue(output.indexOf("<message>" + rb.getString("pattern")
                + "</message>") >= 0);
        assertTrue(output.indexOf("<logger>logger name</logger>") > 0);
        assertTrue(output.indexOf("<class>source class</class>") > 0);
        assertTrue(output.indexOf("<method>source method</method>") > 0);
        assertTrue(output.indexOf("<catalog>rbname</catalog>") > 0);
        assertTrue(output.indexOf("<param>100</param>") > 0);
        assertTrue(output.indexOf("<param>200</param>") > 0);
        assertTrue(output.indexOf("<exception>") > 0);
        assertTrue(output.indexOf("<key>pattern</key>") > 0);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "format",
        args = {java.util.logging.LogRecord.class}
    )
    public void testFormat() {
        String output = formatter.format(lr);
        assertTrue(output.indexOf("<record>") >= 0);
        assertTrue(output.indexOf("<date>") >= 0);
        assertTrue(output.indexOf("<millis>") >= 0);
        assertTrue(output.indexOf("<sequence>") >= 0);
        assertTrue(output.indexOf("<level>SEVERE</level>") >= 0);
        assertTrue(output.indexOf("<thread>") >= 0);
        assertTrue(output.indexOf("<message>pattern</message>") >= 0);
        assertTrue(output.indexOf("<logger>") < 0);
        assertTrue(output.indexOf("<class>") < 0);
        assertTrue(output.indexOf("<method>") < 0);
        assertTrue(output.indexOf("<catalog>") < 0);
        assertTrue(output.indexOf("<param>") < 0);
        assertTrue(output.indexOf("<exception>") < 0);
        assertTrue(output.indexOf("<key>") < 0);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "getHead",
        args = {java.util.logging.Handler.class}
    )
    public void testGetHead() throws SecurityException,
            UnsupportedEncodingException {
        String result = formatter.getHead(handler);
        assertNull(handler.getEncoding());
        handler.setEncoding("ISO-8859-1");
        String head = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\" standalone=\"no\"?>";
        String dtd = "<!DOCTYPE log SYSTEM \"logger.dtd\">";
        String rootELement = "<log>";
        result = formatter.getHead(handler);
        int headPos = result.indexOf(head);
        int dtdPos = result.indexOf(dtd);
        int rootPos = result.indexOf(rootELement);
        assertTrue(headPos >= 0);
        assertTrue(dtdPos > headPos);
        assertTrue(rootPos > dtdPos);
        handler.setEncoding(null);
        result = formatter.getHead(handler);
        assertNull(handler.getEncoding());
        formatter.getHead(null);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "getTail",
        args = {java.util.logging.Handler.class}
    )
    public void testGetTail() {
        assertEquals(
                "Tail string with null handler should be equal expected value",
                "</log>", formatter.getTail(null).trim());
        assertEquals("Tail string should be equal expected value", "</log>",
                formatter.getTail(handler).trim());
        handler.publish(lr);
        assertEquals(
                "Tail string after publish() should be equal expected value",
                "</log>", formatter.getTail(handler).trim());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "format",
            args = {java.util.logging.LogRecord.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "getTail",
            args = {java.util.logging.Handler.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "XMLFormatter",
            args = {}
        )
    })
    @AndroidOnly("This test fails on RI. Output doesn't contain " +
            "<message/>.")
    public void testInvalidParameter() {
        formatter.getTail(null);
        try {
            formatter.format(null);
            fail();
        } catch (NullPointerException e) {
        }
        formatter = new XMLFormatter();
        lr = new LogRecord(Level.SEVERE, null);
        String output = formatter.format(lr);
        assertTrue(output.indexOf("<message/>") != -1);
    }
    public static class MockHandler extends Handler {
        public void close() {
        }
        public void flush() {
        }
        public void publish(LogRecord record) {
        }
    }
}
