@TestTargetClass(PropertyPermission.class) 
public class PropertyPermissionTest extends junit.framework.TestCase {
    static PropertyPermission javaPP = new PropertyPermission("java.*", "read");
    static PropertyPermission userPP = new PropertyPermission("user.name",
            "read,write");
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "PropertyPermission",
        args = {java.lang.String.class, java.lang.String.class}
    )
    public void test_ConstructorLjava_lang_StringLjava_lang_String() {
        assertTrue("Used to test", true);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "equals",
        args = {java.lang.Object.class}
    )
    public void test_equalsLjava_lang_Object() {
        PropertyPermission equalToJavaPP = new PropertyPermission("java.*",
                "read");
        PropertyPermission notEqualToJavaPP = new PropertyPermission("java.*",
                "read, write");
        PropertyPermission alsoNotEqualToJavaPP = new PropertyPermission(
                "java.home", "read");
        assertTrue("Equal returned false for equal objects", javaPP
                .equals(equalToJavaPP));
        assertTrue("Equal returned true for objects with different names",
                !javaPP.equals(notEqualToJavaPP));
        assertTrue(
                "Equal returned true for objects with different permissions",
                !javaPP.equals(alsoNotEqualToJavaPP));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getActions",
        args = {}
    )
    public void test_getActions() {
        assertEquals("getActions did not return proper action", "read", javaPP
                .getActions());
        assertEquals("getActions did not return proper canonical representation of actions",
                "read,write", userPP.getActions());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "hashCode",
        args = {}
    )
    public void test_hashCode() {
        assertTrue("javaPP returned wrong hashCode",
                javaPP.hashCode() == javaPP.getName().hashCode());
        assertTrue("userPP returned wrong hashCode",
                userPP.hashCode() == userPP.getName().hashCode());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "implies",
        args = {java.security.Permission.class}
    )
    public void test_impliesLjava_security_Permission() {
        PropertyPermission impliedByJavaPP = new PropertyPermission(
                "java.home", "read");
        PropertyPermission notImpliedByJavaPP = new PropertyPermission(
                "java.home", "read,write");
        PropertyPermission impliedByUserPP = new PropertyPermission(
                "user.name", "read,write");
        PropertyPermission alsoImpliedByUserPP = new PropertyPermission(
                "user.name", "write");
        assertTrue("Returned false for implied permission (subset of .*)",
                javaPP.implies(impliedByJavaPP));
        assertTrue("Returned true for unimplied permission", !javaPP
                .implies(notImpliedByJavaPP));
        assertTrue("Returned false for implied permission (equal)", userPP
                .implies(impliedByUserPP));
        assertTrue("Returned false for implied permission (subset of actions)",
                userPP.implies(alsoImpliedByUserPP));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "newPermissionCollection",
        args = {}
    )
    public void test_newPermissionCollection() {
        java.security.PermissionCollection pc = javaPP
                .newPermissionCollection();
        pc.add(javaPP);
        Enumeration elementEnum = pc.elements();
        assertTrue("Invalid PermissionCollection returned", elementEnum
                .nextElement().equals(javaPP));
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Verifies serialization/deserialization.",
            method = "!SerializationSelf",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Verifies serialization/deserialization.",
            method = "!SerializationGolden",
            args = {}
        )
    })
    public void test_serialization() throws Exception{
        PropertyPermission pp = new PropertyPermission("test", "read");
        SerializationTest.verifySelf(pp, comparator);
        SerializationTest.verifyGolden(this, pp, comparator);
    }
    protected void setUp() {
    }
    protected void tearDown() {
    }
    private static final SerializableAssert comparator = new SerializableAssert() {
        public void assertDeserialized(Serializable initial, Serializable deserialized) {
            PropertyPermission initialPP = (PropertyPermission) initial;
            PropertyPermission deseriaPP = (PropertyPermission) deserialized;
            assertEquals("should be equal", initialPP, deseriaPP);
        }
    };
}
