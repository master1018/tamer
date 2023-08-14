@TestTargetClass(SSLPermission.class)
public class SSLPermissionTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "SSLPermission",
        args = {String.class}
    )
    public void test_ConstructorLjava_lang_String() {
        try {
            SSLPermission p = new SSLPermission("name");
            assertEquals("Incorrect permission name", "name", p.getName());
            try {
                p = new SSLPermission(null);
            } catch (NullPointerException npe) {
            }
        } catch (Exception e) {
            fail("Unexpected exception " + e.toString());
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "SSLPermission",
        args = {String.class, String.class}
    )
    public void test_ConstructorLjava_lang_StringLjava_lang_String() {
        try {
            SSLPermission p = new SSLPermission("name", "value");
            assertEquals("Incorrect permission name", "name", p.getName());
            assertEquals("Incorrect default permission actions",
                    "", p.getActions());
            try {
                p = new SSLPermission(null, "value");
            } catch (NullPointerException npe) {
            }
            try {
                p = new SSLPermission("name", null);
            } catch (NullPointerException npe) {
            }
            try {
                p = new SSLPermission(null, null);
            } catch (NullPointerException npe) {
            }
        } catch (Exception e) {
            fail("Unexpected exception " + e.toString());
        }
    }
}
