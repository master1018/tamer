@TestTargetClass(SecurityPermission.class)
public class SecurityPermission2Test extends junit.framework.TestCase {
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Verification with valid parameter only",
        method = "SecurityPermission",
        args = {java.lang.String.class}
    )
    public void test_ConstructorLjava_lang_String() {
        assertEquals("create securityPermission constructor(string) failed",
                "SecurityPermission(string)", new SecurityPermission("SecurityPermission(string)").getName()
                        );
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Verification with valid parameters only",
        method = "SecurityPermission",
        args = {java.lang.String.class, java.lang.String.class}
    )
    public void test_ConstructorLjava_lang_StringLjava_lang_String() {
        SecurityPermission sp = new SecurityPermission("security.file", "write");
        assertEquals("creat securityPermission constructor(string,string) failed",
                "security.file", sp.getName());
    }
}