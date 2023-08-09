@TestTargetClass(SerializablePermission.class) 
public class SerializablePermissionTest extends junit.framework.TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "SerializablePermission",
        args = {java.lang.String.class}
    )
    public void test_ConstructorLjava_lang_String() {
        assertEquals("permission ill-formed", 
                "enableSubclassImplementation", new SerializablePermission(
                "enableSubclassImplementation").getName());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "SerializablePermission",
        args = {java.lang.String.class, java.lang.String.class}
    )
    public void test_ConstructorLjava_lang_StringLjava_lang_String() {
        assertEquals("permission ill-formed", 
                "enableSubclassImplementation", new SerializablePermission(
                "enableSubclassImplementation", "").getName());
    }
    protected void setUp() {
    }
    protected void tearDown() {
    }
}
