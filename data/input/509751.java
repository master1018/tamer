@TestTargetClass(AuthPermission.class) 
public class AuthPermissionTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "AuthPermission",
        args = {String.class}
    )
    public void test_Constructor_01() {
        String[] strParam = {"", null};
        try {
            AuthPermission ap = new AuthPermission("AuthPermissionName");
            assertNotNull("Null object returned", ap);
            assertEquals("AuthPermissionName", ap.getName());
        } catch (Exception e) {
            fail("Unexpected exception: " + e);
        }
        for (int i = 0; i < strParam.length; i++) {
            try {
                AuthPermission ap = new AuthPermission(strParam[i]);
            } catch (Exception e) {
            }
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "AuthPermission",
        args = {String.class, String.class}
    )
    public void test_Constructor_02() {
        String[] strParam = {"", null};
        String[] actionParam = {"", null, "ActionName"};
        try {
            AuthPermission ap = new AuthPermission("AuthPermissionName", null);
            assertNotNull("Null object returned", ap);
            assertEquals("AuthPermissionName", ap.getName());
            assertEquals("", ap.getActions());
        } catch (Exception e) {
            fail("Unexpected exception: " + e);
        }
        for (int i = 0; i < strParam.length; i++) {
            try {
                AuthPermission ap = new AuthPermission(strParam[i], null);
            } catch (Exception e) {
            }
        }
        for (int i = 0; i < actionParam.length; i++) {
            try {
                AuthPermission ap = new AuthPermission("AuthPermissionName", actionParam[i]);
                assertNotNull("Null object returned", ap);
                assertEquals("", ap.getActions());
            } catch (Exception e) {
                fail("Unexpected exception: " + e);
            }
        }
    }
}
