@TestTargetClass(LogRecord.class)
public class LogRecordTest extends TestCase {
    static final String MSG = "test msg, pls. ignore itb";
    private LogRecord lr;
    private static String className = LogRecordTest.class.getName();
    protected void setUp() throws Exception {
        super.setUp();
        lr = new LogRecord(Level.CONFIG, MSG);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "LogRecord",
        args = {java.util.logging.Level.class, java.lang.String.class}
    )
    public void testLogRecordWithNullPointers() {
        try {
            new LogRecord(null, null);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
        }
        try {
            new LogRecord(null, MSG);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
        }
        LogRecord r = new LogRecord(Level.WARNING, null);
        assertSame(r.getLevel(), Level.WARNING);
        assertNull(r.getMessage());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "normally set/get don't need to be tested",
            method = "getLoggerName",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "normally set/get don't need to be tested",
            method = "setLoggerName",
            args = {java.lang.String.class}
        )
    })
    public void testGetSetLoggerName() {
        assertNull(lr.getLoggerName());
        lr.setLoggerName(null);
        assertNull(lr.getLoggerName());
        lr.setLoggerName("test logger name");
        assertEquals("test logger name", lr.getLoggerName());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "getResourceBundle",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "setResourceBundle",
            args = {java.util.ResourceBundle.class}
        )
    })
    public void testGetSetResourceBundle() {
        assertNull(lr.getResourceBundleName());
        assertNull(lr.getResourceBundle());
        lr.setResourceBundle(null);
        assertNull(lr.getResourceBundle());
        lr.setResourceBundleName("bundles/java/util/logging/res");
        assertNull(lr.getResourceBundle());
        lr.setResourceBundleName(null);
        ResourceBundle rb = ResourceBundle
                .getBundle("bundles/java/util/logging/res");
        lr.setResourceBundle(rb);
        assertEquals(rb, lr.getResourceBundle());
        assertNull(lr.getResourceBundleName());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "getResourceBundleName",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "setResourceBundleName",
            args = {java.lang.String.class}
        )
    })
    public void testGetSetResourceBundleName() {
        assertNull(lr.getResourceBundleName());
        lr.setResourceBundleName(null);
        assertNull(lr.getResourceBundleName());
        lr.setResourceBundleName("test");
        assertEquals("test", lr.getResourceBundleName());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "normal behavior of getter/setter don't need to be tested (normally)",
            method = "getLevel",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "normal behavior of getter/setter don't need to be tested (normally)",
            method = "setLevel",
            args = {java.util.logging.Level.class}
        )
    })
    public void testGetSetLevelNormal() {           
        assertSame(lr.getLevel(), Level.CONFIG);
        lr.setLevel(Level.ALL);
        assertSame(lr.getLevel(), Level.ALL);
        lr.setLevel(Level.FINEST);
        assertSame(lr.getLevel(), Level.FINEST);
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "getLevel",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "setLevel",
            args = {java.util.logging.Level.class}
        )
    })
    public void testGetSetLevelNullPointerException() {
        try {
            lr.setLevel(null);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
        }
        assertSame(lr.getLevel(), Level.CONFIG);
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "getSequenceNumber",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "setSequenceNumber",
            args = {long.class}
        )
    })
    public void testGetSetSequenceNumber() {
        long l = lr.getSequenceNumber();
        lr.setSequenceNumber(-111);
        assertEquals(lr.getSequenceNumber(), -111L);
        lr.setSequenceNumber(0);
        assertEquals(lr.getSequenceNumber(), 0L);
        lr = new LogRecord(Level.ALL, null); 
        assertEquals(lr.getSequenceNumber(), l + 1);
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "getSourceClassName",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "setSourceClassName",
            args = {java.lang.String.class}
        )
    })
    public void testGetSetSourceClassName() {
        lr.setSourceClassName(null);
        assertNull(lr.getSourceClassName());
        lr.setSourceClassName("bad class name");
        assertEquals("bad class name", lr.getSourceClassName());
        lr.setSourceClassName(this.getClass().getName());
        assertEquals(this.getClass().getName(), lr.getSourceClassName());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "getSourceMethodName",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "setSourceMethodName",
            args = {java.lang.String.class}
        )
    })
    public void testGetSetSourceMethodName() {
        lr.setSourceMethodName(null);
        assertNull(lr.getSourceMethodName());
        lr.setSourceMethodName("bad class name");
        assertEquals("bad class name", lr.getSourceMethodName());
        lr.setSourceMethodName(this.getClass().getName());
        assertEquals(this.getClass().getName(), lr.getSourceMethodName());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "getSourceMethodName",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "setSourceMethodName",
            args = {java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "getSourceClassName",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "setSourceClassName",
            args = {java.lang.String.class}
        )
    })
    public void testGetSourceDefaultValue() {
        assertNull(lr.getSourceMethodName());
        assertNull(lr.getSourceClassName());
        Logger logger = Logger.getLogger("testGetSourceDefaultValue");
        MockHandler handler = new MockHandler();
        logger.addHandler(handler);
        logger.log(Level.SEVERE, MSG);
        assertEquals(this.getClass().getName(), handler.getSourceClassName());
        assertEquals("testGetSourceDefaultValue", handler.getSourceMethodName());
        lr = new LogRecord(Level.SEVERE, MSG);
        lr.setSourceMethodName(null);
        logger.log(lr);
        assertNull(handler.getSourceClassName());
        assertNull(handler.getSourceMethodName());
        lr = new LogRecord(Level.SEVERE, MSG);
        lr.setSourceClassName(null);
        logger.log(lr);
        assertNull(handler.getSourceClassName());
        assertNull(handler.getSourceMethodName());
        lr = new LogRecord(Level.SEVERE, MSG);
        lr.setSourceClassName("className");
        lr.setSourceMethodName("methodName");
        logger.log(lr);
        assertEquals("className", handler.getSourceClassName());
        assertEquals("methodName", handler.getSourceMethodName());
        logger.log(RecordFactory.getDefaultRecord());
        assertEquals(this.getClass().getName(), handler.getSourceClassName());
        assertEquals("testGetSourceDefaultValue", handler.getSourceMethodName());
        lr = RecordFactory.getDefaultRecord();
        RecordFactory.log(logger, lr);
        assertEquals(RecordFactory.class.getName(), handler
                .getSourceClassName());
        assertEquals("log", handler.getSourceMethodName());
        lr = RecordFactory.getDefaultRecord();
        assertNull(lr.getSourceClassName());
        assertNull(lr.getSourceMethodName());
        RecordFactory.log(logger, lr);
        assertNull(handler.getSourceClassName());
        assertNull(handler.getSourceMethodName());
        MockLogger ml = new MockLogger("foo", null);
        ml.addHandler(handler);
        ml.info(MSG);
        assertEquals(className + "$MockLogger", handler.getSourceClassName());
        assertEquals("info", handler.getSourceMethodName());
        ml = new MockLogger("foo", null);
        ml.addHandler(handler);
        ml.log(Level.SEVERE, MSG);
        assertNull(handler.getSourceClassName());
        assertNull(handler.getSourceMethodName());
        lr = new LogRecord(Level.SEVERE, MSG);
        handler.publish(lr);
        assertNull(handler.getSourceClassName());
        assertNull(handler.getSourceMethodName());
        logger.removeHandler(handler);
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "getMessage",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "setMessage",
            args = {java.lang.String.class}
        )
    })
    public void testGetSetMessage() {
        assertEquals(MSG, lr.getMessage());
        lr.setMessage(null);
        assertNull(lr.getMessage());
        lr.setMessage("");
        assertEquals("", lr.getMessage());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "getParameters",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "setParameters",
            args = {java.lang.Object[].class}
        )
    })
    public void testGetSetParameters() {
        assertNull(lr.getParameters());
        lr.setParameters(null);
        assertNull(lr.getParameters());
        Object[] oa = new Object[0];
        lr.setParameters(oa);
        assertEquals(oa, lr.getParameters());
        oa = new Object[] {new Object(), new Object()};
        lr.setParameters(oa);
        assertSame(oa, lr.getParameters());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "getMillis",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "setMillis",
            args = {long.class}
        )
    })
    public void testGetSetMillis() {
        long milli = lr.getMillis();
        assertTrue(milli > 0);
        lr.setMillis(-1);
        assertEquals(-1, lr.getMillis());
        lr.setMillis(0);
        assertEquals(0, lr.getMillis());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "getMillis",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "setMillis",
            args = {long.class}
        )
    })
    public void testGetSetTimeCheck() {
        long before = lr.getMillis();
        try {
            Thread.sleep(2);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        LogRecord lr2 = new LogRecord(Level.CONFIG, "MSG2");
        long after = lr2.getMillis();
        assertTrue(after-before>0);    
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "getThreadID",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "setThreadID",
            args = {int.class}
        )
    })
    public void testGetSetThreadID() {
        int id = lr.getThreadID();
        lr = new LogRecord(Level.ALL, "a1");
        assertEquals(id, lr.getThreadID());
        lr.setThreadID(id + 10);
        assertEquals(id + 10, lr.getThreadID());
        lr = new LogRecord(Level.ALL, "a1");
        assertEquals(id, lr.getThreadID());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "getThreadID",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "setThreadID",
            args = {int.class}
        )
    })
    public void testGetSetThreadID_DifferentThread() {
        int id = lr.getThreadID();
        MockThread thread = new MockThread();
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }  
        MockThread thread2 = new MockThread();
        thread2.start();
        try {
            thread2.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assertTrue(lr.getThreadID() != thread.lr.getThreadID());
        assertTrue(lr.getThreadID() != thread2.lr.getThreadID());
        assertTrue(thread.lr.getThreadID() != thread2.lr.getThreadID());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "getThrown",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "setThrown",
            args = {java.lang.Throwable.class}
        )
    })
    public void testGetSetThrown() {
        assertNull(lr.getThrown());
        lr.setThrown(null);
        assertNull(lr.getThrown());
        Throwable e = new Exception();
        lr.setThrown(e);
        assertEquals(e, lr.getThrown());
        Throwable n = new NullPointerException();
        lr.setThrown(n);
        assertEquals(n, lr.getThrown());
    }
    private static final SerializableAssert LOGRECORD_COMPARATOR = new SerializableAssert() {
        public void assertDeserialized(Serializable initial,
                Serializable deserialized) {
            LogRecord init = (LogRecord) initial;
            LogRecord dser = (LogRecord) deserialized;
            assertEquals("Class", init.getClass(), dser.getClass());
            assertEquals("Level", init.getLevel(), dser.getLevel());
            assertEquals("LoggerName", init.getLoggerName(), dser
                    .getLoggerName());
            assertEquals("Message", init.getMessage(), dser.getMessage());
            assertEquals("Millis", init.getMillis(), dser.getMillis());
            Object[] paramInit = init.getParameters();
            Object[] paramDser = dser.getParameters();
            assertEquals("Parameters length", paramInit.length,
                    paramDser.length);
            for (int i = 0; i < paramInit.length; i++) {
                assertEquals("Param: " + i, paramInit[i].toString(),
                        paramDser[i]);
            }
            assertEquals("ResourceBundleName", init.getResourceBundleName(),
                    dser.getResourceBundleName());
            assertEquals("SequenceNumber", init.getSequenceNumber(), dser
                    .getSequenceNumber());
            assertEquals("SourceClassName", init.getSourceClassName(), dser
                    .getSourceClassName());
            assertEquals("SourceMethodName", init.getSourceMethodName(), dser
                    .getSourceMethodName());
            assertEquals("ThreadID", init.getThreadID(), dser.getThreadID());
            SerializationTest.THROWABLE_COMPARATOR.assertDeserialized(init
                    .getThrown(), dser.getThrown());
        }
    };
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "serialisation/deserialization",
        method = "!SerializationSelf",
        args = {}
    )
    public void testSerializationSelf() throws Exception {
        LogRecord r = new LogRecord(Level.ALL, "msg");
        r.setLoggerName("LoggerName");
        r.setMillis(123456789);
        r.setResourceBundleName("ResourceBundleName");
        r.setSequenceNumber(987654321);
        r.setSourceClassName("SourceClassName");
        r.setSourceMethodName("SourceMethodName");
        r.setParameters(new Object[] {"test string", new Exception("ex-msg")});
        r.setThreadID(3232);
        r.setThrown(new Exception("ExceptionMessage"));
        SerializationTest.verifySelf(r, LOGRECORD_COMPARATOR);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "tests resolution of resource bundle during deserialization",
        method = "!Serialization",
        args = {}
    )
    public void testSerializationResourceBundle() throws Exception {
        lr.setResourceBundleName("bundles/java/util/logging/res2");
        lr.setResourceBundle(ResourceBundle.getBundle(
                "bundles/java/util/logging/res", Locale.US));
        LogRecord result = (LogRecord) SerializationTest.copySerializable(lr);
        assertNotNull(result.getResourceBundle());
        lr.setResourceBundleName("bad bundle name");
        lr.setResourceBundle(ResourceBundle.getBundle(
                "bundles/java/util/logging/res", Locale.US));
        result = (LogRecord) SerializationTest.copySerializable(lr);
        assertNull(result.getResourceBundle());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "!SerializationGolden",
        args = {}
    )
    public void testSerializationCompatibility() throws Exception {
        LogRecord r = new LogRecord(Level.ALL, "msg");
        r.setLoggerName("LoggerName");
        r.setMillis(123456789);
        r.setResourceBundleName("ResourceBundleName");
        r.setSequenceNumber(987654321);
        r.setSourceClassName("SourceClassName");
        r.setSourceMethodName("SourceMethodName");
        r.setParameters(new Object[] {"test string", new Exception("ex-msg")});
        r.setThreadID(3232);
        r.setThrown(new Exception("ExceptionMessage"));
        SerializationTest.verifyGolden(this, r, LOGRECORD_COMPARATOR);
    }
    public static class MockHandler extends Handler {
        private String className;
        private String methodName;
        public void close() {
        }
        public void flush() {
        }
        public void publish(LogRecord record) {
            className = record.getSourceClassName();
            methodName = record.getSourceMethodName();
        }
        public String getSourceMethodName() {
            return methodName;
        }
        public String getSourceClassName() {
            return className;
        }
    }
    public static class RecordFactory {
        public static LogRecord getDefaultRecord() {
            return new LogRecord(Level.SEVERE, MSG);
        }
        public static void log(Logger logger, LogRecord lr) {
            logger.log(lr);
        }
    }
    public static class MockLogger extends Logger {
        public MockLogger(String name, String resourceBundleName) {
            super(name, resourceBundleName);
        }
        public void log(Level l, String s) {
            this.log(new LogRecord(l, s));
        }
        public void info(String s) {
            super.info(s);
        }
        public void log(LogRecord record) {
            if (isLoggable(record.getLevel())) {
                Handler[] ha = this.getHandlers();
                for (int i = 0; i < ha.length; i++) {
                    ha[i].publish(record);
                }
                if (getUseParentHandlers()) {
                    Logger anyParent = this.getParent();
                    while (null != anyParent) {
                        ha = anyParent.getHandlers();
                        for (int i = 0; i < ha.length; i++) {
                            ha[i].publish(record);
                        }
                        if (anyParent.getUseParentHandlers()) {
                            anyParent = anyParent.getParent();
                        } else {
                            break;
                        }
                    }
                }
            }
        }
    }
    public class MockThread extends Thread {
        public LogRecord lr = null; 
        public MockThread(){
            super();
        }
        public void run() {
            update();
        }
        public synchronized void update(){
            lr = new LogRecord(Level.CONFIG, "msg thread");
        }
    }
}
