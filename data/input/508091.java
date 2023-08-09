@TestTargetClass(SQLPermission.class)
public class SQLPermissionTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "invalid parameters checking missed. not fully supported",
        method = "SQLPermission",
        args = {java.lang.String.class, java.lang.String.class}
    )
    public void testSQLPermissionStringString() {
        String validName = "setLog";
        String validActions = "theActions";
        SQLPermission thePermission = new SQLPermission(validName, validActions);
        assertNotNull(thePermission);
        assertEquals(validName, thePermission.getName());
        assertEquals("", thePermission.getActions());
    } 
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "not fully supported",
        method = "SQLPermission",
        args = {java.lang.String.class}
    )
    public void testSQLPermissionString() {
        String validName = "setLog";
        SQLPermission thePermission = new SQLPermission(validName);
        assertNotNull(thePermission);
        assertEquals(validName, thePermission.getName());
        String invalidName = "foo";
        thePermission = new SQLPermission(invalidName);
        assertNotNull(thePermission);
        assertEquals(invalidName, thePermission.getName());
        assertEquals("", thePermission.getActions());
    } 
} 
