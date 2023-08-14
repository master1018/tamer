@TestTargetClass(JDBCDriver.class)
public class JDBCDriverTest extends JDBCDriverFunctionalTest {
    private JDBCDriver jDriver;
    private Driver returnedDriver;
    public void setUp() throws ClassNotFoundException, SQLException, InstantiationException, IllegalAccessException, Exception  {
        try {
            super.setUp();
            returnedDriver = DriverManager.getDriver(getConnectionURL());
            if (returnedDriver instanceof JDBCDriver) {
                this.jDriver = (JDBCDriver) returnedDriver;
            }
        } catch (SQLException e) {
          System.out.println("Cannot get driver");
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("DB Setup failed");
            e.printStackTrace();
        }
   }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "constructor test",
        method = "JDBCDriver",
        args = {}
    )
    public void testJDBCDriver() {
        assertTrue(returnedDriver instanceof JDBCDriver);
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "constructor test",
            method = "acceptsURL",
            args = {java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "constructor test",
            clazz = Driver.class,
            method = "acceptsURL",
            args = {java.lang.String.class}
        )        
    })
    public void testAcceptsURL() {
        try {
            if (this.jDriver != null) {
                assertTrue(jDriver.acceptsURL(getConnectionURL()));
            } else {
                fail("no Driver available");
            }
        } catch (SQLException e) {
            fail("Driver does not accept URL");
            e.printStackTrace();
        }
    }
    @TestTargets({    
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "method test",
            method = "connect",
            args = {java.lang.String.class, java.util.Properties.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            clazz = Driver.class,
            notes = "method test",
            method = "connect",
            args = {java.lang.String.class, java.util.Properties.class}
        )
    })
    public void testConnect() {
        try {
            if (this.jDriver != null) {
                Connection c = jDriver.connect(getConnectionURL(), null);
                assertFalse(c.isClosed());
                DriverManager.getConnection(getConnectionURL());
            } else {
                fail("no Driver available");
            }
        } catch (SQLException e) {
            fail("Driver does not connect");
            e.printStackTrace();
        }
    }
   @TestTargets({    
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "method test",
            method = "getMajorVersion",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            clazz = Driver.class,
            notes = "method test",
            method = "getMajorVersion",
            args = {}
        )
    })
    public void testGetMajorVersion() {
        if (this.jDriver != null) {
            assertTrue(jDriver.getMajorVersion() > 0);
        } else {
            fail("no Driver available");
        }
    }
   @TestTargets({       
       @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "method test",
            method = "getMinorVersion",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "method test",
            clazz = Driver.class,            
            method = "getMinorVersion",
            args = {}
        )
   })
   public void testGetMinorVersion() {
        if (this.jDriver != null) {
            assertTrue(jDriver.getMinorVersion() > 0);
        } else {
            fail("no version information available");
        }
    }
   @TestTargets({
       @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "method test",
            method = "getPropertyInfo",
            args = {java.lang.String.class, java.util.Properties.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "method test",
            clazz = Driver.class,            
            method = "getPropertyInfo",
            args = {java.lang.String.class, java.util.Properties.class}
        )
   })
   public void testGetPropertyInfo() {
        DriverPropertyInfo[] info = null;
        try {
            if (this.jDriver != null) {
                info = jDriver.getPropertyInfo(getConnectionURL(), null);
                assertNotNull(info);
                assertTrue(info.length > 0);
            } else {
                fail("no Driver available");
            }
        } catch (SQLException e) {
            fail("Driver property details not available");
            e.printStackTrace();
        }
        assertNotNull(info);
    }
   @TestTargets({    
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "method test",
            method = "jdbcCompliant",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            clazz = Driver.class,
            notes = "method test",
            method = "jdbcCompliant",
            args = {}
        )
    }) 
    public void testJdbcCompliant() {
        if (this.jDriver != null) {
            assertFalse(jDriver.jdbcCompliant());
        } else {
            fail("no version information available");
        }
    }
    @Override
    protected void tearDown() throws SQLException {
        super.tearDown();
    }
}
