@TestTargetClass(BasicPermission.class)
public class BasicPermission2Test extends junit.framework.TestCase {
    public static class BasicPermissionSubclass extends BasicPermission {
        public BasicPermissionSubclass(String name) {
            super(name);
        }
        public BasicPermissionSubclass(String name, String actions) {
            super(name, actions);
        }
    }
    BasicPermission bp = new BasicPermissionSubclass("aName");
    BasicPermission bp2 = new BasicPermissionSubclass("aName", "anAction");
    BasicPermission bp3 = new BasicPermissionSubclass("*");
    BasicPermission bp4 = new BasicPermissionSubclass("this.that");
    BasicPermission bp5 = new BasicPermissionSubclass("this.*");
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Test cases, where parameter name is null (expect NullPointerException) and parameter name is empty (expect IllegalArgumentException) are absent",
        method = "BasicPermission",
        args = {java.lang.String.class}
    )
    public void test_ConstructorLjava_lang_String() {
        assertEquals("Incorrect name returned", "aName", bp.getName());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Test cases, where parameter name is null (expect NullPointerException) and parameter name is empty (expect IllegalArgumentException) are absent",
        method = "BasicPermission",
        args = {java.lang.String.class, java.lang.String.class}
    )
    public void test_ConstructorLjava_lang_StringLjava_lang_String() {
        assertEquals("Incorrect name returned", "aName", bp2.getName());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "equals",
        args = {java.lang.Object.class}
    )
    public void test_equalsLjava_lang_Object() {
        assertTrue("a) Equal objects returned non-equal", bp.equals(bp2));
        assertTrue("b) Equal objects returned non-equal", bp2.equals(bp));
        assertTrue("a) Unequal objects returned equal", !bp.equals(bp3));
        assertTrue("b) Unequal objects returned equal", !bp4.equals(bp5));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getActions",
        args = {}
    )
    public void test_getActions() {
        assertTrue("a) Incorrect actions returned, wanted the empty String", bp
                .getActions().equals(""));
        assertTrue("b) Incorrect actions returned, wanted the empty String",
                bp2.getActions().equals(""));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "hashCode",
        args = {}
    )
    public void test_hashCode() {
        assertTrue("Equal objects should return same hash",
                bp.hashCode() == bp2.hashCode());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "implies",
        args = {java.security.Permission.class}
    )
    public void test_impliesLjava_security_Permission() {
        assertTrue("Equal objects should imply each other", bp.implies(bp2));
        assertTrue("a) should not imply", !bp.implies(bp3));
        assertTrue("b) should not imply", !bp4.implies(bp5));
        assertTrue("a) should imply", bp3.implies(bp5));
        assertTrue("b) should imply", bp5.implies(bp4));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "newPermissionCollection",
        args = {}
    )
    public void test_newPermissionCollection() {
        PermissionCollection bpc = bp.newPermissionCollection();
        bpc.add(bp5);
        bpc.add(bp);
        assertTrue("Should imply", bpc.implies(bp4));
        assertTrue("Should not imply", !bpc.implies(bp3));
    }
}