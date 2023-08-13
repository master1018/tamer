@TestTargetClass(DriverManager.class)
public class TestHelper_DriverManager extends TestCase {
    static Driver testDriver = null;
    static TestHelper_DriverManager theHelper;
    static {
        theHelper = new TestHelper_DriverManager();
    } 
    public TestHelper_DriverManager() {
        super();
    } 
    public static void setDriver(Driver theDriver) {
        testDriver = theDriver;
        theHelper.checkDeregister();
    } 
    public void checkDeregister() {
        String baseURL = "jdbc:mikes1";
        Driver aDriver;
        try {
            aDriver = DriverManager.getDriver(baseURL);
            fail("testDeregisterDriver: Didn't get exception when getting valid driver from other classloader.");
        } catch (SQLException e) {
            assertTrue(
                    "testDeregisterDriver: Got exception when getting valid driver from other classloader.",
                    true);
        } 
        aDriver = testDriver;
        try {
            DriverManager.deregisterDriver(aDriver);
            DriverManager.registerDriver(aDriver);
            fail("checkDeregisterDriver: Didn't get Security Exception deregistering invalid driver.");
        } catch (SecurityException s) {
        } catch (Exception e) {
            fail("checkDeregisterDriver: Got wrong exception type when deregistering invalid driver.");
        } 
    } 
    static void printClassLoader(Object theObject) {
        Class<? extends Object> theClass = theObject.getClass();
        ClassLoader theClassLoader = theClass.getClassLoader();
        System.out.println("ClassLoader is: " + theClassLoader.toString()
                + " for object: " + theObject.toString());
    } 
} 
