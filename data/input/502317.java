@TestTargetClass(AclEntry.class)
public class IAclEntryTest extends TestCase {
    class MyAclEntry extends AclEntryImpl {
        public MyAclEntry() {
            super();
        }
        public MyAclEntry(Principal pr) {
            super(pr);
        }
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "addPermission",
            args = {java.security.acl.Permission.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "checkPermission",
            args = {java.security.acl.Permission.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "removePermission",
            args = {java.security.acl.Permission.class}
        )
    })
    public void test_AclEntry01() {
        Permission perm = new PermissionImpl("Permission_1");
        MyAclEntry ae = new MyAclEntry(new PrincipalImpl("TestPrincipal"));
        try {
            assertTrue(ae.addPermission(perm));
            assertFalse(ae.addPermission(perm));
            assertTrue(ae.checkPermission(perm));
            assertTrue(ae.removePermission(perm));
            assertFalse(ae.removePermission(perm));
            assertFalse(ae.checkPermission(perm));
        } catch (Exception ex) {
            fail("Unexpected exception " + ex);
        }
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "getPrincipal",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "setPrincipal",
            args = {java.security.Principal.class}
        )
    })
    public void test_AclEntry02() {
        MyAclEntry ae = new MyAclEntry();
        Principal mp = new PrincipalImpl("TestPrincipal");
        try {
            assertTrue(ae.setPrincipal(mp));
            Principal p = ae.getPrincipal();
            assertEquals("Names are not equal", p.getName(), mp.getName());
            assertFalse(ae.setPrincipal(mp));
        } catch (Exception ex) {
            fail("Unexpected exception " + ex);
        }
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "setNegativePermissions",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "isNegative",
            args = {}
        )
    })
    public void test_AclEntry03() {
        MyAclEntry ae = new MyAclEntry(new PrincipalImpl("TestPrincipal"));
        try {
            assertFalse("isNegative() returns TRUE",ae.isNegative());
            ae.setNegativePermissions();
            assertTrue("isNegative() returns FALSE", ae.isNegative());
        } catch (Exception ex) {
            fail("Unexpected exception " + ex);
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "permissions",
        args = {}
    )
    public void test_AclEntry04() {
        MyAclEntry ae = new MyAclEntry(new PrincipalImpl("TestPrincipal"));
        Permission perm = new PermissionImpl("Permission_1");
        try {
            Enumeration en = ae.permissions();
            assertFalse("Not empty enumeration", en.hasMoreElements());
            ae.addPermission(perm);
            en = ae.permissions();
            assertTrue("Eempty enumeration", en.hasMoreElements());
            Vector v = new Vector();
            while (en.hasMoreElements()) {
                v.addElement(en.nextElement());
            }
            assertEquals(v.size(), 1);
            assertEquals(v.elementAt(0).toString(), perm.toString());
        } catch (Exception ex) {
            fail("Unexpected exception " + ex);
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "toString",
        args = {}
    )
    public void test_AclEntry05() {
        MyAclEntry ae = new MyAclEntry(new PrincipalImpl("TestPrincipal"));
        try {
            String res = ae.toString();
            assertTrue(res.contains("TestPrincipal"));
        } catch (Exception ex) {
            fail("Unexpected exception " + ex);
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "clone",
        args = {}
    )
    public void test_AclEntry06() {
        MyAclEntry ae = new MyAclEntry(new PrincipalImpl("TestPrincipal"));
        try {
            assertEquals("Objects are not equal", ae.toString(), ae.clone().toString());
        } catch (Exception ex) {
            fail("Unexpected exception " + ex);
        }
    }
}