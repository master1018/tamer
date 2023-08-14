@TestTargetClass(ConsoleHandler.class) 
public class ConsoleHandlerTest extends TestCase {
    private final static String INVALID_LEVEL = "impossible_level";
    private final PrintStream err = System.err;
    private OutputStream errSubstituteStream = null;
    private static String className = ConsoleHandlerTest.class.getName();
    protected void setUp() throws Exception {
        super.setUp();
        errSubstituteStream = new MockOutputStream();
        System.setErr(new PrintStream(errSubstituteStream));
        LogManager.getLogManager().reset();
    }
    protected void tearDown() throws Exception {
        super.tearDown();
        LogManager.getLogManager().reset();
        CallVerificationStack.getInstance().clear();
        System.setErr(err);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies the constructor with no relevant log manager properties are set.",
        method = "ConsoleHandler",
        args = {}
    )
    public void testConstructor_NoProperties() {
        assertNull(LogManager.getLogManager().getProperty(
                "java.util.logging.ConsoleHandler.level"));
        assertNull(LogManager.getLogManager().getProperty(
                "java.util.logging.ConsoleHandler.filter"));
        assertNull(LogManager.getLogManager().getProperty(
                "java.util.logging.ConsoleHandler.formatter"));
        assertNull(LogManager.getLogManager().getProperty(
                "java.util.logging.ConsoleHandler.encoding"));
        ConsoleHandler h = new ConsoleHandler();
        assertSame(h.getLevel(), Level.INFO);
        assertTrue(h.getFormatter() instanceof SimpleFormatter);
        assertNull(h.getFilter());
        assertSame(h.getEncoding(), null);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies the constructor with insufficient privilege.",
        method = "ConsoleHandler",
        args = {}
    )
    public void testConstructor_InsufficientPrivilege() {
        assertNull(LogManager.getLogManager().getProperty(
                "java.util.logging.ConsoleHandler.level"));
        assertNull(LogManager.getLogManager().getProperty(
                "java.util.logging.ConsoleHandler.filter"));
        assertNull(LogManager.getLogManager().getProperty(
                "java.util.logging.ConsoleHandler.formatter"));
        assertNull(LogManager.getLogManager().getProperty(
                "java.util.logging.ConsoleHandler.encoding"));
        SecurityManager oldMan = System.getSecurityManager();
        System.setSecurityManager(new MockSecurityManager());
        try {
            ConsoleHandler h = new ConsoleHandler();
            assertSame(h.getLevel(), Level.INFO);
            assertTrue(h.getFormatter() instanceof SimpleFormatter);
            assertNull(h.getFilter());
            assertSame(h.getEncoding(), null);
        } finally {
            System.setSecurityManager(oldMan);
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies the constructor with valid relevant log manager properties are set.",
        method = "ConsoleHandler",
        args = {}
    )
    public void testConstructor_ValidProperties() throws Exception {
        Properties p = new Properties();
        p.put("java.util.logging.ConsoleHandler.level", "FINE");
        p.put("java.util.logging.ConsoleHandler.filter", className
                + "$MockFilter");
        p.put("java.util.logging.ConsoleHandler.formatter", className
                + "$MockFormatter");
        p.put("java.util.logging.ConsoleHandler.encoding", "iso-8859-1");
        LogManager.getLogManager().readConfiguration(
                EnvironmentHelper.PropertiesToInputStream(p));
        assertEquals(LogManager.getLogManager().getProperty(
                "java.util.logging.ConsoleHandler.level"), "FINE");
        assertEquals(LogManager.getLogManager().getProperty(
                "java.util.logging.ConsoleHandler.encoding"), "iso-8859-1");
        ConsoleHandler h = new ConsoleHandler();
        assertSame(h.getLevel(), Level.parse("FINE"));
        assertTrue(h.getFormatter() instanceof MockFormatter);
        assertTrue(h.getFilter() instanceof MockFilter);
        assertEquals(h.getEncoding(), "iso-8859-1");
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies the constructor with invalid relevant log manager properties are set.",
        method = "ConsoleHandler",
        args = {}
    )
    public void testConstructor_InvalidProperties() throws Exception {
        Properties p = new Properties();
        p.put("java.util.logging.ConsoleHandler.level", INVALID_LEVEL);
        p.put("java.util.logging.ConsoleHandler.filter", className);
        p.put("java.util.logging.ConsoleHandler.formatter", className);
        p.put("java.util.logging.ConsoleHandler.encoding", "XXXX");
        LogManager.getLogManager().readConfiguration(
                EnvironmentHelper.PropertiesToInputStream(p));
        assertEquals(LogManager.getLogManager().getProperty(
                "java.util.logging.ConsoleHandler.level"), INVALID_LEVEL);
        assertEquals(LogManager.getLogManager().getProperty(
                "java.util.logging.ConsoleHandler.encoding"), "XXXX");
        ConsoleHandler h = new ConsoleHandler();
        assertSame(h.getLevel(), Level.INFO);
        assertTrue(h.getFormatter() instanceof SimpleFormatter);
        assertNull(h.getFilter());
        assertNull(h.getEncoding());
        h.publish(new LogRecord(Level.SEVERE, "test"));
        assertNull(h.getEncoding());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies close() when having sufficient privilege, and a record has been written to the output stream.",
        method = "close",
        args = {}
    )
    public void testClose_SufficientPrivilege_NormalClose() throws Exception {
        Properties p = new Properties();
        p.put("java.util.logging.ConsoleHandler.formatter", className
                + "$MockFormatter");
        LogManager.getLogManager().readConfiguration(
                EnvironmentHelper.PropertiesToInputStream(p));
        ConsoleHandler h = new ConsoleHandler();
        h.publish(new LogRecord(Level.SEVERE,
                "testClose_SufficientPrivilege_NormalClose msg"));
        h.close();
        assertEquals("flush", CallVerificationStack.getInstance()
                .getCurrentSourceMethod());
        assertNull(CallVerificationStack.getInstance().pop());
        h.close();
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies close() when having sufficient privilege, and an output stream that always throws exceptions",
        method = "close",
        args = {}
    )
    public void testClose_SufficientPrivilege_Exception() throws Exception {
        Properties p = new Properties();
        p.put("java.util.logging.ConsoleHandler.formatter", className
                + "$MockFormatter");
        LogManager.getLogManager().readConfiguration(
                EnvironmentHelper.PropertiesToInputStream(p));
        ConsoleHandler h = new ConsoleHandler();
        h.publish(new LogRecord(Level.SEVERE,
                "testClose_SufficientPrivilege_Exception msg"));
        h.flush();
        h.close();
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Checks close() when having sufficient privilege, and no record has been written to the output stream",
        method = "close",
        args = {}
    )
    public void testClose_SufficientPrivilege_DirectClose() throws Exception {
        Properties p = new Properties();
        p.put("java.util.logging.ConsoleHandler.formatter", className
                + "$MockFormatter");
        LogManager.getLogManager().readConfiguration(
                EnvironmentHelper.PropertiesToInputStream(p));
        ConsoleHandler h = new ConsoleHandler();
        h.close();
        assertEquals("flush", CallVerificationStack.getInstance()
                .getCurrentSourceMethod());
        assertNull(CallVerificationStack.getInstance().pop());
        assertTrue(CallVerificationStack.getInstance().empty());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies close() when insufficient privilege is set up.",
        method = "close",
        args = {}
    )
    public void testClose_InsufficientPrivilege() throws Exception {
        Properties p = new Properties();
        p.put("java.util.logging.ConsoleHandler.formatter", className
                + "$MockFormatter");
        LogManager.getLogManager().readConfiguration(
                EnvironmentHelper.PropertiesToInputStream(p));
        ConsoleHandler h = new ConsoleHandler();
        SecurityManager oldMan = System.getSecurityManager();
        System.setSecurityManager(new MockSecurityManager());
        try {
            h.close();
        } finally {
            System.setSecurityManager(oldMan);
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies publish(), use no filter, having output stream, normal log record.",
        method = "publish",
        args = {java.util.logging.LogRecord.class}
    )
    public void testPublish_NoFilter() throws Exception {
        Properties p = new Properties();
        p.put("java.util.logging.ConsoleHandler.formatter", className
                + "$MockFormatter");
        LogManager.getLogManager().readConfiguration(
                EnvironmentHelper.PropertiesToInputStream(p));
        ConsoleHandler h = new ConsoleHandler();
        LogRecord r = new LogRecord(Level.INFO, "testPublish_NoFilter");
        h.setLevel(Level.INFO);
        h.publish(r);
        h.flush();
        assertEquals("MockFormatter_Head" + "testPublish_NoFilter",
                this.errSubstituteStream.toString());
        h.setLevel(Level.WARNING);
        h.publish(r);
        h.flush();
        assertEquals("MockFormatter_Head" + "testPublish_NoFilter",
                this.errSubstituteStream.toString());
        h.setLevel(Level.CONFIG);
        h.publish(r);
        h.flush();
        assertEquals("MockFormatter_Head" + "testPublish_NoFilter"
                + "testPublish_NoFilter", this.errSubstituteStream.toString());
        r.setLevel(Level.OFF);
        h.setLevel(Level.OFF);
        h.publish(r);
        h.flush();
        assertEquals("MockFormatter_Head" + "testPublish_NoFilter"
                + "testPublish_NoFilter", this.errSubstituteStream.toString());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies publish(), after system err is reset.",
        method = "publish",
        args = {java.util.logging.LogRecord.class}
    )
    public void testPublish_AfterResetSystemErr() throws Exception {
        Properties p = new Properties();
        p.put("java.util.logging.ConsoleHandler.formatter", className
                + "$MockFormatter");
        LogManager.getLogManager().readConfiguration(
                EnvironmentHelper.PropertiesToInputStream(p));
        ConsoleHandler h = new ConsoleHandler();
        h.setFilter(new MockFilter());
        System.setErr(new PrintStream(new ByteArrayOutputStream()));
        LogRecord r = new LogRecord(Level.INFO, "testPublish_WithFilter");
        h.setLevel(Level.INFO);
        h.publish(r);
        assertNull(CallVerificationStack.getInstance().pop());
        assertSame(r, CallVerificationStack.getInstance().pop());
        assertEquals("", this.errSubstituteStream.toString());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies  publish(), use a filter, having output stream, normal log record.",
        method = "publish",
        args = {java.util.logging.LogRecord.class}
    )
    public void testPublish_WithFilter() throws Exception {
        Properties p = new Properties();
        p.put("java.util.logging.ConsoleHandler.formatter", className
                + "$MockFormatter");
        LogManager.getLogManager().readConfiguration(
                EnvironmentHelper.PropertiesToInputStream(p));
        ConsoleHandler h = new ConsoleHandler();
        h.setFilter(new MockFilter());
        LogRecord r = new LogRecord(Level.INFO, "testPublish_WithFilter");
        h.setLevel(Level.INFO);
        h.publish(r);
        assertNull(CallVerificationStack.getInstance().pop());
        assertSame(r, CallVerificationStack.getInstance().pop());
        assertEquals("", this.errSubstituteStream.toString());
        h.setLevel(Level.WARNING);
        h.publish(r);
        assertNull(CallVerificationStack.getInstance().pop());
        assertTrue(CallVerificationStack.getInstance().empty());
        assertEquals("", this.errSubstituteStream.toString());
        h.setLevel(Level.CONFIG);
        h.publish(r);
        assertNull(CallVerificationStack.getInstance().pop());
        assertSame(r, CallVerificationStack.getInstance().pop());
        assertEquals("", this.errSubstituteStream.toString());
        r.setLevel(Level.OFF);
        h.setLevel(Level.OFF);
        h.publish(r);
        assertNull(CallVerificationStack.getInstance().pop());
        assertEquals("", this.errSubstituteStream.toString());
        assertTrue(CallVerificationStack.getInstance().empty());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies publish(), null log record, having output stream, spec said rather than throw exception, handler should call errormanager to handle exception case, so NullPointerException shouldn't be thrown.",
        method = "publish",
        args = {java.util.logging.LogRecord.class}
    )
    public void testPublish_Null() throws Exception {
        Properties p = new Properties();
        p.put("java.util.logging.ConsoleHandler.formatter", className
                + "$MockFormatter");
        LogManager.getLogManager().readConfiguration(
                EnvironmentHelper.PropertiesToInputStream(p));
        ConsoleHandler h = new ConsoleHandler();
        h.publish(null);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies publish(), a log record with empty msg, having output stream.",
        method = "publish",
        args = {java.util.logging.LogRecord.class}
    )
    public void testPublish_EmptyMsg() throws Exception {
        Properties p = new Properties();
        p.put("java.util.logging.ConsoleHandler.formatter", className
                + "$MockFormatter");
        LogManager.getLogManager().readConfiguration(
                EnvironmentHelper.PropertiesToInputStream(p));
        ConsoleHandler h = new ConsoleHandler();
        LogRecord r = new LogRecord(Level.INFO, "");
        h.publish(r);
        h.flush();
        assertEquals("MockFormatter_Head", this.errSubstituteStream.toString());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies publish(), a log record with null msg, having output stream.",
        method = "publish",
        args = {java.util.logging.LogRecord.class}
    )
    public void testPublish_NullMsg() throws Exception {
        Properties p = new Properties();
        p.put("java.util.logging.ConsoleHandler.formatter", className
                + "$MockFormatter");
        LogManager.getLogManager().readConfiguration(
                EnvironmentHelper.PropertiesToInputStream(p));
        ConsoleHandler h = new ConsoleHandler();
        LogRecord r = new LogRecord(Level.INFO, null);
        h.publish(r);
        h.flush();
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies publish method after close.",
        method = "publish",
        args = {java.util.logging.LogRecord.class}
    )
    public void testPublish_AfterClose() throws Exception {
        PrintStream backup = System.err;
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            System.setErr(new PrintStream(bos));
            Properties p = new Properties();
            p.put("java.util.logging.ConsoleHandler.level", "FINE");
            p.put("java.util.logging.ConsoleHandler.formatter", className
                    + "$MockFormatter");
            LogManager.getLogManager().readConfiguration(
                    EnvironmentHelper.PropertiesToInputStream(p));
            ConsoleHandler h = new ConsoleHandler();
            assertSame(h.getLevel(), Level.FINE);
            LogRecord r1 = new LogRecord(Level.INFO, "testPublish_Record1");
            LogRecord r2 = new LogRecord(Level.INFO, "testPublish_Record2");
            assertTrue(h.isLoggable(r1));
            h.publish(r1);
            assertTrue(bos.toString().indexOf("testPublish_Record1") >= 0);
            h.close();
            assertTrue(h.isLoggable(r2));
            h.publish(r2);
            assertTrue(bos.toString().indexOf("testPublish_Record2") >= 0);
            h.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            System.setErr(backup);
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies setOutputStream() under normal condition.",
        method = "setOutputStream",
        args = {java.io.OutputStream.class}
    )
    public void testSetOutputStream_Normal() {
        MockStreamHandler h = new MockStreamHandler();
        h.setFormatter(new MockFormatter());
        LogRecord r = new LogRecord(Level.INFO, "testSetOutputStream_Normal");
        h.publish(r);
        assertNull(CallVerificationStack.getInstance().pop());
        assertSame(r, CallVerificationStack.getInstance().pop());
        assertTrue(CallVerificationStack.getInstance().empty());
        assertEquals("MockFormatter_Head" + "testSetOutputStream_Normal",
                this.errSubstituteStream.toString());
        ByteArrayOutputStream aos2 = new ByteArrayOutputStream();
        h.setOutputStream(aos2);
    }
    public static class MockFilter implements Filter {
        public boolean isLoggable(LogRecord record) {
            CallVerificationStack.getInstance().push(record);
            return false;
        }
    }
    public static class MockFormatter extends Formatter {
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
    public static class MockStreamHandler extends ConsoleHandler {
        public MockStreamHandler() {
            super();
        }
        public void setOutputStream(OutputStream out) {
            super.setOutputStream(out);
        }
        public boolean isLoggable(LogRecord r) {
            CallVerificationStack.getInstance().push(r);
            return super.isLoggable(r);
        }
    }
}
