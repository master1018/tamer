@TestTargetClass(DriverPropertyInfo.class)
public class DriverPropertyInfoTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verification with invalid parameters missed: no feasible behaviour not specified (black box approach).",
        method = "DriverPropertyInfo",
        args = {java.lang.String.class, java.lang.String.class}
    )
    public void testDriverPropertyInfoStringString() {
        DriverPropertyInfo aDriverPropertyInfo = new DriverPropertyInfo(
                validName, validValue);
        assertNotNull(aDriverPropertyInfo);
        assertEquals(aDriverPropertyInfo.name,validName);
        assertEquals(aDriverPropertyInfo.value,validValue);
        aDriverPropertyInfo = new DriverPropertyInfo(null, null);
        assertNotNull(aDriverPropertyInfo);
        assertNull(aDriverPropertyInfo.name);
        assertNull(aDriverPropertyInfo.value);
    } 
    static String validName = "testname";
    static String validValue = "testvalue";
    static String[] updateChoices = { "Choice1", "Choice2", "Choice3" };
    static String updateValue = "updateValue";
    static boolean updateRequired = true;
    static String updateDescription = "update description";
    static String updateName = "updateName";
    String connectionURL = "jdbc:sqlite:/" + "Test.db";
    String classname = "SQLite.JDBCDriver";
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Field testing",
        method = "!Constants",
        args = {}
    )
    public void testPublicFields() {
        DriverPropertyInfo aDriverPropertyInfo = new DriverPropertyInfo(
                validName, validValue);
        assertTrue(Arrays.equals(testChoices, aDriverPropertyInfo.choices));
        assertEquals(testValue, aDriverPropertyInfo.value);
        assertEquals(testRequired, aDriverPropertyInfo.required);
        assertEquals(testDescription, aDriverPropertyInfo.description);
        assertEquals(testName, aDriverPropertyInfo.name);
        aDriverPropertyInfo.choices = updateChoices;
        aDriverPropertyInfo.value = updateValue;
        aDriverPropertyInfo.required = updateRequired;
        aDriverPropertyInfo.description = updateDescription;
        aDriverPropertyInfo.name = updateName;
        assertTrue(Arrays.equals(updateChoices, aDriverPropertyInfo.choices));
        assertEquals(updateValue, aDriverPropertyInfo.value);
        assertEquals(updateRequired, aDriverPropertyInfo.required);
        assertEquals(updateDescription, aDriverPropertyInfo.description);
        assertEquals(updateName, aDriverPropertyInfo.name);
        try {
            Class.forName(classname).newInstance();
            Properties props = new Properties();
            Driver d = DriverManager.getDriver(connectionURL);
            DriverPropertyInfo[] info = d.getPropertyInfo(connectionURL,
                    props);
            String name = info[0].name;
            assertNotNull(name);
            assertEquals(name, "encoding");
            String[] choices = info[0].choices;
            assertNull(choices);
            boolean required = info[0].required;
            assertFalse(required);
            String description = info[0].description;
            assertNull(description);
        } catch (SQLException e) {
            System.out.println("Error in test setup: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception ex) {
            System.err.println("Unexpected exception " + ex.toString());
        }
    } 
    static String[] testChoices = null;
    static java.lang.String testValue = validValue;
    static boolean testRequired = false;
    static java.lang.String testDescription = null;
    static java.lang.String testName = validName;
} 
