@TestTargetClass(PasswordCallback.class) 
public class PasswordCallbackTest extends TestCase {
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "PasswordCallback",
            args = {String.class, boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "getPrompt",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "isEchoOn",
            args = {}
        )
    })
    public void test_PasswordCallback() {
        String prompt = "promptTest";
        try {
            PasswordCallback pc = new PasswordCallback(prompt, true);
            assertNotNull("Null object returned", pc);
            assertEquals(prompt, pc.getPrompt());
            assertEquals(true, pc.isEchoOn());
        } catch (Exception e) {
            fail("Unexpected exception: " + e);
        }
        try {
            PasswordCallback pc = new PasswordCallback(prompt, false);
            assertNotNull("Null object returned", pc);
            assertEquals(prompt, pc.getPrompt());
            assertEquals(false, pc.isEchoOn());
        } catch (Exception e) {
            fail("Unexpected exception: " + e);
        }
        try {
            PasswordCallback pc = new PasswordCallback(null, true);
            fail("IllegalArgumentException wasn't thrown");
        } catch (IllegalArgumentException npe) {
        } 
        try {
            PasswordCallback pc = new PasswordCallback("", true);
            fail("IllegalArgumentException wasn't thrown");
        } catch (IllegalArgumentException npe) {
        } 
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "getPassword",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "setPassword",
            args = {char[].class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "clearPassword",
            args = {}
        )
    })
    public void test_Password() {
        String prompt = "promptTest";
        char[] psw1 = "testPassword".toCharArray();
        char[] psw2 = "newPassword".toCharArray();
        PasswordCallback pc = new PasswordCallback(prompt, true);
        try {
            assertNull(pc.getPassword());
            pc.setPassword(psw1);
            assertEquals(psw1.length, pc.getPassword().length);
            pc.setPassword(null);
            assertNull(pc.getPassword());
            pc.setPassword(psw2);
            char[] res = pc.getPassword();
            assertEquals(psw2.length, res.length);
            for (int i = 0; i < res.length; i++) {
                assertEquals("Incorrect password was returned", psw2[i], res[i]);
            }
            pc.clearPassword();
            res = pc.getPassword();
            if (res.equals(psw2)) {
                fail("Incorrect password was returned after clear");
            }
            pc.setPassword(psw1);
            res = pc.getPassword();
            assertEquals(psw1.length, res.length);
            for (int i = 0; i < res.length; i++) {
                assertEquals("Incorrect result", psw1[i], res[i]);
            }
        } catch (Exception e) {
            fail("Unexpected exception: " + e);
        }
    }
}
