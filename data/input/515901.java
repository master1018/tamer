@TestTargetClass(RuntimePermission.class) 
public class RuntimePermissionTest extends junit.framework.TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "RuntimePermission",
        args = {java.lang.String.class}
    )
    public void test_ConstructorLjava_lang_String() {
        RuntimePermission r = new RuntimePermission("createClassLoader");
        assertEquals("Returned incorrect name", 
                "createClassLoader", r.getName());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "RuntimePermission",
        args = {java.lang.String.class, java.lang.String.class}
    )
    public void test_ConstructorLjava_lang_StringLjava_lang_String() {
        RuntimePermission r = new RuntimePermission("createClassLoader", null);
        assertEquals("Returned incorrect name", 
                "createClassLoader", r.getName());
    }
}
