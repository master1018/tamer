@TestTargetClass(Logger.class)
public class LoggerTest extends TestCase {
    private final static String VALID_RESOURCE_BUNDLE = "bundles/java/util/logging/res";
    private final static String VALID_RESOURCE_BUNDLE2 = "bundles/java/util/logging/res2";
    private final static String VALID_RESOURCE_BUNDLE3 = "bundles/java/util/logging/res3";
    private final static String INVALID_RESOURCE_BUNDLE = "impossible_not_existing";
    private final static String LOGGING_CONFIG_FILE = "src/test/resources/config/java/util/logging/logging.config";
    private final static String VALID_KEY = "LOGGERTEST";
    private final static String VALID_VALUE = "Test_ZH_CN";
    private final static String VALID_VALUE2 = "Test_NoLocale2";
    private Logger sharedLogger = null;
    private Locale oldLocale = null;
    protected void setUp() throws Exception {
        super.setUp();
        LogManager.getLogManager().reset();
        oldLocale = Locale.getDefault();
        Locale.setDefault(new Locale("zh", "CN"));
        sharedLogger = new MockLogger("SharedLogger", VALID_RESOURCE_BUNDLE);
        sharedLogger.addHandler(new MockHandler());
    }
    protected void tearDown() throws Exception {
        CallVerificationStack.getInstance().clear();
        Locale.setDefault(oldLocale);
        LogManager.getLogManager().reset();
        super.tearDown();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "Logger",
        args = {java.lang.String.class, java.lang.String.class}
    )
    public void testGlobalLogger() {
        assertNull(Logger.global.getFilter());
        assertEquals(0, Logger.global.getHandlers().length);
        assertNull(Logger.global.getLevel());
        assertEquals("global", Logger.global.getName());
        assertNull(Logger.global.getParent().getParent());
        assertNull(Logger.global.getResourceBundle());
        assertNull(Logger.global.getResourceBundleName());
        assertTrue(Logger.global.getUseParentHandlers());
        assertSame(Logger.global, Logger.getLogger("global"));
        assertSame(Logger.global, LogManager.getLogManager()
                .getLogger("global"));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies constructor under normal conditions.",
        method = "Logger",
        args = {java.lang.String.class, java.lang.String.class}
    )
    public void testConstructor_Normal() {
        MockLogger mlog = new MockLogger("myname", VALID_RESOURCE_BUNDLE);
        assertNull(mlog.getFilter());
        assertEquals(0, mlog.getHandlers().length);
        assertNull(mlog.getLevel());
        assertEquals("myname", mlog.getName());
        assertNull(mlog.getParent());
        ResourceBundle rb = mlog.getResourceBundle();
        assertEquals(VALID_VALUE, rb.getString(VALID_KEY));
        assertEquals(mlog.getResourceBundleName(), VALID_RESOURCE_BUNDLE);
        assertTrue(mlog.getUseParentHandlers());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies constructor with null parameters.",
        method = "Logger",
        args = {java.lang.String.class, java.lang.String.class}
    )
    public void testConstructor_Null() {
        MockLogger mlog = new MockLogger(null, null);
        assertNull(mlog.getFilter());
        assertEquals(0, mlog.getHandlers().length);
        assertNull(mlog.getLevel());
        assertNull(mlog.getName());
        assertNull(mlog.getParent());
        assertNull(mlog.getResourceBundle());
        assertNull(mlog.getResourceBundleName());
        assertTrue(mlog.getUseParentHandlers());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies constructor with invalid name.",
        method = "Logger",
        args = {java.lang.String.class, java.lang.String.class}
    )
    public void testConstructor_InvalidName() {
        MockLogger mlog = new MockLogger("...#$%%^&&()-_+=!@~./,[]{};:'\\\"?|",
                null);
        assertEquals("...#$%%^&&()-_+=!@~./,[]{};:'\\\"?|", mlog.getName());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies constructor with empty name.",
        method = "Logger",
        args = {java.lang.String.class, java.lang.String.class}
    )
    public void testConstructor_EmptyName() {
        MockLogger mlog = new MockLogger("", null);
        assertEquals("", mlog.getName());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies MissingResourceException.",
        method = "Logger",
        args = {java.lang.String.class, java.lang.String.class}
    )
    public void testConstructor_InvalidResourceBundle() {
        try {
            new MockLogger(null, INVALID_RESOURCE_BUNDLE);
            fail("Should throw MissingResourceException!");
        } catch (MissingResourceException e) {
        }
        try {
            new MockLogger("testConstructor_InvalidResourceBundle",
                    INVALID_RESOURCE_BUNDLE);
            fail("Should throw MissingResourceException!");
        } catch (MissingResourceException e) {
        }
        try {
            new MockLogger(null, "");
            fail("Should throw MissingResourceException!");
        } catch (MissingResourceException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getAnonymousLogger",
        args = {}
    )
    public void testGetAnonymousLogger() {
        Logger alog = Logger.getAnonymousLogger();
        assertNotSame(alog, Logger.getAnonymousLogger());
        assertNull(alog.getFilter());
        assertEquals(0, alog.getHandlers().length);
        assertNull(alog.getLevel());
        assertNull(alog.getName());
        assertEquals("", alog.getParent().getName());
        assertNull(alog.getParent().getParent());
        assertNull(alog.getResourceBundle());
        assertNull(alog.getResourceBundleName());
        assertTrue(alog.getUseParentHandlers());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies getAnonymousLogger(String resourceBundleName) with valid resource bundle.",
        method = "getAnonymousLogger",
        args = {java.lang.String.class}
    )
    public void testGetAnonymousLogger_ValidResourceBundle() {
        Logger alog = Logger.getAnonymousLogger(VALID_RESOURCE_BUNDLE);
        assertNotSame(alog, Logger.getAnonymousLogger(VALID_RESOURCE_BUNDLE));
        assertNull(alog.getFilter());
        assertEquals(0, alog.getHandlers().length);
        assertNull(alog.getLevel());
        assertNull(alog.getName());
        assertEquals("", alog.getParent().getName());
        assertNull(alog.getParent().getParent());
        assertEquals(VALID_VALUE, alog.getResourceBundle().getString(VALID_KEY));
        assertEquals(alog.getResourceBundleName(), VALID_RESOURCE_BUNDLE);
        assertTrue(alog.getUseParentHandlers());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies getAnonymousLogger(String resourceBundleName) with null resource bundle.",
        method = "getAnonymousLogger",
        args = {java.lang.String.class}
    )
    public void testGetAnonymousLogger_NullResourceBundle() {
        Logger alog = Logger.getAnonymousLogger(null);
        assertNotSame(alog, Logger.getAnonymousLogger(null));
        assertNull(alog.getFilter());
        assertEquals(0, alog.getHandlers().length);
        assertNull(alog.getLevel());
        assertNull(alog.getName());
        assertEquals("", alog.getParent().getName());
        assertNull(alog.getParent().getParent());
        assertNull(alog.getResourceBundle());
        assertNull(alog.getResourceBundleName());
        assertTrue(alog.getUseParentHandlers());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies getAnonymousLogger(String resourceBundleName) with invalid resource bundle.",
        method = "getAnonymousLogger",
        args = {java.lang.String.class}
    )
    public void testGetAnonymousLogger_InvalidResourceBundle() {
        try {
            Logger.getAnonymousLogger(INVALID_RESOURCE_BUNDLE);
            fail("Should throw MissingResourceException!");
        } catch (MissingResourceException e) {
        }
        try {
            Logger.getAnonymousLogger("");
            fail("Should throw MissingResourceException!");
        } catch (MissingResourceException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies getLogger(String), getting a logger with no parent.",
        method = "getLogger",
        args = {java.lang.String.class}
    )
    public void testGetLogger_Normal() throws Exception {
        Properties p = new Properties();
        p.put("testGetLogger_Normal_ANewLogger.level", "ALL");
        LogManager.getLogManager().readConfiguration(
                EnvironmentHelper.PropertiesToInputStream(p));
        assertNull(LogManager.getLogManager().getLogger(
                "testGetLogger_Normal_ANewLogger"));
        Logger log = Logger.getLogger("testGetLogger_Normal_ANewLogger");
        assertSame(log, Logger.getLogger("testGetLogger_Normal_ANewLogger"));
        assertSame(log, LogManager.getLogManager().getLogger(
                "testGetLogger_Normal_ANewLogger"));
        assertNull(log.getFilter());
        assertEquals(0, log.getHandlers().length);
        assertSame(Level.ALL, log.getLevel());
        assertEquals("testGetLogger_Normal_ANewLogger", log.getName());
        assertNull(log.getParent().getParent());
        assertNull(log.getResourceBundle());
        assertNull(log.getResourceBundleName());
        assertTrue(log.getUseParentHandlers());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies getLogger(String), getting a logger with invalid level configured.",
        method = "getLogger",
        args = {java.lang.String.class}
    )
    public void testGetLogger_InvalidLevel() throws Exception {
        Properties p = new Properties();
        p
                .put("testGetLogger_InvalidLevel_ANewLogger.level",
                        "impossible_level");
        LogManager.getLogManager().readConfiguration(
                EnvironmentHelper.PropertiesToInputStream(p));
        assertNull(LogManager.getLogManager().getLogger(
                "testGetLogger_InvalidLevel_ANewLogger"));
        Logger log = Logger.getLogger("testGetLogger_InvalidLevel_ANewLogger");
        assertSame(log, Logger
                .getLogger("testGetLogger_InvalidLevel_ANewLogger"));
        assertSame(log, LogManager.getLogManager().getLogger(
                "testGetLogger_InvalidLevel_ANewLogger"));
        assertNull(log.getLevel());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies getLogger(String) with null name.",
        method = "getLogger",
        args = {java.lang.String.class}
    )
    public void testGetLogger_Null() {
        try {
            Logger.getLogger(null);
            fail("Should throw NullPointerException!");
        } catch (NullPointerException e) {
        }
        try {
            Logger.getLogger(null, null);
            fail("Should throw NullPointerException!");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies getLogger(String) with invalid name.",
        method = "getLogger",
        args = {java.lang.String.class}
    )
    public void testGetLogger_Invalid() {
        Logger log = Logger.getLogger("...#$%%^&&()-_+=!@~./,[]{};:'\\\"?|");
        assertEquals("...#$%%^&&()-_+=!@~./,[]{};:'\\\"?|", log.getName());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies getLogger(String) with empty name.",
        method = "getLogger",
        args = {java.lang.String.class}
    )
    public void testGetLogger_Empty() {
        assertNotNull(LogManager.getLogManager().getLogger(""));
        Logger log = Logger.getLogger("");
        assertSame(log, LogManager.getLogManager().getLogger(""));
        assertNull(log.getFilter());
        assertEquals(0, log.getHandlers().length);
        assertSame(Level.INFO, log.getLevel());
        assertEquals("", log.getName());
        assertNull(log.getParent());
        assertTrue(log.getUseParentHandlers());
        assertNull(log.getResourceBundle());
        assertNull(log.getResourceBundleName());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies getLogger(String), getting a logger with existing parent.",
        method = "getLogger",
        args = {java.lang.String.class, java.lang.String.class}
    )
    public void testGetLogger_WithParent() {
        assertNull(LogManager.getLogManager().getLogger(
                "testGetLogger_WithParent_ParentLogger"));
        Logger root = Logger.getLogger("");
        Logger pLog = Logger.getLogger("testGetLogger_WithParent_ParentLogger",
                VALID_RESOURCE_BUNDLE);
        pLog.setLevel(Level.CONFIG);
        pLog.addHandler(new MockHandler());
        pLog.setFilter(new MockFilter());
        pLog.setUseParentHandlers(false);
        assertEquals("testGetLogger_WithParent_ParentLogger", pLog.getName());
        assertSame(pLog.getParent(), root);
        assertNull(LogManager.getLogManager().getLogger(
                "testGetLogger_WithParent_ParentLogger.child"));
        Logger child = Logger
                .getLogger("testGetLogger_WithParent_ParentLogger.child");
        assertNull(child.getFilter());
        assertEquals(0, child.getHandlers().length);
        assertNull(child.getLevel());
        assertEquals("testGetLogger_WithParent_ParentLogger.child", child
                .getName());
        assertSame(child.getParent(), pLog);
        assertNull(child.getResourceBundle());
        assertNull(child.getResourceBundleName());
        assertTrue(child.getUseParentHandlers());
        Logger notChild = Logger
                .getLogger("testGetLogger_WithParent_ParentLogger1.child");
        assertNull(notChild.getFilter());
        assertEquals(0, notChild.getHandlers().length);
        assertNull(notChild.getLevel());
        assertEquals("testGetLogger_WithParent_ParentLogger1.child", notChild
                .getName());
        assertNotSame(notChild.getParent(), pLog);
        assertNull(notChild.getResourceBundle());
        assertNull(notChild.getResourceBundleName());
        assertTrue(notChild.getUseParentHandlers());
        assertEquals("testGetLogger_WithParent_ParentLogger.child", child
                .getName());
        assertSame(child.getParent().getParent(), root);
        Logger childOfChild = Logger
                .getLogger("testGetLogger_WithParent_ParentLogger.child.child");
        assertNull(childOfChild.getFilter());
        assertEquals(0, childOfChild.getHandlers().length);
        assertSame(child.getParent().getParent(), root);
        assertNull(childOfChild.getLevel());
        assertEquals("testGetLogger_WithParent_ParentLogger.child.child",
                childOfChild.getName());
        assertSame(childOfChild.getParent(), child);
        assertSame(childOfChild.getParent().getParent(), pLog);
        assertSame(childOfChild.getParent().getParent().getParent(), root);
        assertNull(childOfChild.getResourceBundle());
        assertNull(childOfChild.getResourceBundleName());
        assertTrue(childOfChild.getUseParentHandlers());
        assertEquals("testGetLogger_WithParent_ParentLogger1.child", notChild
                .getName());
        assertSame(child.getParent().getParent(), root); 
        assertNotSame(child.getParent(), root); 
        assertNotSame(root.getParent(), root);
        Logger twoDot = Logger.getLogger("..");
        assertSame(twoDot.getParent(), root);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies getLogger(String, String), getting a logger with no parent.",
        method = "getLogger",
        args = {java.lang.String.class, java.lang.String.class}
    )
    public void testGetLoggerWithRes_Normal() throws Exception {
        Properties p = new Properties();
        p.put("testGetLoggerWithRes_Normal_ANewLogger.level", "ALL");
        LogManager.getLogManager().readConfiguration(
                EnvironmentHelper.PropertiesToInputStream(p));
        assertNull(LogManager.getLogManager().getLogger(
                "testGetLoggerWithRes_Normal_ANewLogger"));
        Logger log = Logger.getLogger("testGetLoggerWithRes_Normal_ANewLogger",
                VALID_RESOURCE_BUNDLE);
        assertSame(log, Logger
                .getLogger("testGetLoggerWithRes_Normal_ANewLogger"));
        assertSame(log, LogManager.getLogManager().getLogger(
                "testGetLoggerWithRes_Normal_ANewLogger"));
        assertNull(log.getFilter());
        assertEquals(0, log.getHandlers().length);
        assertSame(Level.ALL, log.getLevel());
        assertEquals("testGetLoggerWithRes_Normal_ANewLogger", log.getName());
        assertNull(log.getParent().getParent());
        assertEquals(VALID_VALUE, log.getResourceBundle().getString(VALID_KEY));
        assertEquals(log.getResourceBundleName(), VALID_RESOURCE_BUNDLE);
        assertTrue(log.getUseParentHandlers());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies getLogger(String, String) with null parameters.",
        method = "getLogger",
        args = {java.lang.String.class, java.lang.String.class}
    )
    public void testGetLoggerWithRes_Null() {
        Logger.getLogger("testGetLoggerWithRes_Null_ANewLogger", null);
        try {
            Logger.getLogger(null, VALID_RESOURCE_BUNDLE);
            fail("Should throw NullPointerException!");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies getLogger(String, String) with invalid resource bundle.",
        method = "getLogger",
        args = {java.lang.String.class, java.lang.String.class}
    )
    public void testGetLoggerWithRes_InvalidResourceBundle() {
        assertNull(LogManager.getLogManager().getLogger(
                "testMissingResourceException"));
        try {
            Logger.getLogger("testMissingResourceException",
                    INVALID_RESOURCE_BUNDLE);
            fail("Should throw MissingResourceException!");
        } catch (MissingResourceException e) {
        }
        assertNull(Logger.getLogger("testMissingResourceException")
                .getResourceBundle());
        assertNull(Logger.getLogger("testMissingResourceException")
                .getResourceBundleName());
        try {
            Logger.getLogger("testMissingResourceException", "");
            fail("Should throw MissingResourceException!");
        } catch (MissingResourceException e) {
        }
        assertNotNull(LogManager.getLogManager().getLogger(""));
        try { 
            Logger.getLogger("", INVALID_RESOURCE_BUNDLE);         
        } catch (MissingResourceException e) { 
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies getLogger(String, String) with valid resource bundle, to get an existing logger with no associated resource bundle.",
        method = "getLogger",
        args = {java.lang.String.class, java.lang.String.class}
    )
    public void testGetLoggerWithRes_ExistingLoggerWithNoRes() {
        assertNull(LogManager.getLogManager().getLogger(
                "testGetLoggerWithRes_ExistingLoggerWithNoRes_ANewLogger"));
        Logger log1 = Logger
                .getLogger("testGetLoggerWithRes_ExistingLoggerWithNoRes_ANewLogger");
        Logger log2 = Logger.getLogger(
                "testGetLoggerWithRes_ExistingLoggerWithNoRes_ANewLogger",
                VALID_RESOURCE_BUNDLE);
        assertSame(log1, log2);
        assertEquals(VALID_VALUE, log1.getResourceBundle().getString(VALID_KEY));
        assertEquals(log1.getResourceBundleName(), VALID_RESOURCE_BUNDLE);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies getLogger(String, String) with valid resource bundle, to get an existing logger with the same associated resource bundle.",
        method = "getLogger",
        args = {java.lang.String.class, java.lang.String.class}
    )
    public void testGetLoggerWithRes_ExistingLoggerWithSameRes() {
        assertNull(LogManager.getLogManager().getLogger(
                "testGetLoggerWithRes_ExistingLoggerWithSameRes_ANewLogger"));
        Logger log1 = Logger.getLogger(
                "testGetLoggerWithRes_ExistingLoggerWithSameRes_ANewLogger",
                VALID_RESOURCE_BUNDLE);
        Logger log2 = Logger.getLogger(
                "testGetLoggerWithRes_ExistingLoggerWithSameRes_ANewLogger",
                VALID_RESOURCE_BUNDLE);
        assertSame(log1, log2);
        assertEquals(VALID_VALUE, log1.getResourceBundle().getString(VALID_KEY));
        assertEquals(log1.getResourceBundleName(), VALID_RESOURCE_BUNDLE);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies getLogger(String, String) with valid resource bundle, to get an existing logger with different associated resource bundle.",
        method = "getLogger",
        args = {java.lang.String.class, java.lang.String.class}
    )
    public void testGetLoggerWithRes_ExistingLoggerWithDiffRes() {
        assertNull(LogManager.getLogManager().getLogger(
                "testGetLoggerWithRes_ExistingLoggerWithDiffRes_ANewLogger"));
        Logger log1 = Logger.getLogger(
                "testGetLoggerWithRes_ExistingLoggerWithDiffRes_ANewLogger",
                VALID_RESOURCE_BUNDLE);
        assertNotNull(log1);
        try {
            Logger
                    .getLogger(
                            "testGetLoggerWithRes_ExistingLoggerWithDiffRes_ANewLogger",
                            VALID_RESOURCE_BUNDLE2);
            fail("Should throw IllegalArgumentException!");
        } catch (IllegalArgumentException e) {
        }
        try {
            Logger
                    .getLogger(
                            "testGetLoggerWithRes_ExistingLoggerWithDiffRes_ANewLogger",
                            null);
            fail("Should throw IllegalArgumentException!");
        } catch (IllegalArgumentException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies getLogger(String, String) with invalid name.",
        method = "getLogger",
        args = {java.lang.String.class, java.lang.String.class}
    )
    public void testGetLoggerWithRes_InvalidName() {
        Logger log = Logger.getLogger(
                "...#$%%^&&()-_+=!@~./,[]{};:'\\\"?|WithRes",
                VALID_RESOURCE_BUNDLE);
        assertEquals("...#$%%^&&()-_+=!@~./,[]{};:'\\\"?|WithRes", log
                .getName());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies getLogger(String, String) with empty name.",
        method = "getLogger",
        args = {java.lang.String.class, java.lang.String.class}
    )
    @SideEffect("Attaches ResourceBundle to anonymous logger; irreversible")
    public void testGetLoggerWithRes_Empty() {
        Logger log = Logger.getLogger("", VALID_RESOURCE_BUNDLE);
        assertSame(log, LogManager.getLogManager().getLogger(""));
        assertNull(log.getFilter());
        assertEquals(0, log.getHandlers().length);
        assertSame(Level.INFO, log.getLevel());
        assertEquals("", log.getName());
        assertNull(log.getParent());
        assertEquals(VALID_VALUE, log.getResourceBundle().getString(VALID_KEY));
        assertEquals(log.getResourceBundleName(), VALID_RESOURCE_BUNDLE);
        assertTrue(log.getUseParentHandlers());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies getLogger(String, String), getting a logger with existing parent.",
        method = "getLogger",
        args = {java.lang.String.class, java.lang.String.class}
    )
    public void testGetLoggerWithRes_WithParentNormal() {
        assertNull(LogManager.getLogManager().getLogger(
                "testGetLoggerWithRes_WithParent_ParentLogger"));
        Logger pLog = Logger
                .getLogger("testGetLoggerWithRes_WithParent_ParentLogger");
        pLog.setLevel(Level.CONFIG);
        pLog.addHandler(new MockHandler());
        pLog.setFilter(new MockFilter());
        pLog.setUseParentHandlers(false);
        assertNull(LogManager.getLogManager().getLogger(
                "testGetLoggerWithRes_WithParent_ParentLogger.child"));
        Logger log = Logger.getLogger(
                "testGetLoggerWithRes_WithParent_ParentLogger.child",
                VALID_RESOURCE_BUNDLE);
        assertNull(log.getFilter());
        assertEquals(0, log.getHandlers().length);
        assertNull(log.getLevel());
        assertEquals("testGetLoggerWithRes_WithParent_ParentLogger.child", log
                .getName());
        assertSame(log.getParent(), pLog);
        assertEquals(VALID_VALUE, log.getResourceBundle().getString(VALID_KEY));
        assertEquals(log.getResourceBundleName(), VALID_RESOURCE_BUNDLE);
        assertTrue(log.getUseParentHandlers());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "addHandler",
        args = {java.util.logging.Handler.class}
    )
    public void testAddHandler_NamedLoggerSufficientPrivilege() {
        Logger log = Logger
                .getLogger("testAddHandler_NamedLoggerSufficientPrivilege");
        MockHandler h = new MockHandler();
        assertEquals(log.getHandlers().length, 0);
        log.addHandler(h);
        assertEquals(log.getHandlers().length, 1);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "addHandler",
        args = {java.util.logging.Handler.class}
    )
    public void testAddHandler_NamedLoggerSufficientPrivilegeDuplicate() {
        Logger log = Logger
                .getLogger("testAddHandler_NamedLoggerSufficientPrivilegeDuplicate");
        MockHandler h = new MockHandler();
        assertEquals(log.getHandlers().length, 0);
        for (int i = 0; i < 12; i++) {
            log.addHandler(h);
        }
        assertEquals(log.getHandlers().length, 12);
        assertSame(log.getHandlers()[0], h);
        assertSame(log.getHandlers()[5], h);
        assertSame(log.getHandlers()[11], h);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies NullPointerException.",
        method = "addHandler",
        args = {java.util.logging.Handler.class}
    )
    public void testAddHandler_Null() {
        Logger log = Logger.getLogger("testAddHandler_Null");
        try {
            log.addHandler(null);
            fail("Should throw NullPointerException!");
        } catch (NullPointerException e) {
        }
        assertEquals(log.getHandlers().length, 0);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "addHandler",
        args = {java.util.logging.Handler.class}
    )
    public void testAddHandler_NamedLoggerInsufficientPrivilege() {
        Logger log = Logger
                .getLogger("testAddHandler_NamedLoggerInsufficientPrivilege");
        MockHandler h = new MockHandler();
        SecurityManager oldMan = System.getSecurityManager();
        System.setSecurityManager(new MockSecurityManager());
        try {
            log.addHandler(h);
            fail("Should throw SecurityException!");
        } catch (SecurityException e) {
        } finally {
            System.setSecurityManager(oldMan);
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "addHandler",
        args = {java.util.logging.Handler.class}
    )
    public void testAddHandler_NamedLoggerInsufficientPrivilegeNull() {
        Logger log = Logger
                .getLogger("testAddHandler_NamedLoggerInsufficientPrivilege");
        SecurityManager oldMan = System.getSecurityManager();
        System.setSecurityManager(new MockSecurityManager());
        try {
            log.addHandler(null);
            fail("Should throw NullPointerException!");
        } catch (NullPointerException e) {
        } finally {
            System.setSecurityManager(oldMan);
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "addHandler",
        args = {java.util.logging.Handler.class}
    )
    public void testAddHandler_AnonyLoggerSufficientPrivilege() {
        Logger log = Logger.getAnonymousLogger();
        MockHandler h = new MockHandler();
        assertEquals(log.getHandlers().length, 0);
        log.addHandler(h);
        assertEquals(log.getHandlers().length, 1);
        assertSame(log.getHandlers()[0], h);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "addHandler",
        args = {java.util.logging.Handler.class}
    )
    public void testAddHandler_AnonyLoggerInsufficientPrivilege() {
        Logger log = Logger.getAnonymousLogger();
        MockHandler h = new MockHandler();
        SecurityManager oldMan = System.getSecurityManager();
        System.setSecurityManager(new MockSecurityManager());
        try {
            assertEquals(log.getHandlers().length, 0);
            log.addHandler(h);
            assertEquals(log.getHandlers().length, 1);
            assertSame(log.getHandlers()[0], h);
        } finally {
            System.setSecurityManager(oldMan);
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies addHandler(Handler) for a null-named mock logger with insufficient privilege, SecurityException.",
        method = "addHandler",
        args = {java.util.logging.Handler.class}
    )
    public void testAddHandler_NullNamedMockLoggerInsufficientPrivilege() {
        MockLogger mlog = new MockLogger(null, null);
        MockHandler h = new MockHandler();
        SecurityManager oldMan = System.getSecurityManager();
        System.setSecurityManager(new MockSecurityManager());
        try {
            mlog.addHandler(h);
            fail("Should throw SecurityException!");
        } catch (SecurityException e) {
        } finally {
            System.setSecurityManager(oldMan);
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "addHandler",
        args = {java.util.logging.Handler.class}
    )
    public void testRemoveHandler_NamedLoggerSufficientPrivilege() {
        Logger log = Logger
                .getLogger("testRemoveHandler_NamedLoggerSufficientPrivilege");
        MockHandler h = new MockHandler();
        log.addHandler(h);
        assertEquals(log.getHandlers().length, 1);
        log.removeHandler(h);
        assertEquals(log.getHandlers().length, 0);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "removeHandler",
        args = {java.util.logging.Handler.class}
    )
    public void testRemoveHandler_NamedLoggerSufficientPrivilegeNotExisting() {
        Logger log = Logger
                .getLogger("testRemoveHandler_NamedLoggerSufficientPrivilegeNotExisting");
        MockHandler h = new MockHandler();
        assertEquals(log.getHandlers().length, 0);
        log.removeHandler(h);
        assertEquals(log.getHandlers().length, 0);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "removeHandler",
        args = {java.util.logging.Handler.class}
    )
    public void testRemoveHandler_Null() {
        Logger log = Logger.getLogger("testRemoveHandler_Null");
        log.removeHandler(null);
        assertEquals(log.getHandlers().length, 0);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "removeHandler",
        args = {java.util.logging.Handler.class}
    )
    public void testRemoveHandler_NamedLoggerInsufficientPrivilege() {
        Logger log = Logger
                .getLogger("testRemoveHandler_NamedLoggerInsufficientPrivilege");
        MockHandler h = new MockHandler();
        SecurityManager oldMan = System.getSecurityManager();
        System.setSecurityManager(new MockSecurityManager());
        try {
            log.removeHandler(h);
            fail("Should throw SecurityException!");
        } catch (SecurityException e) {
        } finally {
            System.setSecurityManager(oldMan);
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "removeHandler",
        args = {java.util.logging.Handler.class}
    )
    public void testRemoveHandler_NamedLoggerInsufficientPrivilegeNull() {
        Logger log = Logger
                .getLogger("testRemoveHandler_NamedLoggerInsufficientPrivilege");
        SecurityManager oldMan = System.getSecurityManager();
        System.setSecurityManager(new MockSecurityManager());
        try {
            log.removeHandler(null);
            fail("Should throw SecurityException!");
        } catch (SecurityException e) {
        } finally {
            System.setSecurityManager(oldMan);
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "removeHandler",
        args = {java.util.logging.Handler.class}
    )
    public void testRemoveHandler_AnonyLoggerSufficientPrivilege() {
        Logger log = Logger.getAnonymousLogger();
        MockHandler h = new MockHandler();
        log.addHandler(h);
        assertEquals(log.getHandlers().length, 1);
        log.removeHandler(h);
        assertEquals(log.getHandlers().length, 0);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "removeHandler",
        args = {java.util.logging.Handler.class}
    )
    public void testRemoveHandler_AnonyLoggerInsufficientPrivilege() {
        Logger log = Logger.getAnonymousLogger();
        MockHandler h = new MockHandler();
        SecurityManager oldMan = System.getSecurityManager();
        System.setSecurityManager(new MockSecurityManager());
        try {
            log.addHandler(h);
            assertEquals(log.getHandlers().length, 1);
            log.removeHandler(h);
            assertEquals(log.getHandlers().length, 0);
        } finally {
            System.setSecurityManager(oldMan);
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "removeHandler",
        args = {java.util.logging.Handler.class}
    )
    public void testRemoveHandler_NullNamedMockLoggerInsufficientPrivilege() {
        MockLogger mlog = new MockLogger(null, null);
        MockHandler h = new MockHandler();
        SecurityManager oldMan = System.getSecurityManager();
        System.setSecurityManager(new MockSecurityManager());
        try {
            mlog.removeHandler(h);
            fail("Should throw SecurityException!");
        } catch (SecurityException e) {
        } finally {
            System.setSecurityManager(oldMan);
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies getHandlers() when there's no handler.",
        method = "getHandlers",
        args = {}
    )
    public void testGetHandlers_None() {
        Logger log = Logger.getLogger("testGetHandlers_None");
        assertEquals(log.getHandlers().length, 0);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies getHandlers() when there are several handlers.",
        method = "getHandlers",
        args = {}
    )
    public void testGetHandlers_Several() {
        Logger log = Logger.getLogger("testGetHandlers_None");
        assertEquals(log.getHandlers().length, 0);
        MockHandler h1 = new MockHandler();
        MockHandler h2 = new MockHandler();
        MockHandler h3 = new MockHandler();
        log.addHandler(h1);
        log.addHandler(h2);
        log.addHandler(h3);
        assertEquals(log.getHandlers().length, 3);
        assertSame(log.getHandlers()[0], h1);
        assertSame(log.getHandlers()[1], h2);
        assertSame(log.getHandlers()[2], h3);
        log.removeHandler(h2);
        assertEquals(log.getHandlers().length, 2);
        assertSame(log.getHandlers()[0], h1);
        assertSame(log.getHandlers()[1], h3);
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL,
            notes = "Verifies getFilter & setFilter with normal value for a named logger, having sufficient privilege.",
            method = "getFilter",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL,
            notes = "Verifies getFilter & setFilter with normal value for a named logger, having sufficient privilege.",
            method = "setFilter",
            args = {java.util.logging.Filter.class}
        )
    })
    public void testGetSetFilter_NamedLoggerSufficientPrivilege() {
        Logger log = Logger
                .getLogger("testGetSetFilter_NamedLoggerSufficientPrivilege");
        Filter f = new MockFilter();
        assertNull(log.getFilter());
        log.setFilter(f);
        assertSame(f, log.getFilter());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL,
            notes = "Verifies getFilter & setFilter with null value, having sufficient privilege.",
            method = "getFilter",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL,
            notes = "Verifies getFilter & setFilter with null value, having sufficient privilege.",
            method = "setFilter",
            args = {java.util.logging.Filter.class}
        )
    })
    public void testGetSetFilter_Null() {
        Logger log = Logger.getLogger("testGetSetFilter_Null");
        assertNull(log.getFilter());
        log.setFilter(null);
        assertNull(log.getFilter());
        log.setFilter(new MockFilter());
        log.setFilter(null);
        assertNull(log.getFilter());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL,
            notes = "Verifies setFilter with normal value for a named logger, having insufficient privilege.",
            method = "getFilter",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL,
            notes = "Verifies setFilter with normal value for a named logger, having insufficient privilege.",
            method = "setFilter",
            args = {java.util.logging.Filter.class}
        )
    })
    public void testGetSetFilter_NamedLoggerInsufficientPrivilege() {
        Logger log = Logger
                .getLogger("testGetSetFilter_NamedLoggerInsufficientPrivilege");
        Filter f = new MockFilter();
        SecurityManager oldMan = System.getSecurityManager();
        System.setSecurityManager(new MockSecurityManager());
        try {
            log.setFilter(f);
            fail("Should throw SecurityException!");
        } catch (SecurityException e) {
        } finally {
            System.setSecurityManager(oldMan);
        }
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "getFilter",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "setFilter",
            args = {java.util.logging.Filter.class}
        )
    })
    public void testSetFilter_AnonyLoggerSufficientPrivilege() {
        Logger log = Logger.getAnonymousLogger();
        Filter f = new MockFilter();
        assertNull(log.getFilter());
        log.setFilter(f);
        assertSame(f, log.getFilter());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "getFilter",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "setFilter",
            args = {java.util.logging.Filter.class}
        )
    })
    public void testSetFilter_AnonyLoggerInsufficientPrivilege() {
        Logger log = Logger.getAnonymousLogger();
        Filter f = new MockFilter();
        SecurityManager oldMan = System.getSecurityManager();
        System.setSecurityManager(new MockSecurityManager());
        try {
            assertNull(log.getFilter());
            log.setFilter(f);
            assertSame(f, log.getFilter());
        } finally {
            System.setSecurityManager(oldMan);
        }
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "getFilter",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "setFilter",
            args = {java.util.logging.Filter.class}
        )
    })
    public void testSetFilter_NullNamedMockLoggerInsufficientPrivilege() {
        MockLogger mlog = new MockLogger(null, null);
        Filter f = new MockFilter();
        SecurityManager oldMan = System.getSecurityManager();
        System.setSecurityManager(new MockSecurityManager());
        try {
            mlog.setFilter(f);
            fail("Should throw SecurityException!");
        } catch (SecurityException e) {
        } finally {
            System.setSecurityManager(oldMan);
        }
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL,
            notes = "Verifies getLevel & setLevel with normal value for a named logger, having sufficient privilege.",
            method = "setLevel",
            args = {java.util.logging.Level.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL,
            notes = "Verifies getLevel & setLevel with normal value for a named logger, having sufficient privilege.",
            method = "getLevel",
            args = {}
        )
    })
    public void testGetSetLevel_NamedLoggerSufficientPrivilege() {
        Logger log = Logger
                .getLogger("testGetSetLevel_NamedLoggerSufficientPrivilege");
        assertNull(log.getLevel());
        log.setLevel(Level.CONFIG);
        assertSame(Level.CONFIG, log.getLevel());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL,
            notes = "Verifies getLevel & setLevel with null value, having sufficient privilege.",
            method = "getLevel",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL,
            notes = "Verifies getLevel & setLevel with null value, having sufficient privilege.",
            method = "setLevel",
            args = {java.util.logging.Level.class}
        )
    })
    public void testGetSetLevel_Null() {
        Logger log = Logger.getLogger("testGetSetLevel_Null");
        assertNull(log.getLevel());
        log.setLevel(null);
        assertNull(log.getLevel());
        log.setLevel(Level.CONFIG);
        log.setLevel(null);
        assertNull(log.getLevel());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL,
            notes = "Verifies setLevel with normal value for a named logger, having insufficient privilege.",
            method = "setLevel",
            args = {java.util.logging.Level.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL,
            notes = "Verifies setLevel with normal value for a named logger, having insufficient privilege.",
            method = "getLevel",
            args = {}
        )
    })
    public void testGetSetLevel_NamedLoggerInsufficientPrivilege() {
        Logger log = Logger
                .getLogger("testGetSetLevel_NamedLoggerInsufficientPrivilege");
        SecurityManager oldMan = System.getSecurityManager();
        System.setSecurityManager(new MockSecurityManager());
        try {
            log.setLevel(Level.CONFIG);
            fail("Should throw SecurityException!");
        } catch (SecurityException e) {
        } finally {
            System.setSecurityManager(oldMan);
        }
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
    public void testSetLevel_AnonyLoggerSufficientPrivilege() {
        Logger log = Logger.getAnonymousLogger();
        assertNull(log.getLevel());
        log.setLevel(Level.CONFIG);
        assertSame(Level.CONFIG, log.getLevel());
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
    public void testSetLevel_AnonyLoggerInsufficientPrivilege() {
        Logger log = Logger.getAnonymousLogger();
        SecurityManager oldMan = System.getSecurityManager();
        System.setSecurityManager(new MockSecurityManager());
        try {
            assertNull(log.getLevel());
            log.setLevel(Level.CONFIG);
            assertSame(Level.CONFIG, log.getLevel());
        } finally {
            System.setSecurityManager(oldMan);
        }
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
    public void testSetLevel_NullNamedMockLoggerInsufficientPrivilege() {
        MockLogger mlog = new MockLogger(null, null);
        SecurityManager oldMan = System.getSecurityManager();
        System.setSecurityManager(new MockSecurityManager());
        try {
            mlog.setLevel(Level.CONFIG);
            fail("Should throw SecurityException!");
        } catch (SecurityException e) {
        } finally {
            System.setSecurityManager(oldMan);
        }
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "getUseParentHandlers",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "setUseParentHandlers",
            args = {boolean.class}
        )
    })
    public void testGetSetUseParentHandlers_NamedLoggerSufficientPrivilege() {
        Logger log = Logger
                .getLogger("testGetSetUseParentHandlers_NamedLoggerSufficientPrivilege");
        assertTrue(log.getUseParentHandlers());
        log.setUseParentHandlers(false);
        assertFalse(log.getUseParentHandlers());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "getUseParentHandlers",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "setUseParentHandlers",
            args = {boolean.class}
        )
    })
    public void testGetSetUseParentHandlers_NamedLoggerInsufficientPrivilege() {
        Logger log = Logger
                .getLogger("testGetSetUseParentHandlers_NamedLoggerInsufficientPrivilege");
        SecurityManager oldMan = System.getSecurityManager();
        System.setSecurityManager(new MockSecurityManager());
        try {
            log.setUseParentHandlers(true);
            fail("Should throw SecurityException!");
        } catch (SecurityException e) {
        } finally {
            System.setSecurityManager(oldMan);
        }
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "getUseParentHandlers",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "setUseParentHandlers",
            args = {boolean.class}
        )
    })
    public void testSetUseParentHandlers_AnonyLoggerSufficientPrivilege() {
        Logger log = Logger.getAnonymousLogger();
        assertTrue(log.getUseParentHandlers());
        log.setUseParentHandlers(false);
        assertFalse(log.getUseParentHandlers());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "getUseParentHandlers",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "setUseParentHandlers",
            args = {boolean.class}
        )
    })
    public void testSetUseParentHandlers_AnonyLoggerInsufficientPrivilege() {
        Logger log = Logger.getAnonymousLogger();
        SecurityManager oldMan = System.getSecurityManager();
        System.setSecurityManager(new MockSecurityManager());
        try {
            assertTrue(log.getUseParentHandlers());
            log.setUseParentHandlers(false);
            assertFalse(log.getUseParentHandlers());
        } finally {
            System.setSecurityManager(oldMan);
        }
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "getUseParentHandlers",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "setUseParentHandlers",
            args = {boolean.class}
        )
    })
    public void testSetUseParentHandlers_NullNamedMockLoggerInsufficientPrivilege() {
        MockLogger mlog = new MockLogger(null, null);
        SecurityManager oldMan = System.getSecurityManager();
        System.setSecurityManager(new MockSecurityManager());
        try {
            mlog.setUseParentHandlers(true);
            fail("Should throw SecurityException!");
        } catch (SecurityException e) {
        } finally {
            System.setSecurityManager(oldMan);
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies getParent() for root logger.",
        method = "getParent",
        args = {}
    )
    public void testGetParent_Root() {
        assertNull(Logger.getLogger("").getParent());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies getParent() for normal named loggers.",
        method = "getParent",
        args = {}
    )
    public void testGetParent_NormalNamed() {
        Logger log = Logger.getLogger("testGetParent_NormalNamed");
        assertSame(log.getParent(), Logger.getLogger(""));
        Logger child = Logger.getLogger("testGetParent_NormalNamed.child");
        assertSame(child.getParent(), log);
        Logger child2 = Logger.getLogger("testGetParent_NormalNamed.a.b.c");
        assertSame(child2.getParent(), log);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies getParent() for anonymous loggers.",
        method = "getParent",
        args = {}
    )
    public void testGetParent_Anonymous() {
        assertSame(Logger.getAnonymousLogger().getParent(), Logger
                .getLogger(""));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setParent",
        args = {java.util.logging.Logger.class}
    )
    public void testSetParent_Normal() {
        Logger log = new MockLogger(null, null);
        Logger parent = new MockLogger(null, null);
        assertNull(log.getParent());
        log.setParent(parent);
        assertSame(log.getParent(), parent);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setParent",
        args = {java.util.logging.Logger.class}
    )
    public void testSetParent_Null() {
        try {
            (new MockLogger(null, null)).setParent(null);
            fail("Should throw NullPointerException!");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setParent",
        args = {java.util.logging.Logger.class}
    )
    public void testSetParent_InsufficientPrivilege() {
        MockLogger log = new MockLogger(null, null);
        SecurityManager oldMan = System.getSecurityManager();
        System.setSecurityManager(new MockSecurityManager());
        try {
            log.setParent(log);
            fail("Should throw SecurityException!");
        } catch (SecurityException e) {
        } finally {
            System.setSecurityManager(oldMan);
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setParent",
        args = {java.util.logging.Logger.class}
    )
    public void testSetParent_InsufficientPrivilegeNull() {
        MockLogger log = new MockLogger(null, null);
        SecurityManager oldMan = System.getSecurityManager();
        System.setSecurityManager(new MockSecurityManager());
        try {
            log.setParent(null);
            fail("Should throw NullPointerException!");
        } catch (NullPointerException e) {
        } finally {
            System.setSecurityManager(oldMan);
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setParent",
        args = {java.util.logging.Logger.class}
    )
    public void testSetParent_AnonyLoggerInsufficientPrivilege() {
        Logger log = Logger.getAnonymousLogger();
        SecurityManager oldMan = System.getSecurityManager();
        System.setSecurityManager(new MockSecurityManager());
        try {
            log.setParent(log);
            fail("Should throw SecurityException!");
        } catch (SecurityException e) {
        } finally {
            System.setSecurityManager(oldMan);
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies getName() for normal names.",
        method = "getName",
        args = {}
    )
    public void testGetName_Normal() {
        Logger log = Logger.getLogger("testGetName_Normal");
        assertEquals("testGetName_Normal", log.getName());
        Logger mlog = new MockLogger("testGetName_Normal", null);
        assertEquals("testGetName_Normal", mlog.getName());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies getName() for empty name.",
        method = "getName",
        args = {}
    )
    public void testGetName_Empty() {
        Logger log = Logger.getLogger("");
        assertEquals("", log.getName());
        Logger mlog = new MockLogger("", null);
        assertEquals("", mlog.getName());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies getName() for null name.",
        method = "getName",
        args = {}
    )
    public void testGetName_Null() {
        Logger log = Logger.getAnonymousLogger();
        assertNull(log.getName());
        Logger mlog = new MockLogger(null, null);
        assertNull(mlog.getName());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies getResourceBundle() when it it not null.",
        method = "getResourceBundle",
        args = {}
    )
    public void testGetResourceBundle_Normal() {
        Logger log = Logger.getLogger("testGetResourceBundle_Normal",
                VALID_RESOURCE_BUNDLE);
        assertEquals(VALID_VALUE, log.getResourceBundle().getString(VALID_KEY));
        Logger mlog = new MockLogger(null, VALID_RESOURCE_BUNDLE);
        assertEquals(VALID_VALUE, mlog.getResourceBundle().getString(VALID_KEY));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies getResourceBundle() when it it null.",
        method = "getResourceBundle",
        args = {}
    )
    public void testGetResourceBundle_Null() {
        Logger log = Logger.getLogger("testGetResourceBundle_Null", null);
        assertNull(log.getResourceBundle());
        Logger mlog = new MockLogger(null, null);
        assertNull(mlog.getResourceBundle());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies getResourceBundleName() when it it not null.",
        method = "getResourceBundleName",
        args = {}
    )
    public void testGetResourceBundleName_Normal() {
        Logger log = Logger.getLogger("testGetResourceBundleName_Normal",
                VALID_RESOURCE_BUNDLE);
        assertEquals(VALID_RESOURCE_BUNDLE, log.getResourceBundleName());
        Logger mlog = new MockLogger(null, null);
        assertNull(mlog.getResourceBundleName());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies getResourceBundleName() when it it null.",
        method = "getResourceBundleName",
        args = {}
    )
    public void testGetResourceBundleName_Null() {
        Logger log = Logger.getLogger("testGetResourceBundleName_Null", null);
        assertNull(log.getResourceBundleName());
        Logger mlog = new MockLogger(null, null);
        assertNull(mlog.getResourceBundleName());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "isLoggable",
        args = {java.util.logging.Level.class}
    )
    public void testIsLoggable() {
        MockLogger mlog = new MockLogger(null, null);
        assertNull(mlog.getLevel());
        assertNull(mlog.getParent());
        assertTrue(mlog.isLoggable(Level.SEVERE));
        assertTrue(mlog.isLoggable(Level.WARNING));
        assertTrue(mlog.isLoggable(Level.INFO));
        assertFalse(mlog.isLoggable(Level.CONFIG));
        assertFalse(mlog.isLoggable(Level.FINE));
        assertFalse(mlog.isLoggable(Level.ALL));
        assertTrue(mlog.isLoggable(Level.OFF));
        mlog.setLevel(Level.CONFIG);
        assertTrue(mlog.isLoggable(Level.SEVERE));
        assertTrue(mlog.isLoggable(Level.CONFIG));
        assertFalse(mlog.isLoggable(Level.ALL));
        assertTrue(mlog.isLoggable(Level.OFF));
        mlog.setLevel(Level.ALL);
        assertTrue(mlog.isLoggable(Level.ALL));
        assertTrue(mlog.isLoggable(Level.SEVERE));
        assertTrue(mlog.isLoggable(Level.OFF));
        mlog.setLevel(Level.OFF);
        assertFalse(mlog.isLoggable(Level.ALL));
        assertFalse(mlog.isLoggable(Level.SEVERE));
        assertFalse(mlog.isLoggable(Level.OFF));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "throwing",
        args = {java.lang.String.class, java.lang.String.class, java.lang.Throwable.class}
    )
    public void testThrowing_Normal() {
        Throwable t = new Throwable();
        this.sharedLogger.setLevel(Level.FINER);
        this.sharedLogger.throwing("sourceClass", "sourceMethod", t);
        LogRecord r = (LogRecord) CallVerificationStack.getInstance().pop();
        assertTrue(CallVerificationStack.getInstance().empty());
        assertSame(r.getLoggerName(), this.sharedLogger.getName());
        assertEquals(r.getMessage(), "THROW");
        assertSame(r.getResourceBundleName(), this.sharedLogger
                .getResourceBundleName());
        assertSame(r.getResourceBundle(), this.sharedLogger.getResourceBundle());
        assertSame(r.getSourceClassName(), "sourceClass");
        assertSame(r.getSourceMethodName(), "sourceMethod");
        assertSame(r.getLevel(), Level.FINER);
        assertSame(r.getParameters(), null);
        assertSame(r.getThrown(), t);
        this.sharedLogger.setLevel(Level.FINE);
        this.sharedLogger.throwing("sourceClass", "sourceMethod", t);
        assertTrue(CallVerificationStack.getInstance().empty());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "throwing",
        args = {java.lang.String.class, java.lang.String.class, java.lang.Throwable.class}
    )
    public void testThrowing_Null() {
        Logger child = new MockLogger("childLogger", null);
        Logger parent = new MockLogger("parentLogger", VALID_RESOURCE_BUNDLE2);
        child.addHandler(new MockHandler());
        child.setParent(parent);
        child.setLevel(Level.FINER);
        child.throwing(null, null, null);
        LogRecord r = (LogRecord) CallVerificationStack.getInstance().pop();
        assertTrue(CallVerificationStack.getInstance().empty());
        assertSame(r.getLoggerName(), child.getName());
        assertEquals(r.getMessage(), "THROW");
        assertSame(r.getResourceBundleName(), parent.getResourceBundleName());
        assertSame(r.getResourceBundle(), parent.getResourceBundle());
        assertSame(r.getSourceClassName(), null);
        assertSame(r.getSourceMethodName(), null);
        assertSame(r.getLevel(), Level.FINER);
        assertSame(r.getParameters(), null);
        assertSame(r.getThrown(), null);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies entering(String, String) with normal values.",
        method = "entering",
        args = {java.lang.String.class, java.lang.String.class}
    )
    public void testEntering_StringString_Normal() {
        this.sharedLogger.setLevel(Level.FINER);
        this.sharedLogger.entering("sourceClass", "sourceMethod");
        LogRecord r = (LogRecord) CallVerificationStack.getInstance().pop();
        assertTrue(CallVerificationStack.getInstance().empty());
        assertSame(r.getLoggerName(), this.sharedLogger.getName());
        assertEquals(r.getMessage(), "ENTRY");
        assertSame(r.getResourceBundleName(), this.sharedLogger
                .getResourceBundleName());
        assertSame(r.getResourceBundle(), this.sharedLogger.getResourceBundle());
        assertSame(r.getSourceClassName(), "sourceClass");
        assertSame(r.getSourceMethodName(), "sourceMethod");
        assertSame(r.getLevel(), Level.FINER);
        assertSame(r.getParameters(), null);
        assertSame(r.getThrown(), null);
        this.sharedLogger.setLevel(Level.FINE);
        this.sharedLogger.entering("sourceClass", "sourceMethod");
        assertTrue(CallVerificationStack.getInstance().empty());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies entering(String, String) with null values.",
        method = "entering",
        args = {java.lang.String.class, java.lang.String.class}
    )
    public void testEntering_StringString_Null() {
        Logger child = new MockLogger("childLogger", null);
        Logger parent = new MockLogger("parentLogger", VALID_RESOURCE_BUNDLE2);
        child.addHandler(new MockHandler());
        child.setParent(parent);
        child.setLevel(Level.FINER);
        child.entering(null, null);
        LogRecord r = (LogRecord) CallVerificationStack.getInstance().pop();
        assertTrue(CallVerificationStack.getInstance().empty());
        assertSame(r.getLoggerName(), child.getName());
        assertEquals(r.getMessage(), "ENTRY");
        assertSame(r.getResourceBundleName(), parent.getResourceBundleName());
        assertSame(r.getResourceBundle(), parent.getResourceBundle());
        assertSame(r.getSourceClassName(), null);
        assertSame(r.getSourceMethodName(), null);
        assertSame(r.getLevel(), Level.FINER);
        assertSame(r.getParameters(), null);
        assertSame(r.getThrown(), null);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies entering(String, String, Object) with normal values.",
        method = "entering",
        args = {java.lang.String.class, java.lang.String.class, java.lang.Object.class}
    )
    public void testEntering_StringStringObject_Normal() {
        Object param = new Object();
        this.sharedLogger.setLevel(Level.FINER);
        this.sharedLogger.entering("sourceClass", "sourceMethod", param);
        LogRecord r = (LogRecord) CallVerificationStack.getInstance().pop();
        assertTrue(CallVerificationStack.getInstance().empty());
        assertSame(r.getLoggerName(), this.sharedLogger.getName());
        assertEquals(r.getMessage(), "ENTRY {0}");
        assertSame(r.getResourceBundleName(), this.sharedLogger
                .getResourceBundleName());
        assertSame(r.getResourceBundle(), this.sharedLogger.getResourceBundle());
        assertSame(r.getSourceClassName(), "sourceClass");
        assertSame(r.getSourceMethodName(), "sourceMethod");
        assertSame(r.getLevel(), Level.FINER);
        assertSame(r.getParameters()[0], param);
        assertEquals(1, r.getParameters().length);
        assertSame(r.getThrown(), null);
        this.sharedLogger.setLevel(Level.FINE);
        this.sharedLogger.entering("sourceClass", "sourceMethod", param);
        assertTrue(CallVerificationStack.getInstance().empty());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies entering(String, String, Object) with null values.",
        method = "entering",
        args = {java.lang.String.class, java.lang.String.class, java.lang.Object.class}
    )
    public void testEntering_StringStringObject_Null() {
        Logger child = new MockLogger("childLogger", null);
        Logger parent = new MockLogger("parentLogger", VALID_RESOURCE_BUNDLE2);
        child.addHandler(new MockHandler());
        child.setParent(parent);
        child.setLevel(Level.FINER);
        child.entering(null, null, (Object) null);
        LogRecord r = (LogRecord) CallVerificationStack.getInstance().pop();
        assertTrue(CallVerificationStack.getInstance().empty());
        assertSame(r.getLoggerName(), child.getName());
        assertEquals(r.getMessage(), "ENTRY {0}");
        assertSame(r.getResourceBundleName(), parent.getResourceBundleName());
        assertSame(r.getResourceBundle(), parent.getResourceBundle());
        assertSame(r.getSourceClassName(), null);
        assertSame(r.getSourceMethodName(), null);
        assertSame(r.getLevel(), Level.FINER);
        assertEquals(r.getParameters().length, 1);
        assertNull(r.getParameters()[0]);
        assertSame(r.getThrown(), null);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies entering(String, String, Object[]) with normal values.",
        method = "entering",
        args = {java.lang.String.class, java.lang.String.class, java.lang.Object[].class}
    )
    public void testEntering_StringStringObjects_Normal() {
        Object[] params = new Object[2];
        params[0] = new Object();
        params[1] = new Object();
        this.sharedLogger.setLevel(Level.FINER);
        this.sharedLogger.entering("sourceClass", "sourceMethod", params);
        LogRecord r = (LogRecord) CallVerificationStack.getInstance().pop();
        assertTrue(CallVerificationStack.getInstance().empty());
        assertSame(r.getLoggerName(), this.sharedLogger.getName());
        assertEquals(r.getMessage(), "ENTRY {0} {1}");
        assertSame(r.getResourceBundleName(), this.sharedLogger
                .getResourceBundleName());
        assertSame(r.getResourceBundle(), this.sharedLogger.getResourceBundle());
        assertSame(r.getSourceClassName(), "sourceClass");
        assertSame(r.getSourceMethodName(), "sourceMethod");
        assertSame(r.getLevel(), Level.FINER);
        assertSame(r.getParameters()[0], params[0]);
        assertSame(r.getParameters()[1], params[1]);
        assertEquals(2, r.getParameters().length);
        assertSame(r.getThrown(), null);
        this.sharedLogger.setLevel(Level.FINE);
        this.sharedLogger.entering("sourceClass", "sourceMethod", params);
        assertTrue(CallVerificationStack.getInstance().empty());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies entering(String, String, Object[]) with null class name and method name and empty parameter array.",
        method = "entering",
        args = {java.lang.String.class, java.lang.String.class, java.lang.Object[].class}
    )
    public void testEntering_StringStringObjects_NullEmpty() {
        Logger child = new MockLogger("childLogger", null);
        Logger parent = new MockLogger("parentLogger", VALID_RESOURCE_BUNDLE2);
        child.addHandler(new MockHandler());
        child.setParent(parent);
        child.setLevel(Level.FINER);
        child.entering(null, null, new Object[0]);
        LogRecord r = (LogRecord) CallVerificationStack.getInstance().pop();
        assertTrue(CallVerificationStack.getInstance().empty());
        assertSame(r.getLoggerName(), child.getName());
        assertEquals(r.getMessage(), "ENTRY");
        assertSame(r.getResourceBundleName(), parent.getResourceBundleName());
        assertSame(r.getResourceBundle(), parent.getResourceBundle());
        assertSame(r.getSourceClassName(), null);
        assertSame(r.getSourceMethodName(), null);
        assertSame(r.getLevel(), Level.FINER);
        assertEquals(0, r.getParameters().length);
        assertSame(r.getThrown(), null);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies  entering(String, String, Object[]) with null values with appropriate logging level set.",
        method = "entering",
        args = {java.lang.String.class, java.lang.String.class, java.lang.Object[].class}
    )
    public void testEntering_StringStringObjects_Null() {
        sharedLogger.setLevel(Level.FINER);
        sharedLogger.entering(null, null, (Object[]) null);
        LogRecord r = (LogRecord) CallVerificationStack.getInstance().pop();
        assertTrue(CallVerificationStack.getInstance().empty());
        assertSame(sharedLogger.getName(), r.getLoggerName());
        assertEquals("ENTRY", r.getMessage());
        assertSame(sharedLogger.getResourceBundleName(), r
                .getResourceBundleName());
        assertSame(sharedLogger.getResourceBundle(), r.getResourceBundle());
        assertNull(r.getSourceClassName());
        assertNull(r.getSourceMethodName());
        assertSame(Level.FINER, r.getLevel());
        assertNull(r.getParameters());
        assertNull(r.getThrown());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies entering(String, String, Object[]) with null values with inappropriate logging level set.",
        method = "entering",
        args = {java.lang.String.class, java.lang.String.class, java.lang.Object[].class}
    )
    public void testEntering_StringStringObjects_NullDisabled() {
        this.sharedLogger.setLevel(Level.FINE);
        this.sharedLogger.entering(null, null, (Object[]) null);
        assertTrue(CallVerificationStack.getInstance().empty());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies exiting(String, String) with normal values.",
        method = "exiting",
        args = {java.lang.String.class, java.lang.String.class}
    )
    public void testExiting_StringString_Normal() {
        this.sharedLogger.setLevel(Level.FINER);
        this.sharedLogger.exiting("sourceClass", "sourceMethod");
        LogRecord r = (LogRecord) CallVerificationStack.getInstance().pop();
        assertTrue(CallVerificationStack.getInstance().empty());
        assertSame(r.getLoggerName(), this.sharedLogger.getName());
        assertEquals(r.getMessage(), "RETURN");
        assertSame(r.getResourceBundleName(), this.sharedLogger
                .getResourceBundleName());
        assertSame(r.getResourceBundle(), this.sharedLogger.getResourceBundle());
        assertSame(r.getSourceClassName(), "sourceClass");
        assertSame(r.getSourceMethodName(), "sourceMethod");
        assertSame(r.getLevel(), Level.FINER);
        assertNull(r.getParameters());
        assertSame(r.getThrown(), null);
        this.sharedLogger.setLevel(Level.FINE);
        this.sharedLogger.exiting("sourceClass", "sourceMethod");
        assertTrue(CallVerificationStack.getInstance().empty());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies exiting(String, String) with null values.",
        method = "exiting",
        args = {java.lang.String.class, java.lang.String.class}
    )
    public void testExiting_StringString_Null() {
        Logger child = new MockLogger("childLogger", null);
        Logger parent = new MockLogger("parentLogger", VALID_RESOURCE_BUNDLE2);
        child.addHandler(new MockHandler());
        child.setParent(parent);
        child.setLevel(Level.FINER);
        child.exiting(null, null);
        LogRecord r = (LogRecord) CallVerificationStack.getInstance().pop();
        assertTrue(CallVerificationStack.getInstance().empty());
        assertSame(r.getLoggerName(), child.getName());
        assertEquals(r.getMessage(), "RETURN");
        assertSame(r.getResourceBundleName(), parent.getResourceBundleName());
        assertSame(r.getResourceBundle(), parent.getResourceBundle());
        assertSame(r.getSourceClassName(), null);
        assertSame(r.getSourceMethodName(), null);
        assertSame(r.getLevel(), Level.FINER);
        assertSame(r.getParameters(), null);
        assertSame(r.getThrown(), null);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies exiting(String, String, Object) with normal values.",
        method = "exiting",
        args = {java.lang.String.class, java.lang.String.class, java.lang.Object.class}
    )
    public void testExiting_StringStringObject_Normal() {
        Object param = new Object();
        this.sharedLogger.setLevel(Level.FINER);
        this.sharedLogger.exiting("sourceClass", "sourceMethod", param);
        LogRecord r = (LogRecord) CallVerificationStack.getInstance().pop();
        assertTrue(CallVerificationStack.getInstance().empty());
        assertSame(r.getLoggerName(), this.sharedLogger.getName());
        assertEquals(r.getMessage(), "RETURN {0}");
        assertSame(r.getResourceBundleName(), this.sharedLogger
                .getResourceBundleName());
        assertSame(r.getResourceBundle(), this.sharedLogger.getResourceBundle());
        assertSame(r.getSourceClassName(), "sourceClass");
        assertSame(r.getSourceMethodName(), "sourceMethod");
        assertSame(r.getLevel(), Level.FINER);
        assertSame(r.getParameters()[0], param);
        assertEquals(1, r.getParameters().length);
        assertSame(r.getThrown(), null);
        this.sharedLogger.setLevel(Level.FINE);
        this.sharedLogger.exiting("sourceClass", "sourceMethod", param);
        assertTrue(CallVerificationStack.getInstance().empty());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies exiting(String, String, Object) with null values.",
        method = "exiting",
        args = {java.lang.String.class, java.lang.String.class, java.lang.Object.class}
    )
    public void testExiting_StringStringObject_Null() {
        Logger child = new MockLogger("childLogger", null);
        Logger parent = new MockLogger("parentLogger", VALID_RESOURCE_BUNDLE2);
        child.addHandler(new MockHandler());
        child.setParent(parent);
        child.setLevel(Level.FINER);
        child.exiting(null, null, (Object) null);
        LogRecord r = (LogRecord) CallVerificationStack.getInstance().pop();
        assertTrue(CallVerificationStack.getInstance().empty());
        assertSame(r.getLoggerName(), child.getName());
        assertEquals(r.getMessage(), "RETURN {0}");
        assertSame(r.getResourceBundleName(), parent.getResourceBundleName());
        assertSame(r.getResourceBundle(), parent.getResourceBundle());
        assertSame(r.getSourceClassName(), null);
        assertSame(r.getSourceMethodName(), null);
        assertSame(r.getLevel(), Level.FINER);
        assertEquals(r.getParameters().length, 1);
        assertNull(r.getParameters()[0]);
        assertSame(r.getThrown(), null);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "config",
        args = {java.lang.String.class}
    )
    public void testConfig_Normal() {
        this.sharedLogger.setLevel(Level.CONFIG);
        this.sharedLogger.config("config msg");
        LogRecord r = (LogRecord) CallVerificationStack.getInstance().pop();
        assertTrue(CallVerificationStack.getInstance().empty());
        assertSame(r.getLoggerName(), this.sharedLogger.getName());
        assertEquals(r.getMessage(), "config msg");
        assertSame(r.getResourceBundleName(), this.sharedLogger
                .getResourceBundleName());
        assertSame(r.getResourceBundle(), this.sharedLogger.getResourceBundle());
        assertSame(r.getSourceClassName(), null);
        assertSame(r.getSourceMethodName(), null);
        assertSame(r.getLevel(), Level.CONFIG);
        assertNull(r.getParameters());
        assertSame(r.getThrown(), null);
        this.sharedLogger.setLevel(Level.INFO);
        this.sharedLogger.config("config again");
        assertTrue(CallVerificationStack.getInstance().empty());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies null as a parameter.",
        method = "config",
        args = {java.lang.String.class}
    )
    public void testConfig_Null() {
        Logger child = new MockLogger("childLogger", null);
        Logger parent = new MockLogger("parentLogger", VALID_RESOURCE_BUNDLE2);
        child.addHandler(new MockHandler());
        child.setParent(parent);
        child.setLevel(Level.CONFIG);
        child.config(null);
        LogRecord r = (LogRecord) CallVerificationStack.getInstance().pop();
        assertTrue(CallVerificationStack.getInstance().empty());
        assertSame(r.getLoggerName(), child.getName());
        assertNull(r.getMessage());
        assertSame(r.getResourceBundleName(), parent.getResourceBundleName());
        assertSame(r.getResourceBundle(), parent.getResourceBundle());
        assertSame(r.getSourceClassName(), null);
        assertSame(r.getSourceMethodName(), null);
        assertSame(r.getLevel(), Level.CONFIG);
        assertNull(r.getParameters());
        assertSame(r.getThrown(), null);
        this.sharedLogger.setLevel(Level.INFO);
        this.sharedLogger.config(null);
        assertTrue(CallVerificationStack.getInstance().empty());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "fine",
        args = {java.lang.String.class}
    )
    public void testFine_Normal() {
        this.sharedLogger.setLevel(Level.FINE);
        this.sharedLogger.fine("fine msg");
        LogRecord r = (LogRecord) CallVerificationStack.getInstance().pop();
        assertTrue(CallVerificationStack.getInstance().empty());
        assertSame(r.getLoggerName(), this.sharedLogger.getName());
        assertSame(r.getResourceBundle(), this.sharedLogger.getResourceBundle());
        assertEquals(r.getMessage(), "fine msg");
        assertSame(r.getResourceBundleName(), this.sharedLogger
                .getResourceBundleName());
        assertSame(r.getSourceClassName(), null);
        assertSame(r.getSourceMethodName(), null);
        assertSame(r.getLevel(), Level.FINE);
        assertNull(r.getParameters());
        assertSame(r.getThrown(), null);
        this.sharedLogger.setLevel(Level.CONFIG);
        this.sharedLogger.fine("fine again");
        assertTrue(CallVerificationStack.getInstance().empty());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies fine(String) with null values.",
        method = "fine",
        args = {java.lang.String.class}
    )
    public void testFine_Null() {
        Logger child = new MockLogger("childLogger", null);
        Logger parent = new MockLogger("parentLogger", VALID_RESOURCE_BUNDLE2);
        child.addHandler(new MockHandler());
        child.setParent(parent);
        child.setLevel(Level.FINE);
        child.fine(null);
        LogRecord r = (LogRecord) CallVerificationStack.getInstance().pop();
        assertTrue(CallVerificationStack.getInstance().empty());
        assertSame(r.getLoggerName(), child.getName());
        assertNull(r.getMessage());
        assertSame(r.getResourceBundleName(), parent.getResourceBundleName());
        assertSame(r.getResourceBundle(), parent.getResourceBundle());
        assertSame(r.getSourceClassName(), null);
        assertSame(r.getSourceMethodName(), null);
        assertSame(r.getLevel(), Level.FINE);
        assertNull(r.getParameters());
        assertSame(r.getThrown(), null);
        this.sharedLogger.setLevel(Level.CONFIG);
        this.sharedLogger.fine(null);
        assertTrue(CallVerificationStack.getInstance().empty());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies finer(String) with normal values.",
        method = "finer",
        args = {java.lang.String.class}
    )
    public void testFiner_Normal() {
        this.sharedLogger.setLevel(Level.FINER);
        this.sharedLogger.finer("finer msg");
        LogRecord r = (LogRecord) CallVerificationStack.getInstance().pop();
        assertTrue(CallVerificationStack.getInstance().empty());
        assertSame(r.getLoggerName(), this.sharedLogger.getName());
        assertEquals(r.getMessage(), "finer msg");
        assertSame(r.getResourceBundleName(), this.sharedLogger
                .getResourceBundleName());
        assertSame(r.getResourceBundle(), this.sharedLogger.getResourceBundle());
        assertSame(r.getSourceClassName(), null);
        assertSame(r.getSourceMethodName(), null);
        assertSame(r.getLevel(), Level.FINER);
        assertNull(r.getParameters());
        assertSame(r.getThrown(), null);
        this.sharedLogger.setLevel(Level.FINE);
        this.sharedLogger.finer("finer again");
        assertTrue(CallVerificationStack.getInstance().empty());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies finer(String) with null values.",
        method = "finer",
        args = {java.lang.String.class}
    )
    public void testFiner_Null() {
        Logger child = new MockLogger("childLogger", null);
        Logger parent = new MockLogger("parentLogger", VALID_RESOURCE_BUNDLE2);
        child.addHandler(new MockHandler());
        child.setParent(parent);
        child.setLevel(Level.FINER);
        child.finer(null);
        LogRecord r = (LogRecord) CallVerificationStack.getInstance().pop();
        assertTrue(CallVerificationStack.getInstance().empty());
        assertSame(r.getLoggerName(), child.getName());
        assertNull(r.getMessage());
        assertSame(r.getResourceBundleName(), parent.getResourceBundleName());
        assertSame(r.getResourceBundle(), parent.getResourceBundle());
        assertSame(r.getSourceClassName(), null);
        assertSame(r.getSourceMethodName(), null);
        assertSame(r.getLevel(), Level.FINER);
        assertNull(r.getParameters());
        assertSame(r.getThrown(), null);
        this.sharedLogger.setLevel(Level.FINE);
        this.sharedLogger.finer(null);
        assertTrue(CallVerificationStack.getInstance().empty());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies finest(String) with normal values.",
        method = "finest",
        args = {java.lang.String.class}
    )
    public void testFinest_Normal() {
        this.sharedLogger.setLevel(Level.FINEST);
        this.sharedLogger.finest("finest msg");
        LogRecord r = (LogRecord) CallVerificationStack.getInstance().pop();
        assertTrue(CallVerificationStack.getInstance().empty());
        assertSame(r.getLoggerName(), this.sharedLogger.getName());
        assertEquals(r.getMessage(), "finest msg");
        assertSame(r.getResourceBundleName(), this.sharedLogger
                .getResourceBundleName());
        assertSame(r.getResourceBundle(), this.sharedLogger.getResourceBundle());
        assertSame(r.getSourceClassName(), null);
        assertSame(r.getSourceMethodName(), null);
        assertSame(r.getLevel(), Level.FINEST);
        assertNull(r.getParameters());
        assertSame(r.getThrown(), null);
        this.sharedLogger.setLevel(Level.FINER);
        this.sharedLogger.finest("finest again");
        assertTrue(CallVerificationStack.getInstance().empty());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies finest(String) with null values.",
        method = "finest",
        args = {java.lang.String.class}
    )
    public void testFinest_Null() {
        Logger child = new MockLogger("childLogger", null);
        Logger parent = new MockLogger("parentLogger", VALID_RESOURCE_BUNDLE2);
        child.addHandler(new MockHandler());
        child.setParent(parent);
        child.setLevel(Level.FINEST);
        child.finest(null);
        LogRecord r = (LogRecord) CallVerificationStack.getInstance().pop();
        assertTrue(CallVerificationStack.getInstance().empty());
        assertSame(r.getLoggerName(), child.getName());
        assertNull(r.getMessage());
        assertSame(r.getResourceBundleName(), parent.getResourceBundleName());
        assertSame(r.getResourceBundle(), parent.getResourceBundle());
        assertSame(r.getSourceClassName(), null);
        assertSame(r.getSourceMethodName(), null);
        assertSame(r.getLevel(), Level.FINEST);
        assertNull(r.getParameters());
        assertSame(r.getThrown(), null);
        this.sharedLogger.setLevel(Level.FINER);
        this.sharedLogger.finest(null);
        assertTrue(CallVerificationStack.getInstance().empty());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "info",
        args = {java.lang.String.class}
    )
    public void testInfo_Normal() {
        this.sharedLogger.setLevel(Level.INFO);
        this.sharedLogger.info("info msg");
        LogRecord r = (LogRecord) CallVerificationStack.getInstance().pop();
        assertTrue(CallVerificationStack.getInstance().empty());
        assertSame(r.getLoggerName(), this.sharedLogger.getName());
        assertEquals(r.getMessage(), "info msg");
        assertSame(r.getResourceBundleName(), this.sharedLogger
                .getResourceBundleName());
        assertSame(r.getResourceBundle(), this.sharedLogger.getResourceBundle());
        assertSame(r.getSourceClassName(), null);
        assertSame(r.getSourceMethodName(), null);
        assertSame(r.getLevel(), Level.INFO);
        assertNull(r.getParameters());
        assertSame(r.getThrown(), null);
        this.sharedLogger.setLevel(Level.WARNING);
        this.sharedLogger.info("info again");
        assertTrue(CallVerificationStack.getInstance().empty());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "info",
        args = {java.lang.String.class}
    )
    public void testInfo_Null() {
        Logger child = new MockLogger("childLogger", null);
        Logger parent = new MockLogger("parentLogger", VALID_RESOURCE_BUNDLE2);
        child.addHandler(new MockHandler());
        child.setParent(parent);
        child.setLevel(Level.INFO);
        child.info(null);
        LogRecord r = (LogRecord) CallVerificationStack.getInstance().pop();
        assertTrue(CallVerificationStack.getInstance().empty());
        assertSame(r.getLoggerName(), child.getName());
        assertNull(r.getMessage());
        assertSame(r.getResourceBundleName(), parent.getResourceBundleName());
        assertSame(r.getResourceBundle(), parent.getResourceBundle());
        assertSame(r.getSourceClassName(), null);
        assertSame(r.getSourceMethodName(), null);
        assertSame(r.getLevel(), Level.INFO);
        assertNull(r.getParameters());
        assertSame(r.getThrown(), null);
        this.sharedLogger.setLevel(Level.WARNING);
        this.sharedLogger.info(null);
        assertTrue(CallVerificationStack.getInstance().empty());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "warning",
        args = {java.lang.String.class}
    )
    public void testWarning_Normal() {
        this.sharedLogger.setLevel(Level.WARNING);
        this.sharedLogger.warning("warning msg");
        LogRecord r = (LogRecord) CallVerificationStack.getInstance().pop();
        assertTrue(CallVerificationStack.getInstance().empty());
        assertSame(r.getLoggerName(), this.sharedLogger.getName());
        assertEquals(r.getMessage(), "warning msg");
        assertSame(r.getResourceBundleName(), this.sharedLogger
                .getResourceBundleName());
        assertSame(r.getResourceBundle(), this.sharedLogger.getResourceBundle());
        assertSame(r.getSourceClassName(), null);
        assertSame(r.getSourceMethodName(), null);
        assertSame(r.getLevel(), Level.WARNING);
        assertNull(r.getParameters());
        assertSame(r.getThrown(), null);
        this.sharedLogger.setLevel(Level.SEVERE);
        this.sharedLogger.warning("warning again");
        assertTrue(CallVerificationStack.getInstance().empty());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "warning",
        args = {java.lang.String.class}
    )
    public void testWarning_Null() {
        Logger child = new MockLogger("childLogger", null);
        Logger parent = new MockLogger("parentLogger", VALID_RESOURCE_BUNDLE2);
        child.addHandler(new MockHandler());
        child.setParent(parent);
        child.setLevel(Level.WARNING);
        child.warning(null);
        LogRecord r = (LogRecord) CallVerificationStack.getInstance().pop();
        assertTrue(CallVerificationStack.getInstance().empty());
        assertSame(r.getLoggerName(), child.getName());
        assertNull(r.getMessage());
        assertSame(r.getResourceBundleName(), parent.getResourceBundleName());
        assertSame(r.getResourceBundle(), parent.getResourceBundle());
        assertSame(r.getSourceClassName(), null);
        assertSame(r.getSourceMethodName(), null);
        assertSame(r.getLevel(), Level.WARNING);
        assertNull(r.getParameters());
        assertSame(r.getThrown(), null);
        this.sharedLogger.setLevel(Level.SEVERE);
        this.sharedLogger.warning(null);
        assertTrue(CallVerificationStack.getInstance().empty());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "severe",
        args = {java.lang.String.class}
    )
    public void testSevere_Normal() {
        this.sharedLogger.setLevel(Level.SEVERE);
        this.sharedLogger.severe("severe msg");
        LogRecord r = (LogRecord) CallVerificationStack.getInstance().pop();
        assertTrue(CallVerificationStack.getInstance().empty());
        assertSame(r.getLoggerName(), this.sharedLogger.getName());
        assertEquals(r.getMessage(), "severe msg");
        assertSame(r.getResourceBundleName(), this.sharedLogger
                .getResourceBundleName());
        assertSame(r.getResourceBundle(), this.sharedLogger.getResourceBundle());
        assertSame(r.getSourceClassName(), null);
        assertSame(r.getSourceMethodName(), null);
        assertSame(r.getLevel(), Level.SEVERE);
        assertNull(r.getParameters());
        assertSame(r.getThrown(), null);
        this.sharedLogger.setLevel(Level.OFF);
        this.sharedLogger.severe("severe again");
        assertTrue(CallVerificationStack.getInstance().empty());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "severe",
        args = {java.lang.String.class}
    )
    public void testSevere_Null() {
        Logger child = new MockLogger("childLogger", null);
        Logger parent = new MockLogger("parentLogger", VALID_RESOURCE_BUNDLE2);
        child.addHandler(new MockHandler());
        child.setParent(parent);
        child.setLevel(Level.SEVERE);
        child.severe(null);
        LogRecord r = (LogRecord) CallVerificationStack.getInstance().pop();
        assertTrue(CallVerificationStack.getInstance().empty());
        assertSame(r.getLoggerName(), child.getName());
        assertNull(r.getMessage());
        assertSame(r.getResourceBundleName(), parent.getResourceBundleName());
        assertSame(r.getResourceBundle(), parent.getResourceBundle());
        assertSame(r.getSourceClassName(), null);
        assertSame(r.getSourceMethodName(), null);
        assertSame(r.getLevel(), Level.SEVERE);
        assertNull(r.getParameters());
        assertSame(r.getThrown(), null);
        this.sharedLogger.setLevel(Level.OFF);
        this.sharedLogger.severe(null);
        assertTrue(CallVerificationStack.getInstance().empty());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "log",
        args = {java.util.logging.Level.class, java.lang.String.class}
    )
    public void testLog_LevelString_Normal() {
        this.sharedLogger.setLevel(Level.INFO);
        this.sharedLogger.log(Level.INFO, "log(Level, String) msg");
        LogRecord r = (LogRecord) CallVerificationStack.getInstance().pop();
        assertTrue(CallVerificationStack.getInstance().empty());
        assertSame(r.getLoggerName(), this.sharedLogger.getName());
        assertEquals(r.getMessage(), "log(Level, String) msg");
        assertSame(r.getResourceBundleName(), this.sharedLogger
                .getResourceBundleName());
        assertSame(r.getResourceBundle(), this.sharedLogger.getResourceBundle());
        assertSame(r.getSourceClassName(), null);
        assertSame(r.getSourceMethodName(), null);
        assertSame(r.getLevel(), Level.INFO);
        assertNull(r.getParameters());
        assertSame(r.getThrown(), null);
        this.sharedLogger.log(Level.CONFIG, "log(Level, String) msg");
        assertTrue(CallVerificationStack.getInstance().empty());
        this.sharedLogger.setLevel(Level.OFF);
        this.sharedLogger.log(Level.OFF, "log(Level, String) msg");
        assertTrue(CallVerificationStack.getInstance().empty());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "log",
        args = {java.util.logging.Level.class, java.lang.String.class}
    )
    public void testLog_LevelString_NullMsg() {
        Logger child = new MockLogger("childLogger", null);
        Logger parent = new MockLogger("parentLogger", VALID_RESOURCE_BUNDLE2);
        child.addHandler(new MockHandler());
        child.setParent(parent);
        child.setLevel(Level.INFO);
        child.log(Level.INFO, null);
        LogRecord r = (LogRecord) CallVerificationStack.getInstance().pop();
        assertTrue(CallVerificationStack.getInstance().empty());
        assertSame(r.getLoggerName(), child.getName());
        assertNull(r.getMessage());
        assertSame(r.getResourceBundleName(), parent.getResourceBundleName());
        assertSame(r.getResourceBundle(), parent.getResourceBundle());
        assertSame(r.getSourceClassName(), null);
        assertSame(r.getSourceMethodName(), null);
        assertSame(r.getLevel(), Level.INFO);
        assertNull(r.getParameters());
        assertSame(r.getThrown(), null);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "log",
        args = {java.util.logging.Level.class, java.lang.String.class}
    )
    public void testLog_LevelString_NullLevel() {
        try {
            this.sharedLogger.log(null, "log(Level, String) msg");
            fail("Should throw NullPointerException!");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "log",
        args = {java.util.logging.Level.class, java.lang.String.class, java.lang.Object.class}
    )
    public void testLog_LevelStringObject_Normal() {
        Object param = new Object();
        this.sharedLogger.setLevel(Level.INFO);
        this.sharedLogger.log(Level.INFO, "log(Level, String, Object) msg",
                param);
        LogRecord r = (LogRecord) CallVerificationStack.getInstance().pop();
        assertTrue(CallVerificationStack.getInstance().empty());
        assertSame(r.getLoggerName(), this.sharedLogger.getName());
        assertEquals(r.getMessage(), "log(Level, String, Object) msg");
        assertSame(r.getResourceBundleName(), this.sharedLogger
                .getResourceBundleName());
        assertSame(r.getResourceBundle(), this.sharedLogger.getResourceBundle());
        assertSame(r.getSourceClassName(), null);
        assertSame(r.getSourceMethodName(), null);
        assertSame(r.getLevel(), Level.INFO);
        assertEquals(1, r.getParameters().length);
        assertSame(param, r.getParameters()[0]);
        assertSame(r.getThrown(), null);
        this.sharedLogger.log(Level.CONFIG, "log(Level, String, Object) msg",
                param);
        assertTrue(CallVerificationStack.getInstance().empty());
        this.sharedLogger.setLevel(Level.OFF);
        this.sharedLogger.log(Level.OFF, "log(Level, String, Object) msg",
                param);
        assertTrue(CallVerificationStack.getInstance().empty());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "log",
        args = {java.util.logging.Level.class, java.lang.String.class, java.lang.Object.class}
    )
    public void testLog_LevelStringObject_NullMsgObj() {
        Logger child = new MockLogger("childLogger", null);
        Logger parent = new MockLogger("parentLogger", VALID_RESOURCE_BUNDLE2);
        child.addHandler(new MockHandler());
        child.setParent(parent);
        child.setLevel(Level.INFO);
        child.log(Level.INFO, null, (Object) null);
        LogRecord r = (LogRecord) CallVerificationStack.getInstance().pop();
        assertTrue(CallVerificationStack.getInstance().empty());
        assertSame(r.getLoggerName(), child.getName());
        assertNull(r.getMessage());
        assertSame(r.getResourceBundleName(), parent.getResourceBundleName());
        assertSame(r.getResourceBundle(), parent.getResourceBundle());
        assertSame(r.getSourceClassName(), null);
        assertSame(r.getSourceMethodName(), null);
        assertSame(r.getLevel(), Level.INFO);
        assertEquals(1, r.getParameters().length);
        assertNull(r.getParameters()[0]);
        assertSame(r.getThrown(), null);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "log",
        args = {java.util.logging.Level.class, java.lang.String.class, java.lang.Object.class}
    )
    public void testLog_LevelStringObject_NullLevel() {
        try {
            this.sharedLogger.log(null, "log(Level, String, Object) msg",
                    new Object());
            fail("Should throw NullPointerException!");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "log",
        args = {java.util.logging.Level.class, java.lang.String.class, java.lang.Object[].class}
    )
    public void testLog_LevelStringObjects_Normal() {
        Object[] params = new Object[2];
        params[0] = new Object();
        params[1] = new Object();
        this.sharedLogger.setLevel(Level.INFO);
        this.sharedLogger.log(Level.INFO, "log(Level, String, Object[]) msg",
                params);
        LogRecord r = (LogRecord) CallVerificationStack.getInstance().pop();
        assertTrue(CallVerificationStack.getInstance().empty());
        assertSame(r.getLoggerName(), this.sharedLogger.getName());
        assertEquals(r.getMessage(), "log(Level, String, Object[]) msg");
        assertSame(r.getResourceBundleName(), this.sharedLogger
                .getResourceBundleName());
        assertSame(r.getResourceBundle(), this.sharedLogger.getResourceBundle());
        assertSame(r.getSourceClassName(), null);
        assertSame(r.getSourceMethodName(), null);
        assertSame(r.getLevel(), Level.INFO);
        assertEquals(2, r.getParameters().length);
        assertSame(params[0], r.getParameters()[0]);
        assertSame(params[1], r.getParameters()[1]);
        assertSame(r.getThrown(), null);
        this.sharedLogger.log(Level.CONFIG, "log(Level, String, Object[]) msg",
                params);
        assertTrue(CallVerificationStack.getInstance().empty());
        this.sharedLogger.setLevel(Level.OFF);
        this.sharedLogger.log(Level.OFF, "log(Level, String, Object[]) msg",
                params);
        assertTrue(CallVerificationStack.getInstance().empty());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "log",
        args = {java.util.logging.Level.class, java.lang.String.class, java.lang.Object[].class}
    )
    public void testLog_LevelStringObjects_NullMsgObj() {
        Logger child = new MockLogger("childLogger", null);
        Logger parent = new MockLogger("parentLogger", VALID_RESOURCE_BUNDLE2);
        child.addHandler(new MockHandler());
        child.setParent(parent);
        child.setLevel(Level.INFO);
        child.log(Level.INFO, null, (Object[]) null);
        LogRecord r = (LogRecord) CallVerificationStack.getInstance().pop();
        assertTrue(CallVerificationStack.getInstance().empty());
        assertSame(r.getLoggerName(), child.getName());
        assertNull(r.getMessage());
        assertSame(r.getResourceBundleName(), parent.getResourceBundleName());
        assertSame(r.getResourceBundle(), parent.getResourceBundle());
        assertSame(r.getSourceClassName(), null);
        assertSame(r.getSourceMethodName(), null);
        assertSame(r.getLevel(), Level.INFO);
        assertNull(r.getParameters());
        assertSame(r.getThrown(), null);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "log",
        args = {java.util.logging.Level.class, java.lang.String.class, java.lang.Object[].class}
    )
    public void testLog_LevelStringObjects_NullLevel() {
        try {
            this.sharedLogger.log(null, "log(Level, String, Object[]) msg",
                    new Object[0]);
            fail("Should throw NullPointerException!");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "log",
        args = {java.util.logging.Level.class, java.lang.String.class, java.lang.Throwable.class}
    )
    public void testLog_LevelStringThrowable_Normal() {
        Throwable t = new Throwable();
        this.sharedLogger.setLevel(Level.INFO);
        this.sharedLogger.log(Level.INFO, "log(Level, String, Throwable) msg",
                t);
        LogRecord r = (LogRecord) CallVerificationStack.getInstance().pop();
        assertTrue(CallVerificationStack.getInstance().empty());
        assertSame(r.getLoggerName(), this.sharedLogger.getName());
        assertEquals(r.getMessage(), "log(Level, String, Throwable) msg");
        assertSame(r.getResourceBundleName(), this.sharedLogger
                .getResourceBundleName());
        assertSame(r.getResourceBundle(), this.sharedLogger.getResourceBundle());
        assertSame(r.getSourceClassName(), null);
        assertSame(r.getSourceMethodName(), null);
        assertSame(r.getLevel(), Level.INFO);
        assertNull(r.getParameters());
        assertSame(r.getThrown(), t);
        this.sharedLogger.log(Level.CONFIG,
                "log(Level, String, Throwable) msg", t);
        assertTrue(CallVerificationStack.getInstance().empty());
        this.sharedLogger.setLevel(Level.OFF);
        this.sharedLogger
                .log(Level.OFF, "log(Level, String, Throwable) msg", t);
        assertTrue(CallVerificationStack.getInstance().empty());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "log",
        args = {java.util.logging.Level.class, java.lang.String.class, java.lang.Throwable.class}
    )
    public void testLog_LevelStringThrowable_Null() {
        Logger child = new MockLogger("childLogger", null);
        Logger parent = new MockLogger("parentLogger", VALID_RESOURCE_BUNDLE2);
        child.addHandler(new MockHandler());
        child.setParent(parent);
        child.setLevel(Level.INFO);
        child.log(Level.INFO, null, (Throwable) null);
        LogRecord r = (LogRecord) CallVerificationStack.getInstance().pop();
        assertTrue(CallVerificationStack.getInstance().empty());
        assertSame(r.getLoggerName(), child.getName());
        assertNull(r.getMessage());
        assertSame(r.getResourceBundleName(), parent.getResourceBundleName());
        assertSame(r.getResourceBundle(), parent.getResourceBundle());
        assertSame(r.getSourceClassName(), null);
        assertSame(r.getSourceMethodName(), null);
        assertSame(r.getLevel(), Level.INFO);
        assertNull(r.getParameters());
        assertSame(r.getThrown(), null);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "log",
        args = {java.util.logging.Level.class, java.lang.String.class, java.lang.Throwable.class}
    )
    public void testLog_LevelStringThrowable_NullLevel() {
        try {
            this.sharedLogger.log(null, "log(Level, String, Throwable) msg",
                    new Throwable());
            fail("Should throw NullPointerException!");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "logp",
        args = {java.util.logging.Level.class, java.lang.String.class, java.lang.String.class, java.lang.String.class}
    )
    public void testLogp_LevelStringStringString_Normal() {
        this.sharedLogger.setLevel(Level.INFO);
        this.sharedLogger.logp(Level.INFO, "sourceClass", "sourceMethod",
                "logp(Level, String, String, String) msg");
        LogRecord r = (LogRecord) CallVerificationStack.getInstance().pop();
        assertTrue(CallVerificationStack.getInstance().empty());
        assertSame(r.getLoggerName(), this.sharedLogger.getName());
        assertEquals(r.getMessage(), "logp(Level, String, String, String) msg");
        assertSame(r.getResourceBundleName(), this.sharedLogger
                .getResourceBundleName());
        assertSame(r.getResourceBundle(), this.sharedLogger.getResourceBundle());
        assertSame(r.getSourceClassName(), "sourceClass");
        assertSame(r.getSourceMethodName(), "sourceMethod");
        assertSame(r.getLevel(), Level.INFO);
        assertNull(r.getParameters());
        assertSame(r.getThrown(), null);
        this.sharedLogger.logp(Level.CONFIG, "sourceClass", "sourceMethod",
                "logp(Level, String, String, String) msg");
        assertTrue(CallVerificationStack.getInstance().empty());
        this.sharedLogger.setLevel(Level.OFF);
        this.sharedLogger.logp(Level.OFF, "sourceClass", "sourceMethod",
                "logp(Level, String, String, String) msg");
        assertTrue(CallVerificationStack.getInstance().empty());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "logp",
        args = {java.util.logging.Level.class, java.lang.String.class, java.lang.String.class, java.lang.String.class}
    )
    public void testLogp_LevelStringStringString_NullMsg() {
        Logger child = new MockLogger("childLogger", null);
        Logger parent = new MockLogger("parentLogger", VALID_RESOURCE_BUNDLE2);
        child.addHandler(new MockHandler());
        child.setParent(parent);
        child.setLevel(Level.INFO);
        child.logp(Level.INFO, null, null, null);
        LogRecord r = (LogRecord) CallVerificationStack.getInstance().pop();
        assertTrue(CallVerificationStack.getInstance().empty());
        assertSame(r.getLoggerName(), child.getName());
        assertNull(r.getMessage());
        assertSame(r.getResourceBundleName(), parent.getResourceBundleName());
        assertSame(r.getResourceBundle(), parent.getResourceBundle());
        assertSame(r.getSourceClassName(), null);
        assertSame(r.getSourceMethodName(), null);
        assertSame(r.getLevel(), Level.INFO);
        assertNull(r.getParameters());
        assertSame(r.getThrown(), null);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "logp",
        args = {java.util.logging.Level.class, java.lang.String.class, java.lang.String.class, java.lang.String.class}
    )
    public void testLogp_LevelStringStringString_NullLevel() {
        try {
            this.sharedLogger.logp(null, "sourceClass", "sourceMethod",
                    "logp(Level, String, String, String) msg");
            fail("Should throw NullPointerException!");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "logp",
        args = {java.util.logging.Level.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.Object.class}
    )
    public void testLogp_LevelStringStringStringObject_Normal() {
        Object param = new Object();
        this.sharedLogger.setLevel(Level.INFO);
        this.sharedLogger.logp(Level.INFO, "sourceClass", "sourceMethod",
                "logp(Level, String, String, String, Object) msg", param);
        LogRecord r = (LogRecord) CallVerificationStack.getInstance().pop();
        assertTrue(CallVerificationStack.getInstance().empty());
        assertSame(r.getLoggerName(), this.sharedLogger.getName());
        assertEquals(r.getMessage(),
                "logp(Level, String, String, String, Object) msg");
        assertSame(r.getResourceBundleName(), this.sharedLogger
                .getResourceBundleName());
        assertSame(r.getResourceBundle(), this.sharedLogger.getResourceBundle());
        assertSame(r.getSourceClassName(), "sourceClass");
        assertSame(r.getSourceMethodName(), "sourceMethod");
        assertSame(r.getLevel(), Level.INFO);
        assertEquals(1, r.getParameters().length);
        assertSame(param, r.getParameters()[0]);
        assertSame(r.getThrown(), null);
        this.sharedLogger.logp(Level.CONFIG, "sourceClass", "sourceMethod",
                "logp(Level, String, String, String, Object) msg", param);
        assertTrue(CallVerificationStack.getInstance().empty());
        this.sharedLogger.setLevel(Level.OFF);
        this.sharedLogger.logp(Level.OFF, "sourceClass", "sourceMethod",
                "logp(Level, String, String, String, Object) msg", param);
        assertTrue(CallVerificationStack.getInstance().empty());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "logp",
        args = {java.util.logging.Level.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.Object.class}
    )
    public void testLogp_LevelStringStringStringObject_NullMsgObj() {
        Logger child = new MockLogger("childLogger", null);
        Logger parent = new MockLogger("parentLogger", VALID_RESOURCE_BUNDLE2);
        child.addHandler(new MockHandler());
        child.setParent(parent);
        child.setLevel(Level.INFO);
        child.logp(Level.INFO, null, null, null, (Object) null);
        LogRecord r = (LogRecord) CallVerificationStack.getInstance().pop();
        assertTrue(CallVerificationStack.getInstance().empty());
        assertSame(r.getLoggerName(), child.getName());
        assertNull(r.getMessage());
        assertSame(r.getResourceBundleName(), parent.getResourceBundleName());
        assertSame(r.getResourceBundle(), parent.getResourceBundle());
        assertSame(r.getSourceClassName(), null);
        assertSame(r.getSourceMethodName(), null);
        assertSame(r.getLevel(), Level.INFO);
        assertEquals(1, r.getParameters().length);
        assertNull(r.getParameters()[0]);
        assertSame(r.getThrown(), null);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "logp",
        args = {java.util.logging.Level.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.Object.class}
    )
    public void testLogp_LevelStringStringStringObject_NullLevel() {
        try {
            this.sharedLogger.logp(null, "sourceClass", "sourceMethod",
                    "logp(Level, String, String, String, Object) msg",
                    new Object());
            fail("Should throw NullPointerException!");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "logp",
        args = {java.util.logging.Level.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.Object[].class}
    )
    public void testLogp_LevelStringStringStringObjects_Normal() {
        Object[] params = new Object[2];
        params[0] = new Object();
        params[1] = new Object();
        this.sharedLogger.setLevel(Level.INFO);
        this.sharedLogger.logp(Level.INFO, "sourceClass", "sourceMethod",
                "logp(Level, String, String, String, Object[]) msg", params);
        LogRecord r = (LogRecord) CallVerificationStack.getInstance().pop();
        assertTrue(CallVerificationStack.getInstance().empty());
        assertSame(r.getLoggerName(), this.sharedLogger.getName());
        assertEquals(r.getMessage(),
                "logp(Level, String, String, String, Object[]) msg");
        assertSame(r.getResourceBundleName(), this.sharedLogger
                .getResourceBundleName());
        assertSame(r.getResourceBundle(), this.sharedLogger.getResourceBundle());
        assertSame(r.getSourceClassName(), "sourceClass");
        assertSame(r.getSourceMethodName(), "sourceMethod");
        assertSame(r.getLevel(), Level.INFO);
        assertEquals(2, r.getParameters().length);
        assertSame(params[0], r.getParameters()[0]);
        assertSame(params[1], r.getParameters()[1]);
        assertSame(r.getThrown(), null);
        this.sharedLogger.logp(Level.CONFIG, "sourceClass", "sourceMethod",
                "logp(Level, String, String, String, Object[]) msg", params);
        assertTrue(CallVerificationStack.getInstance().empty());
        this.sharedLogger.setLevel(Level.OFF);
        this.sharedLogger.logp(Level.OFF, "sourceClass", "sourceMethod",
                "logp(Level, String, String, String, Object[]) msg", params);
        assertTrue(CallVerificationStack.getInstance().empty());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "logp",
        args = {java.util.logging.Level.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.Object[].class}
    )
    public void testLogp_LevelStringStringStringObjects_NullMsgObj() {
        Logger child = new MockLogger("childLogger", null);
        Logger parent = new MockLogger("parentLogger", VALID_RESOURCE_BUNDLE2);
        child.addHandler(new MockHandler());
        child.setParent(parent);
        child.setLevel(Level.INFO);
        child.logp(Level.INFO, null, null, null, (Object[]) null);
        LogRecord r = (LogRecord) CallVerificationStack.getInstance().pop();
        assertTrue(CallVerificationStack.getInstance().empty());
        assertSame(r.getLoggerName(), child.getName());
        assertNull(r.getMessage());
        assertSame(r.getResourceBundleName(), parent.getResourceBundleName());
        assertSame(r.getResourceBundle(), parent.getResourceBundle());
        assertSame(r.getSourceClassName(), null);
        assertSame(r.getSourceMethodName(), null);
        assertSame(r.getLevel(), Level.INFO);
        assertNull(r.getParameters());
        assertSame(r.getThrown(), null);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "logp",
        args = {java.util.logging.Level.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.Object[].class}
    )
    public void testLogp_LevelStringStringStringObjects_NullLevel() {
        try {
            this.sharedLogger.logp(null, "sourceClass", "sourceMethod",
                    "logp(Level, String, String, String, Object[]) msg",
                    new Object[0]);
            fail("Should throw NullPointerException!");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "logp",
        args = {java.util.logging.Level.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.Throwable.class}
    )
    public void testLogp_LevelStringStringStringThrowable_Normal() {
        Throwable t = new Throwable();
        this.sharedLogger.setLevel(Level.INFO);
        this.sharedLogger.logp(Level.INFO, "sourceClass", "sourceMethod",
                "logp(Level, String, String, String, Throwable) msg", t);
        LogRecord r = (LogRecord) CallVerificationStack.getInstance().pop();
        assertTrue(CallVerificationStack.getInstance().empty());
        assertSame(r.getLoggerName(), this.sharedLogger.getName());
        assertEquals(r.getMessage(),
                "logp(Level, String, String, String, Throwable) msg");
        assertSame(r.getResourceBundleName(), this.sharedLogger
                .getResourceBundleName());
        assertSame(r.getResourceBundle(), this.sharedLogger.getResourceBundle());
        assertSame(r.getSourceClassName(), "sourceClass");
        assertSame(r.getSourceMethodName(), "sourceMethod");
        assertSame(r.getLevel(), Level.INFO);
        assertNull(r.getParameters());
        assertSame(r.getThrown(), t);
        this.sharedLogger.logp(Level.CONFIG, "sourceClass", "sourceMethod",
                "logp(Level, String, String, String, Throwable) msg", t);
        assertTrue(CallVerificationStack.getInstance().empty());
        this.sharedLogger.setLevel(Level.OFF);
        this.sharedLogger.logp(Level.OFF, "sourceClass", "sourceMethod",
                "logp(Level, String, String, String, Throwable) msg", t);
        assertTrue(CallVerificationStack.getInstance().empty());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "logp",
        args = {java.util.logging.Level.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.Throwable.class}
    )
    public void testLogp_LevelStringStringStringThrowable_Null() {
        Logger child = new MockLogger("childLogger", null);
        Logger parent = new MockLogger("parentLogger", VALID_RESOURCE_BUNDLE2);
        child.addHandler(new MockHandler());
        child.setParent(parent);
        child.setLevel(Level.INFO);
        child.logp(Level.INFO, null, null, null, (Throwable) null);
        LogRecord r = (LogRecord) CallVerificationStack.getInstance().pop();
        assertTrue(CallVerificationStack.getInstance().empty());
        assertSame(r.getLoggerName(), child.getName());
        assertNull(r.getMessage());
        assertSame(r.getResourceBundleName(), parent.getResourceBundleName());
        assertSame(r.getResourceBundle(), parent.getResourceBundle());
        assertSame(r.getSourceClassName(), null);
        assertSame(r.getSourceMethodName(), null);
        assertSame(r.getLevel(), Level.INFO);
        assertNull(r.getParameters());
        assertSame(r.getThrown(), null);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "logp",
        args = {java.util.logging.Level.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.Throwable.class}
    )
    public void testLogp_LevelStringStringStringThrowable_NullLevel() {
        try {
            this.sharedLogger.logp(null, "sourceClass", "sourceMethod",
                    "log(Level, String, String, String, Throwable) msg",
                    new Throwable());
            fail("Should throw NullPointerException!");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "logrb",
        args = {java.util.logging.Level.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class}
    )
    public void testLogrb_LevelStringStringString_Normal() {
        this.sharedLogger.setLevel(Level.INFO);
        this.sharedLogger.logrb(Level.INFO, "sourceClass", "sourceMethod",
                VALID_RESOURCE_BUNDLE2,
                "logrb(Level, String, String, String, String) msg");
        LogRecord r = (LogRecord) CallVerificationStack.getInstance().pop();
        assertTrue(CallVerificationStack.getInstance().empty());
        assertSame(r.getLoggerName(), this.sharedLogger.getName());
        assertEquals(r.getMessage(),
                "logrb(Level, String, String, String, String) msg");
        assertSame(r.getResourceBundleName(), VALID_RESOURCE_BUNDLE2);
        assertEquals(VALID_VALUE2, r.getResourceBundle().getString(VALID_KEY));
        assertSame(r.getSourceClassName(), "sourceClass");
        assertSame(r.getSourceMethodName(), "sourceMethod");
        assertSame(r.getLevel(), Level.INFO);
        assertNull(r.getParameters());
        assertSame(r.getThrown(), null);
        this.sharedLogger.logrb(Level.CONFIG, "sourceClass", "sourceMethod",
                VALID_RESOURCE_BUNDLE2,
                "logrb(Level, String, String, String, String) msg");
        assertTrue(CallVerificationStack.getInstance().empty());
        this.sharedLogger.setLevel(Level.OFF);
        this.sharedLogger.logrb(Level.OFF, "sourceClass", "sourceMethod",
                VALID_RESOURCE_BUNDLE2,
                "logrb(Level, String, String, String, String) msg");
        assertTrue(CallVerificationStack.getInstance().empty());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "logrb",
        args = {java.util.logging.Level.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class}
    )
    public void testLogrb_LevelStringStringString_NullMsg() {
        this.sharedLogger.setLevel(Level.INFO);
        this.sharedLogger.logrb(Level.INFO, null, null, null, null);
        LogRecord r = (LogRecord) CallVerificationStack.getInstance().pop();
        assertTrue(CallVerificationStack.getInstance().empty());
        assertSame(r.getLoggerName(), this.sharedLogger.getName());
        assertNull(r.getMessage());
        assertSame(r.getResourceBundleName(), null);
        assertSame(r.getSourceClassName(), null);
        assertSame(r.getSourceMethodName(), null);
        assertSame(r.getLevel(), Level.INFO);
        assertNull(r.getParameters());
        assertSame(r.getThrown(), null);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "logrb",
        args = {java.util.logging.Level.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class}
    )
    public void testLogrb_LevelStringStringString_NullLevel() {
        try {
            this.sharedLogger.logrb(null, "sourceClass", "sourceMethod",
                    VALID_RESOURCE_BUNDLE2,
                    "logrb(Level, String, String, String, String) msg");
            fail("Should throw NullPointerException!");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "logrb",
        args = {java.util.logging.Level.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class}
    )
    public void testLogrb_LevelStringStringString_InvalidRes() {
        this.sharedLogger.setLevel(Level.ALL);
        this.sharedLogger.logrb(Level.ALL, "sourceClass", "sourceMethod",
                INVALID_RESOURCE_BUNDLE,
                "logrb(Level, String, String, String, String) msg");
        LogRecord r = (LogRecord) CallVerificationStack.getInstance().pop();
        assertTrue(CallVerificationStack.getInstance().empty());
        assertSame(r.getLoggerName(), this.sharedLogger.getName());
        assertEquals(r.getMessage(),
                "logrb(Level, String, String, String, String) msg");
        assertSame(r.getResourceBundleName(), INVALID_RESOURCE_BUNDLE);
        assertSame(r.getResourceBundle(), null);
        assertSame(r.getSourceClassName(), "sourceClass");
        assertSame(r.getSourceMethodName(), "sourceMethod");
        assertSame(r.getLevel(), Level.ALL);
        assertNull(r.getParameters());
        assertSame(r.getThrown(), null);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "logrb",
        args = {java.util.logging.Level.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.Object.class}
    )
    public void testLogrb_LevelStringStringStringObject_Normal() {
        Object param = new Object();
        this.sharedLogger.setLevel(Level.INFO);
        this.sharedLogger.logrb(Level.INFO, "sourceClass", "sourceMethod",
                VALID_RESOURCE_BUNDLE2,
                "logrb(Level, String, String, String, String, Object) msg",
                param);
        LogRecord r = (LogRecord) CallVerificationStack.getInstance().pop();
        assertTrue(CallVerificationStack.getInstance().empty());
        assertSame(r.getLoggerName(), this.sharedLogger.getName());
        assertEquals(r.getMessage(),
                "logrb(Level, String, String, String, String, Object) msg");
        assertSame(r.getResourceBundleName(), VALID_RESOURCE_BUNDLE2);
        assertSame(r.getSourceClassName(), "sourceClass");
        assertSame(r.getSourceMethodName(), "sourceMethod");
        assertSame(r.getLevel(), Level.INFO);
        assertEquals(1, r.getParameters().length);
        assertSame(param, r.getParameters()[0]);
        assertSame(r.getThrown(), null);
        this.sharedLogger.logrb(Level.CONFIG, "sourceClass", "sourceMethod",
                VALID_RESOURCE_BUNDLE2,
                "logrb(Level, String, String, String, String, Object) msg",
                param);
        assertTrue(CallVerificationStack.getInstance().empty());
        this.sharedLogger.setLevel(Level.OFF);
        this.sharedLogger.logrb(Level.OFF, "sourceClass", "sourceMethod",
                VALID_RESOURCE_BUNDLE2,
                "logrb(Level, String, String, String, String, Object) msg",
                param);
        assertTrue(CallVerificationStack.getInstance().empty());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "logrb",
        args = {java.util.logging.Level.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.Object.class}
    )
    public void testLogrb_LevelStringStringStringObject_NullMsgObj() {
        this.sharedLogger.setLevel(Level.INFO);
        this.sharedLogger.logrb(Level.INFO, null, null, null, null,
                (Object) null);
        LogRecord r = (LogRecord) CallVerificationStack.getInstance().pop();
        assertTrue(CallVerificationStack.getInstance().empty());
        assertSame(r.getLoggerName(), this.sharedLogger.getName());
        assertNull(r.getMessage());
        assertSame(r.getResourceBundleName(), null);
        assertSame(r.getSourceClassName(), null);
        assertSame(r.getSourceMethodName(), null);
        assertSame(r.getLevel(), Level.INFO);
        assertEquals(1, r.getParameters().length);
        assertNull(r.getParameters()[0]);
        assertSame(r.getThrown(), null);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Regression test.",
        method = "logrb",
        args = {java.util.logging.Level.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class}
    )
    public void test_logrbLLevel_LString_LString_LObject_Security()
            throws Exception {
        SecurityManager originalSecurityManager = System.getSecurityManager();
        try {
            System.setSecurityManager(new MockSecurityManager());
            Logger.global.logrb(Level.OFF, null, null, "abc", "def");
        } finally {
            System.setSecurityManager(originalSecurityManager);
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "logrb",
        args = {java.util.logging.Level.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.Object.class}
    )
    public void testLogrb_LevelStringStringStringObject_NullLevel() {
        try {
            this.sharedLogger.logrb(null, "sourceClass", "sourceMethod",
                    VALID_RESOURCE_BUNDLE2,
                    "logrb(Level, String, String, String, String, Object) msg",
                    new Object());
            fail("Should throw NullPointerException!");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "logrb",
        args = {java.util.logging.Level.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.Object.class}
    )
    public void testLogrb_LevelStringStringStringObject_InvalidRes() {
        Object param = new Object();
        this.sharedLogger.setLevel(Level.ALL);
        this.sharedLogger.logrb(Level.ALL, "sourceClass", "sourceMethod",
                INVALID_RESOURCE_BUNDLE,
                "logrb(Level, String, String, String, String) msg", param);
        LogRecord r = (LogRecord) CallVerificationStack.getInstance().pop();
        assertTrue(CallVerificationStack.getInstance().empty());
        assertSame(r.getLoggerName(), this.sharedLogger.getName());
        assertEquals(r.getMessage(),
                "logrb(Level, String, String, String, String) msg");
        assertSame(r.getResourceBundleName(), INVALID_RESOURCE_BUNDLE);
        assertSame(r.getResourceBundle(), null);
        assertSame(r.getSourceClassName(), "sourceClass");
        assertSame(r.getSourceMethodName(), "sourceMethod");
        assertSame(r.getLevel(), Level.ALL);
        assertEquals(1, r.getParameters().length);
        assertSame(param, r.getParameters()[0]);
        assertSame(r.getThrown(), null);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "logrb",
        args = {java.util.logging.Level.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.Object[].class}
    )      
    public void testLogrb_LevelStringStringStringObjects_Normal() {
        Object[] params = new Object[2];
        params[0] = new Object();
        params[1] = new Object();
        this.sharedLogger.setLevel(Level.INFO);
        this.sharedLogger.logrb(Level.INFO, "sourceClass", "sourceMethod",
                VALID_RESOURCE_BUNDLE2,
                "logrb(Level, String, String, String, String, Object[]) msg",
                params);
        LogRecord r = (LogRecord) CallVerificationStack.getInstance().pop();
        assertTrue(CallVerificationStack.getInstance().empty());
        assertSame(r.getLoggerName(), this.sharedLogger.getName());
        assertEquals(r.getMessage(),
                "logrb(Level, String, String, String, String, Object[]) msg");
        assertSame(r.getResourceBundleName(), VALID_RESOURCE_BUNDLE2);
        assertSame(r.getSourceClassName(), "sourceClass");
        assertSame(r.getSourceMethodName(), "sourceMethod");
        assertSame(r.getLevel(), Level.INFO);
        assertEquals(2, r.getParameters().length);
        assertSame(params[0], r.getParameters()[0]);
        assertSame(params[1], r.getParameters()[1]);
        assertSame(r.getThrown(), null);
        this.sharedLogger.logrb(Level.CONFIG, "sourceClass", "sourceMethod",
                VALID_RESOURCE_BUNDLE2,
                "logrb(Level, String, String, String, String, Object[]) msg",
                params);
        assertTrue(CallVerificationStack.getInstance().empty());
        this.sharedLogger.setLevel(Level.OFF);
        this.sharedLogger.logrb(Level.OFF, "sourceClass", "sourceMethod",
                VALID_RESOURCE_BUNDLE2,
                "logrb(Level, String, String, String, String, Object[]) msg",
                params);
        assertTrue(CallVerificationStack.getInstance().empty());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "logrb",
        args = {java.util.logging.Level.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.Object[].class}
    )
    public void testLogrb_LevelStringStringStringObjects_NullMsgObj() {
        this.sharedLogger.setLevel(Level.INFO);
        this.sharedLogger.logrb(Level.INFO, null, null, null, null,
                (Object[]) null);
        LogRecord r = (LogRecord) CallVerificationStack.getInstance().pop();
        assertTrue(CallVerificationStack.getInstance().empty());
        assertSame(r.getLoggerName(), this.sharedLogger.getName());
        assertNull(r.getMessage());
        assertSame(r.getResourceBundleName(), null);
        assertSame(r.getSourceClassName(), null);
        assertSame(r.getSourceMethodName(), null);
        assertSame(r.getLevel(), Level.INFO);
        assertNull(r.getParameters());
        assertSame(r.getThrown(), null);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "logrb",
        args = {java.util.logging.Level.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.Object[].class}
    )
    public void testLogrb_LevelStringStringStringObjects_NullLevel() {
        try {
            this.sharedLogger.logrb(
                    null,
                    "sourceClass",
                    "sourceMethod",
                    VALID_RESOURCE_BUNDLE2,
                    "logrb(Level, String, String, String, String, Object[]) msg",
                    new Object[0]);
            fail("Should throw NullPointerException!");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "logrb",
        args = {java.util.logging.Level.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.Object[].class}
    )
    public void testLogrb_LevelStringStringStringObjects_InvalidRes() {
        Object[] params = new Object[2];
        params[0] = new Object();
        params[1] = new Object();
        this.sharedLogger.setLevel(Level.ALL);
        this.sharedLogger.logrb(Level.ALL, "sourceClass", "sourceMethod",
                INVALID_RESOURCE_BUNDLE,
                "logrb(Level, String, String, String, String) msg", params);
        LogRecord r = (LogRecord) CallVerificationStack.getInstance().pop();
        assertTrue(CallVerificationStack.getInstance().empty());
        assertSame(r.getLoggerName(), this.sharedLogger.getName());
        assertEquals(r.getMessage(),
                "logrb(Level, String, String, String, String) msg");
        assertSame(r.getResourceBundleName(), INVALID_RESOURCE_BUNDLE);
        assertSame(r.getResourceBundle(), null);
        assertSame(r.getSourceClassName(), "sourceClass");
        assertSame(r.getSourceMethodName(), "sourceMethod");
        assertSame(r.getLevel(), Level.ALL);
        assertEquals(2, r.getParameters().length);
        assertSame(params[0], r.getParameters()[0]);
        assertSame(params[1], r.getParameters()[1]);
        assertSame(r.getThrown(), null);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "logrb",
        args = {java.util.logging.Level.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.Throwable.class}
    )
    public void testLogrb_LevelStringStringStringThrowable_Normal() {
        Throwable t = new Throwable();
        this.sharedLogger.setLevel(Level.INFO);
        this.sharedLogger.logrb(Level.parse("1611"), "sourceClass",
                "sourceMethod", VALID_RESOURCE_BUNDLE2,
                "logrb(Level, String, String, String, String, Throwable) msg",
                t);
        LogRecord r = (LogRecord) CallVerificationStack.getInstance().pop();
        assertTrue(CallVerificationStack.getInstance().empty());
        assertSame(r.getLoggerName(), this.sharedLogger.getName());
        assertEquals(r.getMessage(),
                "logrb(Level, String, String, String, String, Throwable) msg");
        assertSame(r.getResourceBundleName(), VALID_RESOURCE_BUNDLE2);
        assertSame(r.getSourceClassName(), "sourceClass");
        assertSame(r.getSourceMethodName(), "sourceMethod");
        assertSame(r.getLevel(), Level.parse("1611"));
        assertNull(r.getParameters());
        assertSame(r.getThrown(), t);
        assertNull(Level.parse("1611").getResourceBundleName());
        this.sharedLogger.logrb(Level.CONFIG, "sourceClass", "sourceMethod",
                VALID_RESOURCE_BUNDLE2,
                "logrb(Level, String, String, String, String, Throwable) msg",
                t);
        assertTrue(CallVerificationStack.getInstance().empty());
        this.sharedLogger.setLevel(Level.OFF);
        this.sharedLogger.logrb(Level.OFF, "sourceClass", "sourceMethod",
                VALID_RESOURCE_BUNDLE2,
                "logrb(Level, String, String, String, String, Throwable) msg",
                t);
        assertTrue(CallVerificationStack.getInstance().empty());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "logrb",
        args = {java.util.logging.Level.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.Throwable.class}
    )
    public void testLogrb_LevelStringTStringStringhrowable_NullMsgObj() {
        this.sharedLogger.setLevel(Level.INFO);
        this.sharedLogger.logrb(Level.INFO, null, null, null, null,
                (Throwable) null);
        LogRecord r = (LogRecord) CallVerificationStack.getInstance().pop();
        assertTrue(CallVerificationStack.getInstance().empty());
        assertSame(r.getLoggerName(), this.sharedLogger.getName());
        assertNull(r.getMessage());
        assertSame(r.getResourceBundleName(), null);
        assertSame(r.getSourceClassName(), null);
        assertSame(r.getSourceMethodName(), null);
        assertSame(r.getLevel(), Level.INFO);
        assertNull(r.getParameters());
        assertSame(r.getThrown(), null);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "logrb",
        args = {java.util.logging.Level.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.Throwable.class}
    )
    public void testLogrb_LevelStringStringStringThrowable_NullLevel() {
        try {
            this.sharedLogger.logrb(
                    null,
                    "sourceClass",
                    "sourceMethod",
                    VALID_RESOURCE_BUNDLE2,
                    "log(Level, String, String, String, String, Throwable) msg",
                    new Throwable());
            fail("Should throw NullPointerException!");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "logrb",
        args = {java.util.logging.Level.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.Throwable.class}
    )
    public void testLogrb_LevelStringStringStringThrowable_InvalidRes() {
        Throwable t = new Throwable();
        this.sharedLogger.setLevel(Level.ALL);
        this.sharedLogger.logrb(Level.ALL, "sourceClass", "sourceMethod",
                INVALID_RESOURCE_BUNDLE,
                "logrb(Level, String, String, String, String) msg", t);
        LogRecord r = (LogRecord) CallVerificationStack.getInstance().pop();
        assertTrue(CallVerificationStack.getInstance().empty());
        assertSame(r.getLoggerName(), this.sharedLogger.getName());
        assertEquals(r.getMessage(),
        "logrb(Level, String, String, String, String) msg");
        assertSame(r.getResourceBundleName(), INVALID_RESOURCE_BUNDLE);
        assertSame(r.getResourceBundle(), null);
        assertSame(r.getSourceClassName(), "sourceClass");
        assertSame(r.getSourceMethodName(), "sourceMethod");
        assertSame(r.getLevel(), Level.ALL);
        assertNull(r.getParameters());
        assertSame(r.getThrown(), t);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "log",
        args = {java.util.logging.LogRecord.class}
    )
    public void testLog_LogRecord_AppropriateLevelNoFilterNoParent() {
        LogRecord r = new LogRecord(Level.INFO,
        "testLog_LogRecord_AppropriateLevelNoFilterNoParent");
        this.sharedLogger.setLevel(Level.INFO);
        this.sharedLogger.log(r);
        assertSame(r, CallVerificationStack.getInstance().pop());
        assertTrue(CallVerificationStack.getInstance().empty());
        assertSame(r.getLoggerName(), null);
        assertEquals(r.getMessage(),
        "testLog_LogRecord_AppropriateLevelNoFilterNoParent");
        assertSame(r.getResourceBundleName(), null);
        assertSame(r.getSourceClassName(), null);
        assertSame(r.getSourceMethodName(), null);
        assertSame(r.getLevel(), Level.INFO);
        assertNull(r.getParameters());
        assertSame(r.getThrown(), null);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "log",
        args = {java.util.logging.LogRecord.class}
    )
    public void testLog_LogRecord_Null() {
        this.sharedLogger.setLevel(Level.INFO);
        try {
            this.sharedLogger.log(null);
            fail("Should throw NullPointerException!");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "log",
        args = {java.util.logging.LogRecord.class}
    )
    public void testLog_LogRecord_InppropriateLevelNoFilterNoParent() {
        LogRecord r = new LogRecord(Level.INFO,
                "testLog_LogRecord_InppropriateLevelNoFilterNoParent");
        this.sharedLogger.setLevel(Level.WARNING);
        this.sharedLogger.log(r);
        assertTrue(CallVerificationStack.getInstance().empty());
        r.setLevel(Level.OFF);
        this.sharedLogger.setLevel(Level.OFF);
        this.sharedLogger.log(r);
        assertTrue(CallVerificationStack.getInstance().empty());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "log",
        args = {java.util.logging.LogRecord.class}
    )
    public void testLog_LogRecord_AppropriateLevelTrueFilterNoParent() {
        LogRecord r = new LogRecord(Level.INFO,
                "testLog_LogRecord_AppropriateLevelTrueFilterNoParent");
        this.sharedLogger.setLevel(Level.INFO);
        this.sharedLogger.setFilter(new MockTrueFilter());
        this.sharedLogger.log(r);
        assertSame(r, CallVerificationStack.getInstance().pop());
        assertSame(r, CallVerificationStack.getInstance().pop());
        assertTrue(CallVerificationStack.getInstance().empty());
        assertSame(r.getLoggerName(), null);
        assertEquals(r.getMessage(),
                "testLog_LogRecord_AppropriateLevelTrueFilterNoParent");
        assertSame(r.getResourceBundleName(), null);
        assertSame(r.getSourceClassName(), null);
        assertSame(r.getSourceMethodName(), null);
        assertSame(r.getLevel(), Level.INFO);
        assertNull(r.getParameters());
        assertSame(r.getThrown(), null);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "log",
        args = {java.util.logging.LogRecord.class}
    )
    public void testLog_LogRecord_AppropriateLevelFalseFilterNoParent() {
        LogRecord r = new LogRecord(Level.INFO,
                "testLog_LogRecord_AppropriateLevelFalseFilterNoParent");
        this.sharedLogger.setLevel(Level.INFO);
        this.sharedLogger.setFilter(new MockFilter());
        this.sharedLogger.log(r);
        assertSame(r, CallVerificationStack.getInstance().pop());
        assertTrue(CallVerificationStack.getInstance().empty());
        assertSame(r.getLoggerName(), null);
        assertEquals(r.getMessage(),
                "testLog_LogRecord_AppropriateLevelFalseFilterNoParent");
        assertSame(r.getResourceBundleName(), null);
        assertSame(r.getSourceClassName(), null);
        assertSame(r.getSourceMethodName(), null);
        assertSame(r.getLevel(), Level.INFO);
        assertNull(r.getParameters());
        assertSame(r.getThrown(), null);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "log",
        args = {java.util.logging.LogRecord.class}
    )
    public void testLog_ParentInformed() {
        Logger child = new MockLogger("childLogger", VALID_RESOURCE_BUNDLE);
        Logger parent = new MockParentLogger("parentLogger",
                VALID_RESOURCE_BUNDLE2);
        child.setParent(parent);
        child.setLevel(Level.INFO);
        parent.setLevel(Level.INFO);
        parent.addHandler(new MockHandler());
        LogRecord r = new LogRecord(Level.INFO, "testLog_ParentInformed");
        child.log(r);
        assertTrue(child.getUseParentHandlers());
        assertSame(r, CallVerificationStack.getInstance().pop());
        assertTrue(CallVerificationStack.getInstance().empty());
        assertSame(r.getLoggerName(), null);
        assertEquals(r.getMessage(), "testLog_ParentInformed");
        assertSame(r.getResourceBundleName(), null);
        assertSame(r.getSourceClassName(), null);
        assertSame(r.getSourceMethodName(), null);
        assertSame(r.getLevel(), Level.INFO);
        assertNull(r.getParameters());
        assertSame(r.getThrown(), null);
        child.setLevel(Level.SEVERE);
        child.log(r);
        assertTrue(CallVerificationStack.getInstance().empty());
        child.setLevel(Level.INFO);
        parent.setLevel(Level.SEVERE);
        child.log(r);
        assertSame(r, CallVerificationStack.getInstance().pop());
        assertTrue(CallVerificationStack.getInstance().empty());
        child.setLevel(Level.OFF);
        child.log(r);
        assertTrue(CallVerificationStack.getInstance().empty());
        r.setLevel(Level.OFF);
        child.log(r);
        assertTrue(CallVerificationStack.getInstance().empty());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "log",
        args = {java.util.logging.LogRecord.class}
    )
    public void testLog_AncestorInformed() {
        Logger child = new MockLogger("childLogger", VALID_RESOURCE_BUNDLE);
        Logger parent = new MockParentLogger("parentLogger",
                VALID_RESOURCE_BUNDLE2);
        Logger ancestor = new MockParentLogger("ancestorLogger",
                VALID_RESOURCE_BUNDLE3);
        child.setParent(parent);
        parent.setParent(ancestor);
        child.setLevel(Level.INFO);
        parent.setLevel(Level.INFO);
        ancestor.setLevel(Level.OFF);
        ancestor.addHandler(new MockHandler());
        LogRecord r = new LogRecord(Level.INFO, "testLog_AncestorInformed");
        child.log(r);
        assertTrue(child.getUseParentHandlers());
        assertTrue(parent.getUseParentHandlers());
        assertSame(r, CallVerificationStack.getInstance().pop());
        assertTrue(CallVerificationStack.getInstance().empty());
        parent.setLevel(Level.WARNING);
        child.log(r);
        assertSame(r, CallVerificationStack.getInstance().pop());
        assertTrue(CallVerificationStack.getInstance().empty());
        parent.setLevel(Level.INFO);
        child.setLevel(Level.WARNING);
        child.log(r);
        assertTrue(CallVerificationStack.getInstance().empty());
        parent.setLevel(Level.INFO);
        child.setLevel(Level.INFO);
        parent.setUseParentHandlers(false);
        child.log(r);
        assertTrue(CallVerificationStack.getInstance().empty());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "log",
        args = {java.util.logging.LogRecord.class}
    )
    public void testLog_ParentNotInformed() {
        Logger child = new MockLogger("childLogger", VALID_RESOURCE_BUNDLE);
        Logger parent = new MockParentLogger("parentLogger",
                VALID_RESOURCE_BUNDLE2);
        child.setParent(parent);
        child.setLevel(Level.INFO);
        parent.setLevel(Level.INFO);
        parent.addHandler(new MockHandler());
        LogRecord r = new LogRecord(Level.INFO, "testLog_ParentInformed");
        child.setUseParentHandlers(false);
        child.log(r);
        assertFalse(child.getUseParentHandlers());
        assertTrue(CallVerificationStack.getInstance().empty());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "log",
        args = {java.util.logging.LogRecord.class}
    )
    public void testLog_NullLevelNoParent() {
        LogRecord r = new LogRecord(Level.INFO, "testLog_NullLevelNoParent");
        assertNull(this.sharedLogger.getLevel());
        assertNull(this.sharedLogger.getParent());
        assertTrue(this.sharedLogger.isLoggable(r.getLevel()));
        this.sharedLogger.log(r);
        assertSame(r, CallVerificationStack.getInstance().pop());
        assertTrue(CallVerificationStack.getInstance().empty());
        assertNull(this.sharedLogger.getLevel());
        r.setLevel(Level.WARNING);
        assertTrue(this.sharedLogger.isLoggable(r.getLevel()));
        this.sharedLogger.log(r);
        assertSame(r, CallVerificationStack.getInstance().pop());
        assertTrue(CallVerificationStack.getInstance().empty());
        r.setLevel(Level.CONFIG);
        this.sharedLogger.log(r);
        assertFalse(this.sharedLogger.isLoggable(r.getLevel()));
        assertTrue(CallVerificationStack.getInstance().empty());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "log",
        args = {java.util.logging.LogRecord.class}
    )
    public void testLog_NullLevelHasParent() {
        Logger child = new MockLogger("childLogger", VALID_RESOURCE_BUNDLE);
        Logger parent = new MockLogger("parentLogger", VALID_RESOURCE_BUNDLE2);
        child.addHandler(new MockHandler());
        child.setParent(parent);
        parent.setLevel(Level.FINER);
        assertNull(child.getLevel());
        LogRecord r = new LogRecord(Level.FINE, "testLog_NullLevelHasParent");
        child.log(r);
        assertTrue(child.isLoggable(r.getLevel()));
        assertSame(r, CallVerificationStack.getInstance().pop());
        assertTrue(CallVerificationStack.getInstance().empty());
        assertSame(r.getLoggerName(), null);
        assertEquals(r.getMessage(), "testLog_NullLevelHasParent");
        assertSame(r.getResourceBundleName(), null);
        assertSame(r.getSourceClassName(), null);
        assertSame(r.getSourceMethodName(), null);
        assertSame(r.getLevel(), Level.FINE);
        assertNull(r.getParameters());
        assertSame(r.getThrown(), null);
        assertNull(child.getLevel());
        parent.setLevel(Level.CONFIG);
        assertFalse(child.isLoggable(r.getLevel()));
        child.log(r);
        assertTrue(CallVerificationStack.getInstance().empty());
        assertNull(child.getLevel());
        Logger ancestor = new MockLogger("ancestorLogger",
                VALID_RESOURCE_BUNDLE3);
        parent.setParent(ancestor);
        parent.setLevel(null);
        parent.setUseParentHandlers(false);
        ancestor.setLevel(Level.ALL);
        child.log(r);
        assertTrue(child.isLoggable(r.getLevel()));
        assertSame(r, CallVerificationStack.getInstance().pop());
        assertTrue(CallVerificationStack.getInstance().empty());
        assertNull(child.getLevel());
        assertNull(parent.getLevel());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "log",
        args = {java.util.logging.LogRecord.class}
    )
    public void testLog_NullResNoParent() {
        Logger log = new MockLogger("Logger", null);
        log.addHandler(new MockHandler());
        log.setLevel(Level.FINE);
        assertNull(log.getResourceBundle());
        assertNull(log.getResourceBundleName());
        assertNull(log.getParent());
        log.log(Level.INFO, "testLog_NullResNoParent");
        LogRecord r = (LogRecord) CallVerificationStack.getInstance().pop();
        assertTrue(CallVerificationStack.getInstance().empty());
        assertNull(log.getResourceBundle());
        assertNull(log.getResourceBundleName());
        assertNull(r.getResourceBundle());
        assertNull(r.getResourceBundleName());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "log",
        args = {java.util.logging.LogRecord.class}
    )
    public void testLog_NullResHasParent() {
        Logger child = new MockLogger("childLogger", null);
        Logger parent = new MockLogger("parentLogger", VALID_RESOURCE_BUNDLE2);
        child.addHandler(new MockHandler());
        child.setParent(parent);
        parent.setLevel(Level.FINER);
        assertNull(child.getResourceBundle());
        assertNull(child.getResourceBundleName());
        child.log(Level.FINE, "testLog_NullResHasParent");
        LogRecord r = (LogRecord) CallVerificationStack.getInstance().pop();
        assertTrue(CallVerificationStack.getInstance().empty());
        assertSame(r.getLoggerName(), "childLogger");
        assertEquals(r.getMessage(), "testLog_NullResHasParent");
        assertSame(r.getResourceBundleName(), VALID_RESOURCE_BUNDLE2);
        assertSame(r.getSourceClassName(), null);
        assertSame(r.getSourceMethodName(), null);
        assertSame(r.getLevel(), Level.FINE);
        assertNull(r.getParameters());
        assertSame(r.getThrown(), null);
        assertNull(child.getResourceBundle());
        assertNull(child.getResourceBundleName());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "log",
        args = {java.util.logging.LogRecord.class}
    )
    public void testLog_NullResHasAncestor() {
        Logger child = new MockLogger("childLogger", null);
        Logger parent = new MockLogger("parentLogger", null);
        Logger ancestor = new MockLogger("ancestorLogger",
                VALID_RESOURCE_BUNDLE3);
        child.addHandler(new MockHandler());
        child.setParent(parent);
        parent.setParent(ancestor);
        parent.setLevel(Level.FINER);
        assertNull(child.getResourceBundle());
        assertNull(child.getResourceBundleName());
        child.log(Level.FINE, "testLog_NullResHasAncestor");
        LogRecord r = (LogRecord) CallVerificationStack.getInstance().pop();
        assertTrue(CallVerificationStack.getInstance().empty());
        assertSame(r.getLoggerName(), "childLogger");
        assertEquals(r.getMessage(), "testLog_NullResHasAncestor");
        assertSame(r.getResourceBundleName(), VALID_RESOURCE_BUNDLE3);
        assertSame(r.getSourceClassName(), null);
        assertSame(r.getSourceMethodName(), null);
        assertSame(r.getLevel(), Level.FINE);
        assertNull(r.getParameters());
        assertSame(r.getThrown(), null);
        assertNull(child.getResourceBundle());
        assertNull(child.getResourceBundleName());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "log",
        args = {java.util.logging.LogRecord.class}
    )
    public void testLog_ExceptionalHandler() {
        MockLogger l = new MockLogger("testLog_ExceptionalHandler", null);
        l.addHandler(new MockExceptionalHandler());
        l.addHandler(new MockHandler());
        try {
            l.severe("testLog_ExceptionalHandler");
            fail("Should throw RuntimeException!");
        } catch (RuntimeException e) {
        }
        assertTrue(CallVerificationStack.getInstance().empty());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getAnonymousLogger",
        args = {java.lang.String.class}
    )
    public void testLoadResourceBundle() {
        SecurityManager oldMan = System.getSecurityManager();
        System.setSecurityManager(new MockNoLoadingClassSecurityManager());
        try {
            Logger.getAnonymousLogger(VALID_RESOURCE_BUNDLE);
        } finally {
            System.setSecurityManager(oldMan);
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "getLogger",
        args = {java.lang.String.class}
    )
    public void test_init_logger() throws Exception {
        Properties p = new Properties();
        p.put("testGetLogger_Normal_ANewLogger2.level", "ALL");
        LogManager.getLogManager().readConfiguration(
                EnvironmentHelper.PropertiesToInputStream(p));
        assertNull(LogManager.getLogManager().getLogger(
                "testGetLogger_Normal_ANewLogger2"));
        SecurityManager originalSecurityManager = System.getSecurityManager();
        try {
            Logger logger = Logger
                            .getLogger("testGetLogger_Normal_ANewLogger2");
            System.setSecurityManager(new MockSecurityManager());
            try {
                logger.setLevel(Level.ALL);
                fail("should throw SecurityException");
            } catch (SecurityException e) {
            }
            try {
                logger.setParent(Logger.getLogger(""));
                fail("should throw SecurityException");
            } catch (SecurityException e) {
            }
        } finally {
            System.setSecurityManager(originalSecurityManager);
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "initHandler",
        args = {}
    )
    @KnownFailure("This test doesn't load its resources properly")
    public void test_initHandler() throws Exception {
        File logProps = new File(LOGGING_CONFIG_FILE);
        LogManager lm = LogManager.getLogManager();
        lm.readConfiguration(new FileInputStream(logProps));
        Logger log = Logger.getLogger("");
        Handler[] handlers = log.getHandlers();
        assertEquals(2, handlers.length);
    }
    public static class MockLogger extends Logger {
        public MockLogger(String name, String resourceBundleName) {
            super(name, resourceBundleName);
        }
    }
    public static class MockParentLogger extends Logger {
        public MockParentLogger(String name, String resourceBundleName) {
            super(name, resourceBundleName);
        }
        public void log(LogRecord record) {
            CallVerificationStack.getInstance().push(record);
            super.log(record);
        }
    }
    public static class MockHandler extends Handler {
        public void close() {
        }
        public void flush() {
        }
        public void publish(LogRecord record) {
            CallVerificationStack.getInstance().push(record);
        }
    }
    public static class MockExceptionalHandler extends Handler {
        public void close() {
        }
        public void flush() {
        }
        public void publish(LogRecord record) {
            throw new RuntimeException();
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
    public static class MockNoLoadingClassSecurityManager extends
            SecurityManager {
        public MockNoLoadingClassSecurityManager() {
        }
        public void checkPermission(Permission perm) {
            if (perm instanceof RuntimePermission) {
                if ("getClassLoader".equals(perm.getName())) {
                    throw new SecurityException();
                }
            }
        }
        public void checkPermission(Permission perm, Object context) {
            if (perm instanceof LoggingPermission) {
                throw new SecurityException();
            }
        }
    }
    public static class MockFilter implements Filter {
        public boolean isLoggable(LogRecord record) {
            CallVerificationStack.getInstance().push(record);
            return false;
        }
    }
    public static class MockTrueFilter implements Filter {
        public boolean isLoggable(LogRecord record) {
            CallVerificationStack.getInstance().push(record);
            return true;
        }
    }
}
