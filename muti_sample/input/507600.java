@TestTargetClass(AccessControlException.class)
public class AccessControlExceptionTest extends TestCase {
    public static void main(String[] args) {
        junit.textui.TestRunner.run(AccessControlExceptionTest.class);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "AccessControlException",
        args = {java.lang.String.class}
    )
    public void testAccessControlExceptionString() {
        new AccessControlException(null);
        new AccessControlException("Failure");
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "AccessControlException",
        args = {java.lang.String.class, java.security.Permission.class}
    )
    public void testAccessControlExceptionStringPermission() {
        Permission perm = new AllPermission();
        AccessControlException controlException = new AccessControlException("001", perm);
        assertEquals("exception message", "001", controlException.getMessage());
        assertEquals("permission", perm, controlException.getPermission());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getPermission",
        args = {}
    )
    public void testGetPermission() {
        Permission perm = new UnresolvedPermission("unresolvedType",
                "unresolvedName", "unresolvedActions", null);
        AccessControlException ex = new AccessControlException("001", perm);
        assertSame(ex.getPermission(), perm);
    }
}
