@TestTargetClass(DriverManager.class)
public class DriverManagerTest extends TestCase {
    static final String DRIVER1 = "org.apache.harmony.sql.tests.java.sql.TestHelper_Driver1";
    static final String DRIVER2 = "org.apache.harmony.sql.tests.java.sql.TestHelper_Driver2";
    static final String DRIVER3 = "org.apache.harmony.sql.tests.java.sql.TestHelper_Driver3";
    static final String DRIVER4 = "org.apache.harmony.sql.tests.java.sql.TestHelper_Driver4";
    static final String DRIVER5 = "org.apache.harmony.sql.tests.java.sql.TestHelper_Driver5";
    static final String INVALIDDRIVER1 = "abc.klm.Foo";
    static String[] driverNames = { DRIVER1, DRIVER2 };
    static int numberLoaded;
    static String baseURL1 = "jdbc:mikes1";
    static String baseURL4 = "jdbc:mikes4";
    static final String JDBC_PROPERTY = "jdbc.drivers";
    static TestHelper_ClassLoader testClassLoader = new TestHelper_ClassLoader();
    @Override
    public void setUp() {
        TestEnvironment.reset();
        numberLoaded = loadDrivers();
    } 
    @TestTargetNew(
        level = TestLevel.SUFFICIENT,
        notes = "SQLException checking missed: not feasible.",
        method = "deregisterDriver",
        args = {java.sql.Driver.class}
    )
    @KnownFailure("Not all Drivers are loaded in testsetup. Classloader issue in DriverManager.")
    public void testDeregisterDriver() throws Exception {
        Driver aDriver;
        aDriver = DriverManager.getDriver(baseURL4);
        DriverManager.deregisterDriver(aDriver);
        assertFalse("testDeregisterDriver: Driver was not deregistered.",
                isDriverLoaded(aDriver));
        DriverManager.registerDriver(aDriver);
        assertTrue("testDeregisterDriver: Driver did not reload.",
                isDriverLoaded(aDriver));
        DriverManager.deregisterDriver(null);
        aDriver = DriverManager.getDriver(baseURL1);
        try {
            Class<?> driverClass = Class
                    .forName(
                            "org.apache.harmony.sql.tests.java.sql.TestHelper_DriverManager",
                            true, testClassLoader);
            Class<?>[] methodClasses = {Class.forName("java.sql.Driver")};
            Method theMethod = driverClass.getDeclaredMethod("setDriver",
                    methodClasses);
            Object[] args = {aDriver};
            assertNotNull(args);
            theMethod.invoke(null, args);
        } catch (Exception e) {
            fail("testDeregisterDriver: Got exception allocating TestHelper: "
                    + e.getMessage());
            e.printStackTrace();
            return;
        } 
        assertTrue(
                "testDeregisterDriver: Driver was incorrectly deregistered.",
                DriverManagerTest.isDriverLoaded(aDriver));
    } 
    static void printClassLoader(Object theObject) {
        Class<? extends Object> theClass = theObject.getClass();
        ClassLoader theClassLoader = theClass.getClassLoader();
        System.out.println("ClassLoader is: " + theClassLoader.toString()
                + " for object: " + theObject.toString());
    } 
    static boolean isDriverLoaded(Driver theDriver) {
        Enumeration<?> driverList = DriverManager.getDrivers();
        while (driverList.hasMoreElements()) {
            if ((Driver) driverList.nextElement() == theDriver) {
                return true;
            }
        } 
        return false;
    } 
    static String validConnectionURL = "jdbc:mikes1:data1";
    static String invalidConnectionURL1 = "jdbc:mikes1:data2";
    static String invalidConnectionURL2 = "xyz1:abc3:456q";
    static String invalidConnectionURL3 = null;
    static String[] invalidConnectionURLs = { invalidConnectionURL2,
            invalidConnectionURL3 };
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getConnection",
        args = {java.lang.String.class}
    )
    public void testGetConnectionString() throws SQLException {
        Connection theConnection = null;
        theConnection = DriverManager.getConnection(validConnectionURL);
        assertNotNull(theConnection);
        assertNotNull(DriverManager.getConnection(invalidConnectionURL1));
        for (String element : invalidConnectionURLs) {
            try {
                theConnection = DriverManager
                        .getConnection(element);
                fail("Should throw SQLException");
            } catch (SQLException e) {
            } 
        } 
    } 
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getConnection",
        args = {java.lang.String.class, java.util.Properties.class}
    )
    public void test_getConnection_LStringLProperties() {
        try {
            DriverManager.getConnection("fff", 
                    new Properties());
            fail("Should throw SQLException.");
        } catch (SQLException e) {
            assertEquals("08001", e.getSQLState()); 
        }
        try {
            DriverManager.getConnection(null, 
                    new Properties());
            fail("Should throw SQLException.");
        } catch (SQLException e) {
            assertEquals("08001", e.getSQLState()); 
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getConnection",
        args = {java.lang.String.class, java.util.Properties.class}
    )
    public void testGetConnectionStringProperties() throws SQLException {
        String validURL1 = "jdbc:mikes1:data2";
        String validuser1 = "theuser";
        String validpassword1 = "thepassword";
        String invalidURL1 = "xyz:abc1:foo";
        String invalidURL2 = "jdbc:mikes1:crazyone";
        String invalidURL3 = "";
        String invaliduser1 = "jonny nouser";
        String invalidpassword1 = "whizz";
        Properties nullProps = null;
        Properties validProps = new Properties();
        validProps.setProperty("user", validuser1);
        validProps.setProperty("password", validpassword1);
        Properties invalidProps1 = new Properties();
        invalidProps1.setProperty("user", invaliduser1);
        invalidProps1.setProperty("password", invalidpassword1);
        String[] invalidURLs = { null, invalidURL1,
                invalidURL2, invalidURL3 };
        Properties[] invalidProps = { nullProps, invalidProps1};
        Connection theConnection = null;
        theConnection = DriverManager.getConnection(validURL1, validProps);
        assertNotNull(theConnection);
        for (int i = 0; i < invalidURLs.length; i++) {
            theConnection = null;
            try {
                theConnection = DriverManager.getConnection(invalidURLs[i],
                        validProps);
                fail("Should throw SQLException");
            } catch (SQLException e) {
            } 
        } 
        for (Properties invalidProp : invalidProps) {
            assertNotNull(DriverManager.getConnection(validURL1, invalidProp));
        } 
    } 
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getConnection",
        args = {java.lang.String.class, java.lang.String.class, java.lang.String.class}
    )
    public void testGetConnectionStringStringString() throws SQLException {
        String validURL1 = "jdbc:mikes1:data2";
        String validuser1 = "theuser";
        String validpassword1 = "thepassword";
        String invalidURL1 = "xyz:abc1:foo";
        String invaliduser1 = "jonny nouser";
        String invalidpassword1 = "whizz";
        String[] invalid1 = { null, validuser1, validpassword1 };
        String[] invalid2 = { validURL1, null, validpassword1 };
        String[] invalid3 = { validURL1, validuser1, null };
        String[] invalid4 = { invalidURL1, validuser1, validpassword1 };
        String[] invalid5 = { validURL1, invaliduser1, invalidpassword1 };
        String[] invalid6 = { validURL1, validuser1, invalidpassword1 };
        String[][] invalids1 = { invalid1, invalid4};
        String[][] invalids2 = {invalid2, invalid3, invalid5, invalid6 };
        Connection theConnection = null;
        theConnection = DriverManager.getConnection(validURL1, validuser1,
                validpassword1);
        assertNotNull(theConnection);
        for (String[] theData : invalids1) {
            theConnection = null;
            try {
                theConnection = DriverManager.getConnection(theData[0],
                        theData[1], theData[2]);
                fail("Should throw SQLException.");
            } catch (SQLException e) {
            } 
        } 
        for (String[] theData : invalids2) {
            assertNotNull(DriverManager.getConnection(theData[0], theData[1],
                    theData[2]));
        } 
    } 
    static String validURL1 = "jdbc:mikes1";
    static String validURL2 = "jdbc:mikes2";
    static String invalidURL1 = "xyz:acb";
    static String invalidURL2 = null;
    static String[] validURLs = { validURL1, validURL2 };
    static String[] invalidURLs = { invalidURL1, invalidURL2 };
    static String exceptionMsg1 = "No suitable driver";
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getDriver",
        args = {java.lang.String.class}
    )
    public void testGetDriver() throws SQLException {
        for (String element : validURLs) {
            Driver validDriver = DriverManager.getDriver(element);
            assertNotNull("Driver " + element + " not loaded", validDriver);
        } 
        for (String element : invalidURLs) {
            try {
                DriverManager.getDriver(element);
                fail("Should throw SQLException");
            } catch (SQLException e) {
                assertEquals("08001", e.getSQLState());
                assertEquals(exceptionMsg1, e.getMessage());
            } 
        } 
    } 
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "test only passes in CTS host environment.",
        method = "getDrivers",
        args = {}
    )
    @KnownFailure("We're working out issues with built-in SQL drivers")
    public void testGetDrivers() {
        Enumeration<Driver> driverList = DriverManager.getDrivers();
        int i = 0;
        while (driverList.hasMoreElements()) {
            Driver theDriver = driverList.nextElement();
            assertNotNull(theDriver);
            i++;
        } 
        final int noOfSystemDriversLoaded = 2; 
        assertEquals("testGetDrivers: Don't see all the loaded drivers - ", numberLoaded - noOfSystemDriversLoaded, i);
    } 
    static int timeout1 = 25;
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getLoginTimeout",
        args = {}
    )
    public void testGetLoginTimeout() {
        DriverManager.setLoginTimeout(timeout1);
        assertEquals(timeout1, DriverManager.getLoginTimeout());
    } 
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getLogStream",
        args = {}
    )
    @SuppressWarnings("deprecation")
    public void testGetLogStream() {
        assertNull(DriverManager.getLogStream());
        DriverManager.setLogStream(testPrintStream);
        assertTrue(DriverManager.getLogStream() == testPrintStream);
        DriverManager.setLogStream(null);
    } 
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getLogWriter",
        args = {}
    )
    public void testGetLogWriter() {
        assertNull(DriverManager.getLogWriter());
        DriverManager.setLogWriter(testPrintWriter);
        assertTrue(DriverManager.getLogWriter() == testPrintWriter);
        DriverManager.setLogWriter(null);
    } 
    static String testMessage = "DriverManagerTest: test message for print stream";
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "println",
        args = {java.lang.String.class}
    )
    @SuppressWarnings("deprecation")
    public void testPrintln() {
        DriverManager.println(testMessage);
        DriverManager.setLogWriter(testPrintWriter);
        DriverManager.println(testMessage);
        String theOutput = outputStream.toString();
        assertTrue(theOutput.startsWith(testMessage));
        DriverManager.setLogWriter(null);
        DriverManager.setLogStream(testPrintStream);
        DriverManager.println(testMessage);
        theOutput = outputStream2.toString();
        assertTrue(theOutput.startsWith(testMessage));
        DriverManager.setLogStream(null);
    } 
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "SQLException checking missed: not feasible",
        method = "registerDriver",
        args = {java.sql.Driver.class}
    )
    public void testRegisterDriver() throws ClassNotFoundException,
            SQLException, IllegalAccessException, InstantiationException {
        try {
            DriverManager.registerDriver(null);
            fail("Should throw NullPointerException.");
        } catch (NullPointerException e) {
        } 
        Driver theDriver = null;
        Class<?> driverClass = Class.forName(DRIVER3);
        theDriver = (Driver) driverClass.newInstance();
        DriverManager.registerDriver(theDriver);
        assertTrue("testRegisterDriver: driver not in loaded set",
                isDriverLoaded(theDriver));
    } 
    static int validTimeout1 = 15;
    static int validTimeout2 = 0;
    static int[] validTimeouts = { validTimeout1, validTimeout2 };
    static int invalidTimeout1 = -10;
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setLoginTimeout",
        args = {int.class}
    )
    public void testSetLoginTimeout() {
        for (int element : validTimeouts) {
            DriverManager.setLoginTimeout(element);
            assertEquals(element, DriverManager.getLoginTimeout());
        } 
        DriverManager.setLoginTimeout(invalidTimeout1);
        assertEquals(invalidTimeout1, DriverManager.getLoginTimeout());
    } 
    static ByteArrayOutputStream outputStream2 = new ByteArrayOutputStream();
    static PrintStream testPrintStream = new PrintStream(outputStream2);
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setLogStream",
        args = {java.io.PrintStream.class}
    )
    @SuppressWarnings("deprecation")
    public void testSetLogStream() {
        DriverManager.setLogStream(testPrintStream);
        assertSame(testPrintStream, DriverManager.getLogStream());
        DriverManager.setLogStream(null);
        assertNull(DriverManager.getLogStream());
        TestSecurityManager theSecManager = new TestSecurityManager();
        System.setSecurityManager(theSecManager);
        theSecManager.setLogAccess(false);
        try {
            DriverManager.setLogStream(testPrintStream);
            fail("Should throw SecurityException.");
        } catch (SecurityException s) {
        }
        theSecManager.setLogAccess(true);
        DriverManager.setLogStream(testPrintStream);
        System.setSecurityManager(null);
    } 
    static ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    static PrintWriter testPrintWriter = new PrintWriter(outputStream);
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setLogWriter",
        args = {java.io.PrintWriter.class}
    )
    public void testSetLogWriter() {
        DriverManager.setLogWriter(testPrintWriter);
        assertSame(testPrintWriter, DriverManager.getLogWriter());
        DriverManager.setLogWriter(null);
        assertNull("testDriverManager: Log writer not null:", DriverManager
                .getLogWriter());
        TestSecurityManager theSecManager = new TestSecurityManager();
        System.setSecurityManager(theSecManager);
        theSecManager.setLogAccess(false);
        try {
            DriverManager.setLogWriter(testPrintWriter);
            fail("Should throw SecurityException.");
        } catch (SecurityException s) {
        }
        theSecManager.setLogAccess(true);
        DriverManager.setLogWriter(testPrintWriter);
        System.setSecurityManager(null);
    } 
    static boolean driversLoaded = false;
    private static int loadDrivers() {
        if (driversLoaded) {
            return numberLoaded;
        }
        int numberLoaded = 0;
        String theSystemDrivers = DRIVER4 + ":" + DRIVER5 + ":"
                + INVALIDDRIVER1;
        System.setProperty(JDBC_PROPERTY, theSystemDrivers);
        numberLoaded += 2;
        for (String element : driverNames) {
            try {
                Class<?> driverClass = Class.forName(element);
                assertNotNull(driverClass);
                 System.out.println("Loaded driver - classloader = " +
                 driverClass.getClassLoader());
                numberLoaded++;
            } catch (ClassNotFoundException e) {
                System.out.println("DriverManagerTest: failed to load Driver: "
                        + element);
            } 
        } 
        driversLoaded = true;
        return numberLoaded;
    } 
    class TestSecurityManager extends SecurityManager {
        boolean logAccess = true;
        SQLPermission sqlPermission = new SQLPermission("setLog");
        RuntimePermission setManagerPermission = new RuntimePermission(
                "setSecurityManager");
        TestSecurityManager() {
            super();
        } 
        void setLogAccess(boolean allow) {
            logAccess = allow;
        } 
        @Override
        public void checkPermission(Permission thePermission) {
            if (thePermission.equals(sqlPermission)) {
                if (!logAccess) {
                    throw new SecurityException("Cannot set the sql Log Writer");
                } 
                return;
            } 
            if (thePermission.equals(setManagerPermission)) {
                return;
            } 
        } 
    } 
    public void test_registerDriver_MultiTimes() throws SQLException {
        int register_count = 10;
        int deregister_count = 1;
        Driver dummy = new DummyDriver();
        DriverManager.registerDriver(new BadDummyDriver());
        for (int i = 0; i < register_count; i++) {
            DriverManager.registerDriver(dummy);
        }
        DriverManager.registerDriver(new BadDummyDriver());
        for (int i = 0; i < deregister_count; i++) {
            DriverManager.deregisterDriver(dummy);
        }
        Driver d = DriverManager.getDriver("jdbc:dummy_protocol:dummy_subname");
        assertNotNull(d);
    }
    @KnownFailure("The test doesn't fork the VM properly.")
    public void test_initClass() throws Exception {
        ProcessBuilder builder = javaProcessBuilder();
        builder.command().add("org/apache/harmony/sql/tests/java/sql/TestMainForDriver");
        assertEquals("", execAndGetOutput(builder));
    }
    private static class BadDummyDriver extends DummyDriver {
        public boolean acceptsURL(String url) {
            return false;
        }
    }
    private static class DummyDriver implements Driver {
        String goodurl = "jdbc:dummy_protocol:dummy_subname";
        public boolean acceptsURL(String url) {
            return url.equals(goodurl);
        }
        public Connection connect(String url, Properties info) {
            return null;
        }
        public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) {
            return null;
        }
        public int getMajorVersion() {
            return 0;
        }
        public int getMinorVersion() {
            return 0;
        }
        public boolean jdbcCompliant() {
            return true;
        }
    }
} 
