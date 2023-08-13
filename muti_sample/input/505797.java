@TestTargetClass(Permission.class)
public class Permission2Test extends junit.framework.TestCase {
    static class ConcretePermission extends Permission {
        public ConcretePermission() {
            super("noname");
        }
        public boolean equals(Object obj) {
            return true;
        }
        public String getActions() {
            return "none";
        }
        public int hashCode() {
            return 1;
        }
        public boolean implies(Permission p) {
            return true;
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "Permission",
        args = {java.lang.String.class}
    )
    public void test_ConstructorLjava_lang_String() {
        try {
        SecurityPermission permi = new SecurityPermission(
                "Testing the permission abstract class");
        String name = permi.getName();
        assertEquals("Permission Constructor failed",
                "Testing the permission abstract class", name);
        } catch (Exception e) {
            fail("Unexpected excpetion");
        }
        try {
            SecurityPermission permi = new SecurityPermission(null);
            fail("NullPointerException was not thrown for NULL parameter");
        } catch (NullPointerException e) {
        }
        try {
            SecurityPermission permi = new SecurityPermission("");
            fail("IllegalArgumentException was not thrown for empty parameter");
        } catch (IllegalArgumentException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "checkGuard",
        args = {java.lang.Object.class}
    )
    public void test_checkGuardLjava_lang_Object() {
        SecurityPermission permi = new SecurityPermission(
                "Testing the permission abstract class");
        String name = permi.getName();
        try {
            permi.checkGuard(name);
        } catch (SecurityException e) {
            fail("security not granted when it is suppose to be : " + e);
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getName",
        args = {}
    )
    public void test_getName() {
        SecurityPermission permi = new SecurityPermission("testing getName()");
        String name = permi.getName();
        assertEquals("getName failed to obtain the correct name",
                "testing getName()", name);
        SecurityPermission permi2 = new SecurityPermission("93048Helloworld");
        assertEquals("getName failed to obtain correct name",
                "93048Helloworld", permi2.getName());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "newPermissionCollection",
        args = {}
    )
    public void test_newPermissionCollection() {
        Permission permi = new ConcretePermission();
        PermissionCollection permiCollect = permi.newPermissionCollection();
        assertNull("newPermissionCollector of the abstract class "
                + "permission did not return a null instance "
                + "of permissionCollection", permiCollect);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "toString",
        args = {}
    )
    public void test_toString() {
        SecurityPermission permi = new SecurityPermission("testing toString");
        String toString = permi.toString();
        assertNotNull("toString should have returned a string of elements",
                toString);
    }
}