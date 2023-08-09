@TestTargetClass(ReflectPermission.class) 
public class ReflectPermissionTest extends junit.framework.TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "ReflectPermission",
        args = {java.lang.String.class}
    )
    public void test_ConstructorLjava_lang_String() {
        String permString = new ReflectPermission("Blah").toString();
        assertTrue("Incorrect permission constructed", permString.indexOf(
                "java.lang.reflect.ReflectPermission Blah") >= 0);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "ReflectPermission",
        args = {java.lang.String.class, java.lang.String.class}
    )
    public void test_ConstructorLjava_lang_StringLjava_lang_String() {
        assertTrue("Incorrect permission constructed", new ReflectPermission(
                "Blah", "suppressAccessChecks").toString().indexOf(
                "java.lang.reflect.ReflectPermission Blah") >= 0);
    }
    protected void setUp() {
    }
    protected void tearDown() {
    }
}
