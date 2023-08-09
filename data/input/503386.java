@TestTargetClass(StreamHandler.class)
public class StreamHandlerTest extends TestCase {
    private final static String INVALID_LEVEL = "impossible_level";
    private final PrintStream err = System.err;
    private OutputStream errSubstituteStream = null;     
    private static String className = StreamHandlerTest.class.getName();
    private static CharsetEncoder encoder;
    static {
        encoder = Charset.forName("iso-8859-1").newEncoder();
        encoder.onMalformedInput(CodingErrorAction.REPLACE);
        encoder.onUnmappableCharacter(CodingErrorAction.REPLACE);
    }
    protected void setUp() throws Exception {
        super.setUp();
        errSubstituteStream = new NullOutputStream();
        System.setErr(new PrintStream(errSubstituteStream));          
    }
    protected void tearDown() throws Exception {
        LogManager.getLogManager().reset();
        CallVerificationStack.getInstance().clear();
        System.setErr(err);        
        super.tearDown();
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies the constructor with no parameter, and no relevant log manager properties are set.",
        method = "StreamHandler",
        args = {}
    )
    public void testConstructor_NoParameter_NoProperties() {
        assertNull(LogManager.getLogManager().getProperty(
                "java.util.logging.StreamHandler.level"));
        assertNull(LogManager.getLogManager().getProperty(
                "java.util.logging.StreamHandler.filter"));
        assertNull(LogManager.getLogManager().getProperty(
                "java.util.logging.StreamHandler.formatter"));
        assertNull(LogManager.getLogManager().getProperty(
                "java.util.logging.StreamHandler.encoding"));
        StreamHandler h = new StreamHandler();
        assertSame(Level.INFO, h.getLevel());
        assertTrue(h.getFormatter() instanceof SimpleFormatter);
        assertNull(h.getFilter());
        assertNull(h.getEncoding());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies the constructor with insufficient privilege.",
        method = "StreamHandler",
        args = {java.io.OutputStream.class, java.util.logging.Formatter.class}
    )
    public void testConstructor_NoParameter_InsufficientPrivilege() {
        assertNull(LogManager.getLogManager().getProperty(
                "java.util.logging.StreamHandler.level"));
        assertNull(LogManager.getLogManager().getProperty(
                "java.util.logging.StreamHandler.filter"));
        assertNull(LogManager.getLogManager().getProperty(
                "java.util.logging.StreamHandler.formatter"));
        assertNull(LogManager.getLogManager().getProperty(
                "java.util.logging.StreamHandler.encoding"));
        SecurityManager oldMan = System.getSecurityManager();
        System.setSecurityManager(new MockSecurityManager());
        try {
            StreamHandler h = new StreamHandler();
            assertSame(Level.INFO, h.getLevel());
            assertTrue(h.getFormatter() instanceof SimpleFormatter);
            assertNull(h.getFilter());
            assertNull(h.getEncoding());
        } finally {
            System.setSecurityManager(oldMan);
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies the constructor with no parameter, and valid relevant log manager properties are set.",
        method = "StreamHandler",
        args = {}
    )
    public void testConstructor_NoParameter_ValidProperties() throws Exception {
        Properties p = new Properties();
        p.put("java.util.logging.StreamHandler.level", "FINE");
        p.put("java.util.logging.StreamHandler.filter", className
                + "$MockFilter");
        p.put("java.util.logging.StreamHandler.formatter", className
                + "$MockFormatter");
        p.put("java.util.logging.StreamHandler.encoding", "iso-8859-1");
        LogManager.getLogManager().readConfiguration(
                EnvironmentHelper.PropertiesToInputStream(p));
        assertEquals("FINE", LogManager.getLogManager().getProperty(
                "java.util.logging.StreamHandler.level"));
        assertEquals("iso-8859-1", LogManager.getLogManager().getProperty(
                "java.util.logging.StreamHandler.encoding"));
        StreamHandler h = new StreamHandler();
        assertSame(h.getLevel(), Level.parse("FINE"));
        assertTrue(h.getFormatter() instanceof MockFormatter);
        assertTrue(h.getFilter() instanceof MockFilter);
        assertEquals("iso-8859-1", h.getEncoding());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies the constructor with no parameter, and invalid relevant log manager properties are set.",
        method = "StreamHandler",
        args = {}
    )
    public void testConstructor_NoParameter_InvalidProperties()
            throws Exception {
        Properties p = new Properties();
        p.put("java.util.logging.StreamHandler.level", INVALID_LEVEL);
        p.put("java.util.logging.StreamHandler.filter", className + "");
        p.put("java.util.logging.StreamHandler.formatter", className + "");
        p.put("java.util.logging.StreamHandler.encoding", "XXXX");
        LogManager.getLogManager().readConfiguration(
                EnvironmentHelper.PropertiesToInputStream(p));
        assertEquals(INVALID_LEVEL, LogManager.getLogManager().getProperty(
                "java.util.logging.StreamHandler.level"));
        assertEquals("XXXX", LogManager.getLogManager().getProperty(
                "java.util.logging.StreamHandler.encoding"));
        StreamHandler h = new StreamHandler();
        assertSame(Level.INFO, h.getLevel());
        assertTrue(h.getFormatter() instanceof SimpleFormatter);
        assertNull(h.getFilter());
        assertNull(h.getEncoding());
        h.publish(new LogRecord(Level.SEVERE, "test"));
        assertTrue(CallVerificationStack.getInstance().empty());
        assertNull(h.getEncoding());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies the constructor with normal parameter values, and no relevant log manager properties are set.",
        method = "StreamHandler",
        args = {java.io.OutputStream.class, java.util.logging.Formatter.class}
    )
    public void testConstructor_HasParameters_NoProperties() {
        assertNull(LogManager.getLogManager().getProperty(
                "java.util.logging.StreamHandler.level"));
        assertNull(LogManager.getLogManager().getProperty(
                "java.util.logging.StreamHandler.filter"));
        assertNull(LogManager.getLogManager().getProperty(
                "java.util.logging.StreamHandler.formatter"));
        assertNull(LogManager.getLogManager().getProperty(
                "java.util.logging.StreamHandler.encoding"));
        StreamHandler h = new StreamHandler(new ByteArrayOutputStream(),
                new MockFormatter2());
        assertSame(Level.INFO, h.getLevel());
        assertTrue(h.getFormatter() instanceof MockFormatter2);
        assertNull(h.getFilter());
        assertNull(h.getEncoding());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies the constructor with insufficient privilege.",
        method = "StreamHandler",
        args = {java.io.OutputStream.class, java.util.logging.Formatter.class}
    )
    public void testConstructor_HasParameter_InsufficientPrivilege() {
        assertNull(LogManager.getLogManager().getProperty(
                "java.util.logging.StreamHandler.level"));
        assertNull(LogManager.getLogManager().getProperty(
                "java.util.logging.StreamHandler.filter"));
        assertNull(LogManager.getLogManager().getProperty(
                "java.util.logging.StreamHandler.formatter"));
        assertNull(LogManager.getLogManager().getProperty(
                "java.util.logging.StreamHandler.encoding"));
        SecurityManager oldMan = System.getSecurityManager();
        System.setSecurityManager(new MockSecurityManager());
        try {
            StreamHandler h = new StreamHandler(new ByteArrayOutputStream(),
                    new MockFormatter2());
            assertSame(Level.INFO, h.getLevel());
            assertTrue(h.getFormatter() instanceof MockFormatter2);
            assertNull(h.getFilter());
            assertNull(h.getEncoding());
        } finally {
            System.setSecurityManager(oldMan);
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies the constructor with normal parameter values, and valid relevant log manager properties are set.",
        method = "StreamHandler",
        args = {java.io.OutputStream.class, java.util.logging.Formatter.class}
    )
    public void testConstructor_HasParameters_ValidProperties()
            throws Exception {
        Properties p = new Properties();
        p.put("java.util.logging.StreamHandler.level", "FINE");
        p.put("java.util.logging.StreamHandler.filter", className
                + "$MockFilter");
        p.put("java.util.logging.StreamHandler.formatter", className
                + "$MockFormatter");
        p.put("java.util.logging.StreamHandler.encoding", "iso-8859-1");
        LogManager.getLogManager().readConfiguration(
                EnvironmentHelper.PropertiesToInputStream(p));
        assertEquals("FINE", LogManager.getLogManager().getProperty(
                "java.util.logging.StreamHandler.level"));
        assertEquals("iso-8859-1", LogManager.getLogManager().getProperty(
                "java.util.logging.StreamHandler.encoding"));
        StreamHandler h = new StreamHandler(new ByteArrayOutputStream(),
                new MockFormatter2());
        assertSame(h.getLevel(), Level.parse("FINE"));
        assertTrue(h.getFormatter() instanceof MockFormatter2);
        assertTrue(h.getFilter() instanceof MockFilter);
        assertEquals("iso-8859-1", h.getEncoding());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies the constructor with normal parameter, and invalid relevant log manager properties are set.",
        method = "StreamHandler",
        args = {java.io.OutputStream.class, java.util.logging.Formatter.class}
    )
    public void testConstructor_HasParameters_InvalidProperties()
            throws Exception {
        Properties p = new Properties();
        p.put("java.util.logging.StreamHandler.level", INVALID_LEVEL);
        p.put("java.util.logging.StreamHandler.filter", className + "");
        p.put("java.util.logging.StreamHandler.formatter", className + "");
        p.put("java.util.logging.StreamHandler.encoding", "XXXX");
        LogManager.getLogManager().readConfiguration(
                EnvironmentHelper.PropertiesToInputStream(p));
        assertEquals(INVALID_LEVEL, LogManager.getLogManager().getProperty(
                "java.util.logging.StreamHandler.level"));
        assertEquals("XXXX", LogManager.getLogManager().getProperty(
                "java.util.logging.StreamHandler.encoding"));
        StreamHandler h = new StreamHandler(new ByteArrayOutputStream(),
                new MockFormatter2());
        assertSame(Level.INFO, h.getLevel());
        assertTrue(h.getFormatter() instanceof MockFormatter2);
        assertNull(h.getFilter());
        assertNull(h.getEncoding());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies the constructor with null formatter, and invalid relevant log manager properties are set.",
        method = "StreamHandler",
        args = {java.io.OutputStream.class, java.util.logging.Formatter.class}
    )
    public void testConstructor_HasParameters_ValidPropertiesNullStream()
            throws Exception {
        Properties p = new Properties();
        p.put("java.util.logging.StreamHandler.level", "FINE");
        p.put("java.util.logging.StreamHandler.filter", className
                + "$MockFilter");
        p.put("java.util.logging.StreamHandler.formatter", className
                + "$MockFormatter");
        p.put("java.util.logging.StreamHandler.encoding", "iso-8859-1");
        LogManager.getLogManager().readConfiguration(
                EnvironmentHelper.PropertiesToInputStream(p));
        assertEquals("FINE", LogManager.getLogManager().getProperty(
                "java.util.logging.StreamHandler.level"));
        assertEquals("iso-8859-1", LogManager.getLogManager().getProperty(
                "java.util.logging.StreamHandler.encoding"));
        try {
            new StreamHandler(new ByteArrayOutputStream(), null);
            fail("Should throw NullPointerException!");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies the constructor with null output stream, and invalid relevant log manager properties are set.",
        method = "StreamHandler",
        args = {java.io.OutputStream.class, java.util.logging.Formatter.class}
    )
    public void testConstructor_HasParameters_ValidPropertiesNullFormatter()
            throws Exception {
        Properties p = new Properties();
        p.put("java.util.logging.StreamHandler.level", "FINE");
        p.put("java.util.logging.StreamHandler.filter", className
                + "$MockFilter");
        p.put("java.util.logging.StreamHandler.formatter", className
                + "$MockFormatter");
        p.put("java.util.logging.StreamHandler.encoding", "iso-8859-1");
        LogManager.getLogManager().readConfiguration(
                EnvironmentHelper.PropertiesToInputStream(p));
        assertEquals("FINE", LogManager.getLogManager().getProperty(
                "java.util.logging.StreamHandler.level"));
        assertEquals("iso-8859-1", LogManager.getLogManager().getProperty(
                "java.util.logging.StreamHandler.encoding"));
        try {
            new StreamHandler(null, new MockFormatter2());
            fail("Should throw NullPointerException!");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies close() when having sufficient privilege, and a record has been written to the output stream.",
        method = "close",
        args = {}
    )
    public void testClose_SufficientPrivilege_NormalClose() {
        ByteArrayOutputStream aos = new MockOutputStream();
        StreamHandler h = new StreamHandler(aos, new MockFormatter());
        h.publish(new LogRecord(Level.SEVERE,
                "testClose_SufficientPrivilege_NormalClose msg"));
        h.close();
        assertEquals("close", CallVerificationStack.getInstance()
                .getCurrentSourceMethod());
        assertNull(CallVerificationStack.getInstance().pop());
        assertEquals("flush", CallVerificationStack.getInstance()
                .getCurrentSourceMethod());
        CallVerificationStack.getInstance().clear();
        assertTrue(aos.toString().endsWith("MockFormatter_Tail"));
        h.close();
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies close() when having sufficient privilege, and an output stream that always throws exceptions.",
        method = "close",
        args = {}
    )
    public void testClose_SufficientPrivilege_Exception() {
        ByteArrayOutputStream aos = new MockExceptionOutputStream();
        StreamHandler h = new StreamHandler(aos, new MockFormatter());
        h.publish(new LogRecord(Level.SEVERE,
                "testClose_SufficientPrivilege_Exception msg"));
        h.flush();
        h.close();
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies close() method when having sufficient privilege, and no record has been written to the output stream.",
        method = "close",
        args = {}
    )
    public void testClose_SufficientPrivilege_DirectClose() {
        ByteArrayOutputStream aos = new MockOutputStream();
        StreamHandler h = new StreamHandler(aos, new MockFormatter());
        h.close();
        assertEquals("close", CallVerificationStack.getInstance()
                .getCurrentSourceMethod());
        assertNull(CallVerificationStack.getInstance().pop());
        assertEquals("flush", CallVerificationStack.getInstance()
                .getCurrentSourceMethod());
        CallVerificationStack.getInstance().clear();
        assertEquals("MockFormatter_HeadMockFormatter_Tail", aos.toString());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies SecurityException.",
        method = "close",
        args = {}
    )
     public void testClose_InsufficientPrivilege() {          
        StreamHandler h = new StreamHandler(new ByteArrayOutputStream(),
                new MockFormatter());
        SecurityManager oldMan = System.getSecurityManager();
        System.setSecurityManager(new MockSecurityManager());
        try {
            h.close();
            fail("Should throw SecurityException!");
        } catch (SecurityException e) {
        } finally {
            System.setSecurityManager(oldMan);
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies close() method when having no output stream.",
        method = "close",
        args = {}
    )
    public void testClose_NoOutputStream() {
        StreamHandler h = new StreamHandler();
        h.close();
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies flush() method.",
        method = "flush",
        args = {}
    )
    public void testFlush_Normal() {
        ByteArrayOutputStream aos = new MockOutputStream();
        StreamHandler h = new StreamHandler(aos, new MockFormatter());
        h.flush();
        assertEquals("flush", CallVerificationStack.getInstance().getCurrentSourceMethod());
        assertNull(CallVerificationStack.getInstance().pop());
        CallVerificationStack.getInstance().clear();
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies flush() when having no output stream.",
        method = "flush",
        args = {}
    )
    public void testFlush_NoOutputStream() {
        StreamHandler h = new StreamHandler();
        h.flush();
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies isLoggable(), use no filter, having no output stream.",
        method = "isLoggable",
        args = {java.util.logging.LogRecord.class}
    )
    public void testIsLoggable_NoOutputStream() {
        StreamHandler h = new StreamHandler();
        LogRecord r = new LogRecord(Level.INFO, null);
        assertFalse(h.isLoggable(r));
        h.setLevel(Level.WARNING);
        assertFalse(h.isLoggable(r));
        h.setLevel(Level.CONFIG);
        assertFalse(h.isLoggable(r));
        r.setLevel(Level.OFF);
        h.setLevel(Level.OFF);
        assertFalse(h.isLoggable(r));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies isLoggable(), use no filter, having output stream.",
        method = "isLoggable",
        args = {java.util.logging.LogRecord.class}
    )
    public void testIsLoggable_NoFilter() {
        StreamHandler h = new StreamHandler(new ByteArrayOutputStream(),
                new SimpleFormatter());
        LogRecord r = new LogRecord(Level.INFO, null);
        assertTrue(h.isLoggable(r));
        h.setLevel(Level.WARNING);
        assertFalse(h.isLoggable(r));
        h.setLevel(Level.CONFIG);
        assertTrue(h.isLoggable(r));
        r.setLevel(Level.OFF);
        h.setLevel(Level.OFF);
        assertFalse(h.isLoggable(r));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies isLoggable(), use a filter, having output stream.",
        method = "isLoggable",
        args = {java.util.logging.LogRecord.class}
    )
    public void testIsLoggable_WithFilter() {
        StreamHandler h = new StreamHandler(new ByteArrayOutputStream(),
                new SimpleFormatter());
        LogRecord r = new LogRecord(Level.INFO, null);
        h.setFilter(new MockFilter());
        assertFalse(h.isLoggable(r));
        assertSame(r, CallVerificationStack.getInstance().pop());
        h.setLevel(Level.CONFIG);
        assertFalse(h.isLoggable(r));
        assertSame(r, CallVerificationStack.getInstance().pop());
        h.setLevel(Level.WARNING);
        assertFalse(h.isLoggable(r)); 
        assertTrue(CallVerificationStack.getInstance().empty());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies isLoggable(), null log record, having output stream. Handler should call ErrorManager to handle exceptional case.",
        method = "isLoggable",
        args = {java.util.logging.LogRecord.class}
    )
    public void testIsLoggable_Null() {
        StreamHandler h = new StreamHandler(new ByteArrayOutputStream(),
                new SimpleFormatter());
        assertFalse(h.isLoggable(null));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies isLoggable(), null log record, without output stream.",
        method = "isLoggable",
        args = {java.util.logging.LogRecord.class}
    )
    public void testIsLoggable_Null_NoOutputStream() {
        StreamHandler h = new StreamHandler();
        assertFalse(h.isLoggable(null));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies publish(), use no filter, having output stream, normal log record.",
        method = "publish",
        args = {java.util.logging.LogRecord.class}
    )
    public void testPublish_NoOutputStream() {
        StreamHandler h = new StreamHandler(); 
        LogRecord r = new LogRecord(Level.INFO, "testPublish_NoOutputStream");
        h.publish(r);
        h.setLevel(Level.WARNING);
        h.publish(r);
        h.setLevel(Level.CONFIG);
        h.publish(r);
        r.setLevel(Level.OFF);
        h.setLevel(Level.OFF);
        h.publish(r);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies publish(), use no filter, having output stream, normal log record.",
        method = "publish",
        args = {java.util.logging.LogRecord.class}
    )
    public void testPublish_NoFilter() {
        ByteArrayOutputStream aos = new ByteArrayOutputStream();
        StreamHandler h = new StreamHandler(aos, new MockFormatter());
        LogRecord r = new LogRecord(Level.INFO, "testPublish_NoFilter");
        h.setLevel(Level.INFO);
        h.publish(r);
        h.flush();
        assertEquals("MockFormatter_Head" + "testPublish_NoFilter", aos
                .toString());
        h.setLevel(Level.WARNING);
        h.publish(r);
        h.flush();
        assertEquals("MockFormatter_Head" + "testPublish_NoFilter", aos
                .toString());
        h.setLevel(Level.CONFIG);
        h.publish(r);
        h.flush();
        assertEquals("MockFormatter_Head" + "testPublish_NoFilter"
                + "testPublish_NoFilter", aos.toString());
        r.setLevel(Level.OFF);
        h.setLevel(Level.OFF);
        h.publish(r);
        h.flush();
        assertEquals("MockFormatter_Head" + "testPublish_NoFilter"
                + "testPublish_NoFilter", aos.toString());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies publish(), use a filter, having output stream, normal log record.",
        method = "publish",
        args = {java.util.logging.LogRecord.class}
    )
    public void testPublish_WithFilter() {
        ByteArrayOutputStream aos = new ByteArrayOutputStream();
        StreamHandler h = new StreamHandler(aos, new MockFormatter());
        h.setFilter(new MockFilter());
        LogRecord r = new LogRecord(Level.INFO, "testPublish_WithFilter");
        h.setLevel(Level.INFO);
        h.publish(r);
        h.flush();
        assertEquals("", aos.toString());
        assertSame(r, CallVerificationStack.getInstance().pop());
        h.setLevel(Level.WARNING);
        h.publish(r);
        h.flush();
        assertEquals("", aos.toString());
        assertTrue(CallVerificationStack.getInstance().empty());
        h.setLevel(Level.CONFIG);
        h.publish(r);
        h.flush();
        assertEquals("", aos.toString());
        assertSame(r, CallVerificationStack.getInstance().pop());
        r.setLevel(Level.OFF);
        h.setLevel(Level.OFF);
        h.publish(r);
        h.flush();
        assertEquals("", aos.toString());
        assertTrue(CallVerificationStack.getInstance().empty());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies publish(), null log record, handler should call ErrorManager to handle exceptional case.",
        method = "publish",
        args = {java.util.logging.LogRecord.class}
    )
    public void testPublish_Null() {
        StreamHandler h = new StreamHandler(new ByteArrayOutputStream(),
                new SimpleFormatter());
        h.publish(null);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies publish(), null log record, without output stream.",
        method = "publish",
        args = {java.util.logging.LogRecord.class}
    )
    public void testPublish_Null_NoOutputStream() {
        StreamHandler h = new StreamHandler();
        h.publish(null);
        MockFilter filter = new MockFilter();
        h.setLevel(Level.FINER);
        h.setFilter(filter);
        LogRecord record = new LogRecord(Level.FINE, "abc");
        h.publish(record);
        assertTrue(CallVerificationStack.getInstance().empty());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies publish(), a log record with empty msg, having output stream.",
        method = "publish",
        args = {java.util.logging.LogRecord.class}
    )
    public void testPublish_EmptyMsg() {
        ByteArrayOutputStream aos = new ByteArrayOutputStream();
        StreamHandler h = new StreamHandler(aos, new MockFormatter());
        LogRecord r = new LogRecord(Level.INFO, "");
        h.publish(r);
        h.flush();
        assertEquals("MockFormatter_Head", aos.toString());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies publish(), a log record with null msg, having output stream.",
        method = "publish",
        args = {java.util.logging.LogRecord.class}
    )
    public void testPublish_NullMsg() {
        ByteArrayOutputStream aos = new ByteArrayOutputStream();
        StreamHandler h = new StreamHandler(aos, new MockFormatter());
        LogRecord r = new LogRecord(Level.INFO, null);
        h.publish(r);
        h.flush();
        assertEquals("MockFormatter_Head", aos.toString());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies publish(), after close.",
        method = "publish",
        args = {java.util.logging.LogRecord.class}
    )
    public void testPublish_AfterClose() throws Exception {
        Properties p = new Properties();
        p.put("java.util.logging.StreamHandler.level", "FINE");
        LogManager.getLogManager().readConfiguration(
                EnvironmentHelper.PropertiesToInputStream(p));
        ByteArrayOutputStream aos = new ByteArrayOutputStream();
        StreamHandler h = new StreamHandler(aos, new MockFormatter());
        assertSame(h.getLevel(), Level.FINE);
        LogRecord r = new LogRecord(Level.INFO, "testPublish_NoFormatter");
        assertTrue(h.isLoggable(r));
        h.close();
        assertFalse(h.isLoggable(r));
        h.publish(r);
        h.flush();
        assertEquals("MockFormatter_HeadMockFormatter_Tail", aos.toString());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies setEncoding() method with supported encoding.",
        method = "setEncoding",
        args = {java.lang.String.class}
    )
    public void testSetEncoding_Normal() throws Exception {
        ByteArrayOutputStream aos = new ByteArrayOutputStream();
        StreamHandler h = new StreamHandler(aos, new MockFormatter());
        h.setEncoding("iso-8859-1");
        assertEquals("iso-8859-1", h.getEncoding());
        LogRecord r = new LogRecord(Level.INFO, "\u6881\u884D\u8F69");
        h.publish(r);
        h.flush();
        byte[] bytes = encoder.encode(
                CharBuffer.wrap("MockFormatter_Head" + "\u6881\u884D\u8F69"))
                .array();
        assertTrue(Arrays.equals(bytes, aos.toByteArray()));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies setEncoding() method with supported encoding, after a log record has been written.",
        method = "setEncoding",
        args = {java.lang.String.class}
    )
    public void testSetEncoding_AfterPublish() throws Exception {
        ByteArrayOutputStream aos = new ByteArrayOutputStream();
        StreamHandler h = new StreamHandler(aos, new MockFormatter());
        h.setEncoding("iso-8859-1");
        assertEquals("iso-8859-1", h.getEncoding());
        LogRecord r = new LogRecord(Level.INFO, "\u6881\u884D\u8F69");
        h.publish(r);
        h.flush();
        assertTrue(Arrays.equals(aos.toByteArray(), encoder.encode(
                CharBuffer.wrap("MockFormatter_Head" + "\u6881\u884D\u8F69"))
                .array()));
        h.setEncoding("iso8859-1");
        assertEquals("iso8859-1", h.getEncoding());
        r = new LogRecord(Level.INFO, "\u6881\u884D\u8F69");
        h.publish(r);
        h.flush();
        assertFalse(Arrays.equals(aos.toByteArray(), encoder.encode(
                CharBuffer.wrap("MockFormatter_Head" + "\u6881\u884D\u8F69"
                        + "testSetEncoding_Normal2")).array()));
        byte[] b0 = aos.toByteArray();
        byte[] b1 = encoder.encode(
                CharBuffer.wrap("MockFormatter_Head" + "\u6881\u884D\u8F69"))
                .array();
        byte[] b2 = encoder.encode(CharBuffer.wrap("\u6881\u884D\u8F69"))
                .array();
        byte[] b3 = new byte[b1.length + b2.length];
        System.arraycopy(b1, 0, b3, 0, b1.length);
        System.arraycopy(b2, 0, b3, b1.length, b2.length);
        assertTrue(Arrays.equals(b0, b3));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies setEncoding() methods with null.",
        method = "setEncoding",
        args = {java.lang.String.class}
    )
    public void testSetEncoding_Null() throws Exception {
        StreamHandler h = new StreamHandler();
        h.setEncoding(null);
        assertNull(h.getEncoding());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies setEncoding() methods with unsupported encoding.",
        method = "setEncoding",
        args = {java.lang.String.class}
    )
    public void testSetEncoding_Unsupported() {
        StreamHandler h = new StreamHandler();
        try {
            h.setEncoding("impossible");
            fail("Should throw UnsupportedEncodingException!");
        } catch (UnsupportedEncodingException e) {
        }
        assertNull(h.getEncoding());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies setEncoding() method with insufficient privilege.",
        method = "setEncoding",
        args = {java.lang.String.class}
    )
    public void testSetEncoding_InsufficientPrivilege() throws Exception {
        StreamHandler h = new StreamHandler();
        SecurityManager oldMan = System.getSecurityManager();
        System.setSecurityManager(new MockSecurityManager());
        try {
            h.setEncoding("iso-8859-1");
            fail("Should throw SecurityException!");
        } catch (SecurityException e) {
        } finally {
            System.setSecurityManager(oldMan);
        }
        assertNull(h.getEncoding());
        System.setSecurityManager(new MockSecurityManager());
        try {
            h.setEncoding("impossible");
            fail("Should throw SecurityException!");
        } catch (SecurityException e) {
        } finally {
            System.setSecurityManager(oldMan);
        }
        assertNull(h.getEncoding());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies that setEncoding() method will flush a stream before setting.",
        method = "setEncoding",
        args = {java.lang.String.class}
    )
    public void testSetEncoding_FlushBeforeSetting() throws Exception {
        ByteArrayOutputStream aos = new ByteArrayOutputStream();
        StreamHandler h = new StreamHandler(aos, new MockFormatter());
        LogRecord r = new LogRecord(Level.INFO, "abcd");
        h.publish(r);
        assertFalse(aos.toString().indexOf("abcd") > 0);
        h.setEncoding("iso-8859-1");
        assertTrue(aos.toString().indexOf("abcd") > 0);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies setOutputStream() method with null.",
        method = "setOutputStream",
        args = {java.io.OutputStream.class}
    )
    public void testSetOutputStream_null() {
        MockStreamHandler h = new MockStreamHandler(
                new ByteArrayOutputStream(), new SimpleFormatter());
        try {
            h.setOutputStream(null);
            fail("Should throw NullPointerException!");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies setOutputStream() method under normal condition.",
        method = "setOutputStream",
        args = {java.io.OutputStream.class}
    )
    public void testSetOutputStream_Normal() {
        ByteArrayOutputStream aos = new ByteArrayOutputStream();
        MockStreamHandler h = new MockStreamHandler(aos, new MockFormatter());
        LogRecord r = new LogRecord(Level.INFO, "testSetOutputStream_Normal");
        h.publish(r);
        assertSame(r, CallVerificationStack.getInstance().pop());
        assertTrue(CallVerificationStack.getInstance().empty());
        h.flush();
        assertEquals("MockFormatter_Head" + "testSetOutputStream_Normal", aos
                .toString());
        ByteArrayOutputStream aos2 = new ByteArrayOutputStream();
        h.setOutputStream(aos2);
        assertEquals("MockFormatter_Head" + "testSetOutputStream_Normal"
                + "MockFormatter_Tail", aos.toString());
        r = new LogRecord(Level.INFO, "testSetOutputStream_Normal2");
        h.publish(r);
        assertSame(r, CallVerificationStack.getInstance().pop());
        assertTrue(CallVerificationStack.getInstance().empty());
        h.flush();
        assertEquals("MockFormatter_Head" + "testSetOutputStream_Normal2", aos2
                .toString());
        assertEquals("MockFormatter_Head" + "testSetOutputStream_Normal"
                + "MockFormatter_Tail", aos.toString());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies setOutputStream() method after close.",
        method = "setOutputStream",
        args = {java.io.OutputStream.class}
    )
    public void testSetOutputStream_AfterClose() {
        ByteArrayOutputStream aos = new ByteArrayOutputStream();
        MockStreamHandler h = new MockStreamHandler(aos, new MockFormatter());
        LogRecord r = new LogRecord(Level.INFO, "testSetOutputStream_Normal");
        h.publish(r);
        assertSame(r, CallVerificationStack.getInstance().pop());
        assertTrue(CallVerificationStack.getInstance().empty());
        h.flush();
        assertEquals("MockFormatter_Head" + "testSetOutputStream_Normal", aos
                .toString());
        h.close();
        ByteArrayOutputStream aos2 = new ByteArrayOutputStream();
        h.setOutputStream(aos2);
        assertEquals("MockFormatter_Head" + "testSetOutputStream_Normal"
                + "MockFormatter_Tail", aos.toString());
        r = new LogRecord(Level.INFO, "testSetOutputStream_Normal2");
        h.publish(r);
        assertSame(r, CallVerificationStack.getInstance().pop());
        assertTrue(CallVerificationStack.getInstance().empty());
        h.flush();
        assertEquals("MockFormatter_Head" + "testSetOutputStream_Normal2", aos2
                .toString());
        assertEquals("MockFormatter_Head" + "testSetOutputStream_Normal"
                + "MockFormatter_Tail", aos.toString());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies setOutputStream() method when having insufficient privilege.",
        method = "setOutputStream",
        args = {java.io.OutputStream.class}
    )
    public void testSetOutputStream_InsufficientPrivilege() {
        MockStreamHandler h = new MockStreamHandler();
        SecurityManager oldMan = System.getSecurityManager();
        System.setSecurityManager(new MockSecurityManager());
        try {
            h.setOutputStream(new ByteArrayOutputStream());
            fail("Should throw SecurityException!");
        } catch (SecurityException e) {
        } finally {
            System.setSecurityManager(oldMan);
        }
        h = new MockStreamHandler();
        System.setSecurityManager(new MockSecurityManager());
        try {
            h.setOutputStream(null);
            fail("Should throw NullPointerException!");
        } catch (NullPointerException e) {
        } finally {
            System.setSecurityManager(oldMan);
        }
    }
    public static class MockStreamHandler extends StreamHandler {
        public MockStreamHandler() {
            super();
        }
        public MockStreamHandler(OutputStream out, Formatter formatter) {
            super(out, formatter);
        }
        public void setOutputStream(OutputStream out) {
            super.setOutputStream(out);
        }
        public boolean isLoggable(LogRecord r) {
            CallVerificationStack.getInstance().push(r);
            return super.isLoggable(r);
        }
    }
    public static class MockFilter implements Filter {
        public boolean isLoggable(LogRecord record) {
            CallVerificationStack.getInstance().push(record);
            return false;
        }
    }
    public static class MockFormatter extends java.util.logging.Formatter {
        public String format(LogRecord r) {
            return super.formatMessage(r);
        }
        public String getHead(Handler h) {
            return "MockFormatter_Head";
        }
        public String getTail(Handler h) {
            return "MockFormatter_Tail";
        }
    }
    public static class MockFormatter2 extends java.util.logging.Formatter {
        public String format(LogRecord r) {
            return r.getMessage();
        }
    }
    public static class MockOutputStream extends ByteArrayOutputStream {
        public void close() throws IOException {
            CallVerificationStack.getInstance().push(null);
            super.close();
        }
        public void flush() throws IOException {
            CallVerificationStack.getInstance().push(null);
            super.flush();
        }
        public void write(int oneByte) {
            super.write(oneByte);
        }
    }
    public static class MockExceptionOutputStream extends ByteArrayOutputStream {
        public void close() throws IOException {
            throw new IOException();
        }
        public void flush() throws IOException {
            throw new IOException();
        }
        public synchronized void write(byte[] buffer, int offset, int count) {
            throw new NullPointerException();
        }
        public synchronized void write(int oneByte) {
            throw new NullPointerException();
        }
    }
    public static class MockSecurityManager extends SecurityManager {
        public MockSecurityManager() {
        }
        public void checkPermission(Permission perm) {
            if (perm instanceof LoggingPermission) {
                throw new SecurityException();
            }
        }
        public void checkPermission(Permission perm, Object context) {
            if (perm instanceof LoggingPermission) {
                throw new SecurityException();
            }
        }
    }
}
