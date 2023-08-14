@TestTargetClass(AllPermission.class)
public class AllPermissionTest extends TestCase {
    public static void main(String[] args) {
        junit.textui.TestRunner.run(AllPermissionTest.class);
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "AllPermission",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "AllPermission",
            args = {java.lang.String.class, java.lang.String.class}
        )
    })
    public void testCtor()
    {
        AllPermission a1 = new AllPermission();
        assertEquals("<all permissions>", a1.getName());
        assertEquals("<all actions>", a1.getActions());
        a1 = new AllPermission("sdfsdfwe&^$", "*&IUGJKHVB764");
        assertEquals("<all permissions>", a1.getName());
        assertEquals("<all actions>", a1.getActions());
        a1 = new AllPermission(null, "");
        assertEquals("<all permissions>", a1.getName());
        assertEquals("<all actions>", a1.getActions());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "equals",
        args = {java.lang.Object.class}
    )
    public void testEquals()
    {
        AllPermission a1 = new AllPermission();
        AllPermission a2 = new AllPermission();
        assertTrue(a1.equals(a2));
        assertTrue(a1.hashCode() == a2.hashCode());
        assertFalse(a1.equals(null));
        assertFalse(a1.equals(new BasicPermission("hgf"){}));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Null parameter checking missed",
        method = "implies",
        args = {java.security.Permission.class}
    )
    public void testImplies()
    {
        AllPermission a1 = new AllPermission();
        assertTrue(a1.implies(new AllPermission()));
        assertTrue(a1.implies(new BasicPermission("2323"){}));
        assertTrue(a1.implies(new UnresolvedPermission("2323", "", "", null)));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "newPermissionCollection",
        args = {}
    )
    public void testCollection()
    {
        AllPermission a1 = new AllPermission();
        PermissionCollection pc1 = a1.newPermissionCollection();
        PermissionCollection pc2 = a1.newPermissionCollection();
        assertNotSame(pc1, pc2);
    }
}
