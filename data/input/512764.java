@TestTargetClass(AccessControlException.class)
public class AccessControlException2Test extends junit.framework.TestCase {
    FilePermission filePermission;
    AccessControlException acException;
    AccessControlException acException1;
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "AccessControlException",
        args = {java.lang.String.class}
    )    
    public void test_ConstructorLjava_lang_String() {
        assertTrue("AccessControlException's toString() should have returned "
                + "'java.security.AccessControlException: test message' but "
                + "returned: " + acException.toString(), acException.toString()
                .equals("java.security.AccessControlException: test message"));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "AccessControlException",
        args = {java.lang.String.class, java.security.Permission.class}
    )
    public void test_ConstructorLjava_lang_StringLjava_security_Permission() {
        assertTrue("AccessControlException's toString() should have returned "
                + "'java.security.AccessControlException: test message "
                + "(java.io.FilePermission 
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getPermission",
        args = {}
    )
    public void test_getPermission() {
        assertNull(
                "getPermission should have returned null if no permission was set",
                acException.getPermission());
        assertTrue(
                "getPermission should have returned the permission we assigned to it",
                acException1.getPermission() == filePermission);
    }
    protected void setUp() {
        filePermission = new FilePermission("/*", "read");
        acException = new AccessControlException("test message");
        acException1 = new AccessControlException("test message",
                filePermission);
    }
}