@TestTargetClass(SocketHandler.class)
public class SocketHandlerTest extends TestCase {
    private static final LogManager LOG_MANAGER = LogManager.getLogManager();
    private final static String INVALID_LEVEL = "impossible_level";
    private final PrintStream err = System.err;
    private OutputStream errSubstituteStream = null;    
    private static String className = SocketHandlerTest.class.getName();
    private SocketHandler h = null;
    private Properties props;
    protected void setUp() throws Exception {
        super.setUp();
        errSubstituteStream = new NullOutputStream();
        System.setErr(new PrintStream(errSubstituteStream));  
    }
    protected void tearDown() throws Exception {
        props = new Properties();
        props.put("handlers", className + "$MockHandler " + className
                + "$MockHandler");
        props.put("java.util.logging.FileHandler.pattern", "%h/java%u.log");
        props.put("java.util.logging.FileHandler.limit", "50000");
        props.put("java.util.logging.FileHandler.count", "5");
        props.put("java.util.logging.FileHandler.formatter",
                "java.util.logging.XMLFormatter");
        props.put(".level", "FINE");
        props.put("java.util.logging.ConsoleHandler.level", "OFF");
        props.put("java.util.logging.ConsoleHandler.formatter",
                "java.util.logging.SimpleFormatter");
        props.put("foo.handlers", "java.util.logging.ConsoleHandler");
        props.put("foo.level", "WARNING");
        props.put("com.xyz.foo.level", "SEVERE");
        LOG_MANAGER.reset();
        LOG_MANAGER.readConfiguration(EnvironmentHelper
                .PropertiesToInputStream(props));
        CallVerificationStack.getInstance().clear();
        if (null != h) {
            h.close();
            h = null;
        }
        System.setErr(err);
        super.tearDown();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies the constructor with no relevant log manager properties are set.",
            method = "SocketHandler",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies the constructor with no relevant log manager properties are set.",
            method = "SocketHandler",
            args = {java.lang.String.class, int.class}
        )
    })
    public void testConstructor_NoProperties() throws Exception {
        assertNull(LOG_MANAGER.getProperty(
                "java.util.logging.SocketHandler.level"));
        assertNull(LOG_MANAGER.getProperty(
                "java.util.logging.SocketHandler.filter"));
        assertNull(LOG_MANAGER.getProperty(
                "java.util.logging.SocketHandler.formatter"));
        assertNull(LOG_MANAGER.getProperty(
                "java.util.logging.SocketHandler.encoding"));
        assertNull(LOG_MANAGER.getProperty(
                "java.util.logging.SocketHandler.host"));
        assertNull(LOG_MANAGER.getProperty(
                "java.util.logging.SocketHandler.port"));
        try {
            h = new SocketHandler();
            fail("Should throw IllegalArgumentException!");
        } catch (IllegalArgumentException e) {
        }
        try {
            h = new SocketHandler(null, 0);
            fail("Should throw IllegalArgumentException!");
        } catch (IllegalArgumentException e) {
        }
        try {
            h = new SocketHandler("", 0);
            fail("Should throw IllegalArgumentException!");
        } catch (IllegalArgumentException e) {
        }
        try {
            h = new SocketHandler("127.0.0.1", -1);
            fail("Should throw IllegalArgumentException!");
        } catch (IllegalArgumentException e) {
        }
        try {
            h = new SocketHandler("127.0.0.1", Integer.MAX_VALUE);
            fail("Should throw IllegalArgumentException!");
        } catch (IllegalArgumentException e) {
        }
        try {
            h = new SocketHandler("127.0.0.1", 66666);
            fail("Should throw IllegalArgumentException!");
        } catch (IllegalArgumentException e) {
        }
        try {
            h = new SocketHandler("127.0.0.1", 0);
            fail("Should throw IllegalArgumentException!");
        } catch (IllegalArgumentException e) {
        }
        ServerThread thread = new ServerThread();
        thread.start();
        Thread.sleep(2000);
        h = new SocketHandler("127.0.0.1", 6666);
        assertSame(h.getLevel(), Level.ALL);
        assertTrue(h.getFormatter() instanceof XMLFormatter);
        assertNull(h.getFilter());
        assertNull(h.getEncoding());
        h.close();
        thread.getReadString();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies the constructor with no relevant log manager properties are set except host and port.",
            method = "SocketHandler",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies the constructor with no relevant log manager properties are set except host and port.",
            method = "SocketHandler",
            args = {java.lang.String.class, int.class}
        )
    })
    public void testConstructor_NoBasicProperties() throws Exception {
        assertNull(LOG_MANAGER.getProperty(
                "java.util.logging.SocketHandler.level"));
        assertNull(LOG_MANAGER.getProperty(
                "java.util.logging.SocketHandler.filter"));
        assertNull(LOG_MANAGER.getProperty(
                "java.util.logging.SocketHandler.formatter"));
        assertNull(LOG_MANAGER.getProperty(
                "java.util.logging.SocketHandler.encoding"));
        Properties p = new Properties();
        p.put("java.util.logging.SocketHandler.host", "127.0.0.1");
        p.put("java.util.logging.SocketHandler.port", "6666");
        LOG_MANAGER.readConfiguration(
                EnvironmentHelper.PropertiesToInputStream(p));
        ServerThread thread = new ServerThread();
        thread.start();
        Thread.sleep(2000);
        h = new SocketHandler();
        assertSame(h.getLevel(), Level.ALL);
        assertTrue(h.getFormatter() instanceof XMLFormatter);
        assertNull(h.getFilter());
        assertNull(h.getEncoding());
        h.close();
        thread.getReadString();
        try {
            h = new SocketHandler("127.0.sdfcdsfsa%%&&^0.1", 6665);
            fail("Should throw IOException!");
        } catch (IOException e) {
        }
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies SecurityException.",
            method = "SocketHandler",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies SecurityException.",
            method = "SocketHandler",
            args = {java.lang.String.class, int.class}
        )
    })
    public void testConstructor_InsufficientPrivilege() throws Exception {
        SecurityManager oldMan = null;
        Properties p = new Properties();
        p.put("java.util.logging.SocketHandler.level", "FINE");
        p.put("java.util.logging.SocketHandler.filter", className
                + "$MockFilter");
        p.put("java.util.logging.SocketHandler.formatter", className
                + "$MockFormatter");
        p.put("java.util.logging.SocketHandler.encoding", "utf-8");
        p.put("java.util.logging.SocketHandler.host", "127.0.0.1");
        p.put("java.util.logging.SocketHandler.port", "6666");
        LOG_MANAGER.readConfiguration(
                EnvironmentHelper.PropertiesToInputStream(p));
        oldMan = System.getSecurityManager();
        System.setSecurityManager(new MockNoSocketSecurityManager());
        try {
            new SocketHandler();
            fail("Should throw SecurityException!");
        } catch (SecurityException e) {
        } finally {
            System.setSecurityManager(oldMan);
        }
        System.setSecurityManager(new MockNoSocketSecurityManager());
        try {
            new SocketHandler("127.0.0.1", 6666);
            fail("Should throw SecurityException!");
        } catch (SecurityException e) {
        } finally {
            System.setSecurityManager(oldMan);
        }
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies the constructor with valid relevant log manager properties are set.",
            method = "SocketHandler",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies the constructor with valid relevant log manager properties are set.",
            method = "SocketHandler",
            args = {java.lang.String.class, int.class}
        )
    })
    public void testConstructor_ValidProperties() throws Exception {
        Properties p = new Properties();
        p.put("java.util.logging.SocketHandler.level", "FINE");
        p.put("java.util.logging.SocketHandler.filter", className
                + "$MockFilter");
        p.put("java.util.logging.SocketHandler.formatter", className
                + "$MockFormatter");
        p.put("java.util.logging.SocketHandler.encoding", "iso-8859-1");
        p.put("java.util.logging.SocketHandler.host", "127.0.0.1");
        p.put("java.util.logging.SocketHandler.port", "6666");
        LOG_MANAGER.readConfiguration(
                EnvironmentHelper.PropertiesToInputStream(p));
        ServerThread thread = new ServerThread();
        thread.start();
        Thread.sleep(2000);
        h = new SocketHandler();
        assertSame(h.getLevel(), Level.parse("FINE"));
        assertTrue(h.getFormatter() instanceof MockFormatter);
        assertTrue(h.getFilter() instanceof MockFilter);
        assertEquals(h.getEncoding(), "iso-8859-1");
        h.close();
        thread.getReadString();
        thread = new ServerThread();
        thread.start();
        Thread.sleep(2000);
        h = new SocketHandler("127.0.0.1", 6666);
        assertSame(h.getLevel(), Level.parse("FINE"));
        assertTrue(h.getFormatter() instanceof MockFormatter);
        assertTrue(h.getFilter() instanceof MockFilter);
        assertEquals(h.getEncoding(), "iso-8859-1");
        h.close();
        thread.getReadString();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies the constructor with invalid relevant log manager properties are set except host and port.",
            method = "SocketHandler",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies the constructor with invalid relevant log manager properties are set except host and port.",
            method = "SocketHandler",
            args = {java.lang.String.class, int.class}
        )
    })
    public void testConstructor_InvalidBasicProperties() throws Exception {
        Properties p = new Properties();
        p.put("java.util.logging.SocketHandler.level", INVALID_LEVEL);
        p.put("java.util.logging.SocketHandler.filter", className + "");
        p.put("java.util.logging.SocketHandler.formatter", className + "");
        p.put("java.util.logging.SocketHandler.encoding", "XXXX");
        p.put("java.util.logging.SocketHandler.host", "127.0.0.1");
        p.put("java.util.logging.SocketHandler.port", "6666");
        LOG_MANAGER.readConfiguration(
                EnvironmentHelper.PropertiesToInputStream(p));
        ServerThread thread = new ServerThread();
        thread.start();
        Thread.sleep(2000);
        h = new SocketHandler();
        assertSame(h.getLevel(), Level.ALL);
        assertTrue(h.getFormatter() instanceof XMLFormatter);
        assertNull(h.getFilter());
        assertNull(h.getEncoding());
        h.publish(new LogRecord(Level.SEVERE, "test"));
        assertNull(h.getEncoding());
        h.close();
        thread.getReadString();
        thread = new ServerThread();
        thread.start();
        Thread.sleep(2000);
        h = new SocketHandler("127.0.0.1", 6666);
        assertSame(h.getLevel(), Level.ALL);
        assertTrue(h.getFormatter() instanceof XMLFormatter);
        assertNull(h.getFilter());
        assertNull(h.getEncoding());
        h.publish(new LogRecord(Level.SEVERE, "test"));
        assertNull(h.getEncoding());
        h.close();
        thread.getReadString();
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies IllegalArgumentException.",
        method = "SocketHandler",
        args = {}
    )
    public void testConstructor_InvalidPort() throws Exception {
        Properties p = new Properties();
        p.put("java.util.logging.SocketHandler.level", "FINE");
        p.put("java.util.logging.SocketHandler.filter", className
                + "$MockFilter");
        p.put("java.util.logging.SocketHandler.formatter", className
                + "$MockFormatter");
        p.put("java.util.logging.SocketHandler.encoding", "iso-8859-1");
        p.put("java.util.logging.SocketHandler.host", "127.0.0.1");
        p.put("java.util.logging.SocketHandler.port", "6666i");
        LOG_MANAGER.readConfiguration(
                EnvironmentHelper.PropertiesToInputStream(p));
        try {
            h = new SocketHandler();
            fail("Should throw IllegalArgumentException!");
        } catch (IllegalArgumentException e) {
        }
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies that the constructor with valid relevant log manager properties are set, but the port is not open.",
            method = "SocketHandler",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies that the constructor with valid relevant log manager properties are set, but the port is not open.",
            method = "SocketHandler",
            args = {java.lang.String.class, int.class}
        )
    })
    public void testConstructor_NotOpenPort() throws Exception {
        Properties p = new Properties();
        p.put("java.util.logging.SocketHandler.level", "FINE");
        p.put("java.util.logging.SocketHandler.filter", className
                + "$MockFilter");
        p.put("java.util.logging.SocketHandler.formatter", className
                + "$MockFormatter");
        p.put("java.util.logging.SocketHandler.encoding", "iso-8859-1");
        p.put("java.util.logging.SocketHandler.host", "127.0.0.1");
        p.put("java.util.logging.SocketHandler.port", "6665");
        LOG_MANAGER.readConfiguration(
                EnvironmentHelper.PropertiesToInputStream(p));
        try {
            h = new SocketHandler();
            fail("Should throw IOException!");
        } catch (IOException e) {
        }
        try {
            h = new SocketHandler("127.0.0.1", 6665);
            fail("Should throw IOException!");
        } catch (IOException e) {
        }
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies IOException.",
            method = "SocketHandler",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies IOException.",
            method = "SocketHandler",
            args = {java.lang.String.class, int.class}
        )
    })
    public void testConstructor_InvalidHost() throws Exception {
        Properties p = new Properties();
        p.put("java.util.logging.SocketHandler.level", "FINE");
        p.put("java.util.logging.SocketHandler.filter", className
                + "$MockFilter");
        p.put("java.util.logging.SocketHandler.formatter", className
                + "$MockFormatter");
        p.put("java.util.logging.SocketHandler.encoding", "iso-8859-1");
        p.put("java.util.logging.SocketHandler.host", " 34345 #$#%$%$");
        p.put("java.util.logging.SocketHandler.port", "6666");
        LOG_MANAGER.readConfiguration(
                EnvironmentHelper.PropertiesToInputStream(p));
        try {
            h = new SocketHandler();
            fail("Should throw IOException!");
        } catch (IOException e) {
        }
        try {
            h = new SocketHandler(" 34345 #$#%$%$", 6666);
            fail("Should throw IOException!");
        } catch (IOException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies close() when having sufficient privilege, and a record has been written to the output stream.",
        method = "close",
        args = {}
    )
    public void testClose_SufficientPrivilege_NormalClose() throws Exception {
        Properties p = new Properties();
        p.put("java.util.logging.SocketHandler.formatter", className
                + "$MockFormatter");
        p.put("java.util.logging.SocketHandler.host", "127.0.0.1");
        p.put("java.util.logging.SocketHandler.port", "6666");
        LOG_MANAGER.readConfiguration(
                EnvironmentHelper.PropertiesToInputStream(p));
        ServerThread thread = new ServerThread();
        thread.start();
        Thread.sleep(2000);
        h = new SocketHandler();
        h.publish(new LogRecord(Level.SEVERE,
                "testClose_SufficientPrivilege_NormalClose msg"));
        h.close();
        assertEquals("MockFormatter_Head"
                + "testClose_SufficientPrivilege_NormalClose msg"
                + "MockFormatter_Tail", thread.getReadString());
        h.close();
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies close() when having sufficient privilege, and no record has been written to the output stream.",
        method = "close",
        args = {}
    )
    public void testClose_SufficientPrivilege_DirectClose() throws Exception {
        Properties p = new Properties();
        p.put("java.util.logging.SocketHandler.formatter", className
                + "$MockFormatter");
        p.put("java.util.logging.SocketHandler.host", "127.0.0.1");
        p.put("java.util.logging.SocketHandler.port", "6666");
        LOG_MANAGER.readConfiguration(
                EnvironmentHelper.PropertiesToInputStream(p));
        ServerThread thread = new ServerThread();
        thread.start();
        Thread.sleep(2000);
        h = new SocketHandler();
        h.setLevel(Level.INFO);
        h.close();
        assertEquals("MockFormatter_Head" + "MockFormatter_Tail", thread
                .getReadString());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies close() when having insufficient privilege.",
        method = "close",
        args = {}
    )
    public void testClose_InsufficientPrivilege() throws Exception {
        Properties p = new Properties();
        p.put("java.util.logging.SocketHandler.formatter", className
                + "$MockFormatter");
        p.put("java.util.logging.SocketHandler.host", "127.0.0.1");
        p.put("java.util.logging.SocketHandler.port", "6666");
        LOG_MANAGER.readConfiguration(
                EnvironmentHelper.PropertiesToInputStream(p));
        ServerThread thread = new ServerThread();
        thread.start();
        Thread.sleep(2000);
        h = new SocketHandler();
        h.setLevel(Level.INFO);
        SecurityManager oldMan = System.getSecurityManager();
        System.setSecurityManager(new MockSecurityManager());
        try {
            h.close();
            fail("Should throw SecurityException!");
        } catch (SecurityException e) {
        } finally {
            System.setSecurityManager(oldMan);
            h.close();
            thread.getReadString();
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
        p.put("java.util.logging.SocketHandler.formatter", className
                + "$MockFormatter");
        p.put("java.util.logging.SocketHandler.host", "127.0.0.1");
        p.put("java.util.logging.SocketHandler.port", "6666");
        LOG_MANAGER.readConfiguration(
                EnvironmentHelper.PropertiesToInputStream(p));
        ServerThread thread = new ServerThread();
        thread.start();
        Thread.sleep(2000);
        h = new SocketHandler();
        h.setLevel(Level.INFO);
        LogRecord r = new LogRecord(Level.INFO, "testPublish_NoFilter");
        h.setLevel(Level.INFO);
        h.publish(r);
        h.setLevel(Level.WARNING);
        h.publish(r);
        h.setLevel(Level.CONFIG);
        h.publish(r);
        r.setLevel(Level.OFF);
        h.setLevel(Level.OFF);
        h.publish(r);
        h.close();
        assertEquals("MockFormatter_Head" + "testPublish_NoFilter"
                + "testPublish_NoFilter" + "MockFormatter_Tail", thread
                .getReadString());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies publish(), use a filter, having output stream, normal log record.",
        method = "publish",
        args = {java.util.logging.LogRecord.class}
    )
    public void testPublish_WithFilter() throws Exception {
        Properties p = new Properties();
        p.put("java.util.logging.SocketHandler.formatter", className
                + "$MockFormatter");
        p.put("java.util.logging.SocketHandler.host", "127.0.0.1");
        p.put("java.util.logging.SocketHandler.port", "6666");
        LOG_MANAGER.readConfiguration(
                EnvironmentHelper.PropertiesToInputStream(p));
        ServerThread thread = new ServerThread();
        thread.start();
        Thread.sleep(2000);
        h = new SocketHandler();
        h.setLevel(Level.INFO);
        h.setFilter(new MockFilter());
        System.setErr(new PrintStream(new ByteArrayOutputStream()));
        LogRecord r = new LogRecord(Level.INFO, "testPublish_WithFilter");
        h.setLevel(Level.INFO);
        h.publish(r);
        h.close();
        assertEquals("MockFormatter_Head" + "MockFormatter_Tail", thread
                .getReadString());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies publish(), null log record, having output stream.",
        method = "publish",
        args = {java.util.logging.LogRecord.class}
    )
    public void testPublish_Null() throws Exception {
        Properties p = new Properties();
        p.put("java.util.logging.SocketHandler.formatter", className
                + "$MockFormatter");
        p.put("java.util.logging.SocketHandler.host", "127.0.0.1");
        p.put("java.util.logging.SocketHandler.port", "6666");
        LOG_MANAGER.readConfiguration(
                EnvironmentHelper.PropertiesToInputStream(p));
        ServerThread thread = new ServerThread();
        thread.start();
        Thread.sleep(2000);
        h = new SocketHandler();
        h.setLevel(Level.INFO);
        try {
            h.publish(null);
        } finally {
            h.close();
            thread.getReadString();
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies  publish() method, a log record with empty msg, having output stream.",
        method = "publish",
        args = {java.util.logging.LogRecord.class}
    )
    public void testPublish_EmptyMsg() throws Exception {
        Properties p = new Properties();
        p.put("java.util.logging.SocketHandler.formatter", className
                + "$MockFormatter");
        p.put("java.util.logging.SocketHandler.host", "127.0.0.1");
        p.put("java.util.logging.SocketHandler.port", "6666");
        LOG_MANAGER.readConfiguration(
                EnvironmentHelper.PropertiesToInputStream(p));
        ServerThread thread = new ServerThread();
        thread.start();
        Thread.sleep(2000);
        h = new SocketHandler();
        h.setLevel(Level.INFO);
        LogRecord r = new LogRecord(Level.INFO, "");
        h.publish(r);
        h.close();
        assertEquals("MockFormatter_Head" + "MockFormatter_Tail", thread
                .getReadString());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies publish(), a log record with null msg, having output stream.",
        method = "publish",
        args = {java.util.logging.LogRecord.class}
    )
    public void testPublish_NullMsg() throws Exception {
        Properties p = new Properties();
        p.put("java.util.logging.SocketHandler.formatter", className
                + "$MockFormatter");
        p.put("java.util.logging.SocketHandler.host", "127.0.0.1");
        p.put("java.util.logging.SocketHandler.port", "6666");
        LOG_MANAGER.readConfiguration(
                EnvironmentHelper.PropertiesToInputStream(p));
        ServerThread thread = new ServerThread();
        thread.start();
        Thread.sleep(2000);
        h = new SocketHandler();
        h.setLevel(Level.INFO);
        LogRecord r = new LogRecord(Level.INFO, null);
        h.publish(r);
        h.close();
        assertEquals("MockFormatter_Head" + "MockFormatter_Tail", thread
                .getReadString());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies publish() method after close.",
        method = "publish",
        args = {java.util.logging.LogRecord.class}
    )
    public void testPublish_AfterClose() throws Exception {
        Properties p = new Properties();
        p.put("java.util.logging.SocketHandler.formatter", className
                + "$MockFormatter");
        p.put("java.util.logging.SocketHandler.host", "127.0.0.1");
        p.put("java.util.logging.SocketHandler.port", "6666");
        LOG_MANAGER.readConfiguration(
                EnvironmentHelper.PropertiesToInputStream(p));
        ServerThread thread = new ServerThread();
        thread.start();
        Thread.sleep(2000);
        h = new SocketHandler();
        h.setLevel(Level.FINE);
        assertSame(h.getLevel(), Level.FINE);
        LogRecord r = new LogRecord(Level.INFO, "testPublish_NoFormatter");
        assertTrue(h.isLoggable(r));
        h.close();
        thread.getReadString();
        h.publish(r);
        h.flush();
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
    public static class MockNoSocketSecurityManager extends SecurityManager {
        public MockNoSocketSecurityManager() {
        }
        public void checkPermission(Permission perm) {
        }
        public void checkPermission(Permission perm, Object context) {
        }
        public void checkConnect(String host, int port) {
            throw new SecurityException();
        }
    }
    public static class MockSocketHandler extends SocketHandler {
        public MockSocketHandler() throws Exception {
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
    public static class MockHandler extends Handler {
        public MockHandler() throws Exception {
        }
        @Override
        public void close() {
        }
        @Override
        public void flush() {
        }
        @Override
        public void publish(LogRecord record) {
        }
    }
    public static class ServerThread extends Thread {
        private volatile StringBuffer sb = new StringBuffer();
        private volatile boolean finished = false;
        public boolean finished() {
            return this.finished;
        }
        public String getReadString() throws Exception {
            int i = 0;
            while (!this.finished) {
                sleep(100);
                if (++i > 100) {
                    try {
                        Socket s = new Socket("127.0.0.1", 6666);
                        OutputStream os = s.getOutputStream();
                        os.write(1);
                        os.close();
                        s.close();
                    } catch (Exception e) {
                    }
                }
            }
            return this.sb.toString();
        }
        public void run() {
            ServerSocket ss = null;
            Socket s = null;
            InputStreamReader reader = null;
            try {
                char[] buffer = new char[32];
                ss = new ServerSocket(6666);
                s = ss.accept();
                reader = new InputStreamReader(s.getInputStream());
                while (true) {
                    int length = reader.read(buffer);
                    if (-1 == length) {
                        break;
                    }
                    this.sb.append(buffer, 0, length);
                }
            } catch (Exception e) {
                e.printStackTrace(System.err);
            } finally {
                try {
                    if (null != reader) {
                        reader.close();
                        s.close();
                        ss.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace(System.err);
                }
                this.finished = true;
            }
        }
    }
}
