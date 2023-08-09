@TestTargetClass(AllPermission.class)
public class AllPermission2Test extends junit.framework.TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "AllPermission",
        args = {}
    )
    public void test_Constructor() {
        AllPermission ap = new AllPermission();
        assertEquals("Bogus name for AllPermission \"" + ap.getName() + "\".",
                "<all permissions>", ap.getName());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Null/empty parameters checking missed",
        method = "AllPermission",
        args = {java.lang.String.class, java.lang.String.class}
    )
    public void test_ConstructorLjava_lang_StringLjava_lang_String() {
        AllPermission ap = new AllPermission("Don't remember this stupid name",
                "or this action");
        assertEquals("Bogus name for AllPermission \"" + ap.getName() + "\".",
                "<all permissions>", ap.getName());
        assertEquals(
                "AllPermission constructed with actions didn't ignore them.",
                "<all actions>", ap.getActions());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Update comment for first assertTrue method.Because: Two AllPermission objects are always equal",
        method = "equals",
        args = {java.lang.Object.class}
    )
    public void test_equalsLjava_lang_Object() {
        assertTrue("Two AllPermissions not equal to each other.",
                new AllPermission().equals(new AllPermission()));
        assertTrue("AllPermission equals a SecurityPermission.",
                !(new AllPermission().equals(new SecurityPermission("ugh!"))));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getActions",
        args = {}
    )
    public void test_getActions() {
        AllPermission ap = new AllPermission();
        assertTrue("AllPermission has non-empty actions. (" + ap.getActions()
                + ")", ap.getActions().equals("<all actions>"));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "hashCode",
        args = {}
    )
    public void test_hashCode() {
        final int ALLPERMISSION_HASH = 1;
        AllPermission TestAllPermission = new AllPermission();
        assertTrue("AllPermission hashCode is wrong. Should have been "
                + ALLPERMISSION_HASH + " but was "
                + TestAllPermission.hashCode(),
                TestAllPermission.hashCode() == ALLPERMISSION_HASH);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "implies",
        args = {java.security.Permission.class}
    )
    public void test_impliesLjava_security_Permission() {
        assertTrue("AllPermission does not imply a AllPermission.",
                new AllPermission().implies(new AllPermission()));
        assertTrue("AllPermission does not imply a SecurityPermission.",
                new AllPermission().implies(new SecurityPermission("ugh!")));
        assertTrue("SecurityPermission implies AllPermission.",
                !(new SecurityPermission("ugh!").implies(new AllPermission())));
        assertTrue("AllPermission does not imply when parametr NULL", new AllPermission().implies(null));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "newPermissionCollection",
        args = {}
    )
    public void test_newPermissionCollection() {
        AllPermission ap1 = new AllPermission();
        AllPermission ap2 = new AllPermission("Don't remember this stupid name",
        "or this action");
        AllPermission ap3 = new AllPermission("Remember this cool name",
        "and this action");
        PermissionCollection pc1 = ap1.newPermissionCollection();
        assertFalse(pc1.isReadOnly());
        Enumeration<Permission> perm1 = pc1.elements();
        assertFalse(perm1.hasMoreElements());
        assertNotNull(perm1);
        pc1.add(ap1);
        pc1.add(ap2);
        assertTrue("Should imply", pc1.implies(ap1));
        assertTrue("Should imply", pc1.implies(ap2));
        assertTrue("Should imply", pc1.implies(ap3));
        perm1 = pc1.elements();
        assertTrue(perm1.hasMoreElements());
        PermissionCollection pc2 = ap2.newPermissionCollection();
        assertFalse(pc2.isReadOnly());
        Enumeration<Permission> perm2 = pc2.elements();
        assertFalse(perm2.hasMoreElements());
        assertNotNull(perm2);
        pc2.add(ap1);
        pc2.add(ap2);
        assertTrue("Should imply", pc2.implies(ap1));
        assertTrue("Should imply", pc2.implies(ap2));
        assertTrue("Should imply", pc2.implies(ap3));
        perm2 = pc2.elements();
        assertTrue(perm2.hasMoreElements());
    }
}