@TestTargetClass(KeyStore.PasswordProtection.class)
public class KSPasswordProtectionTest extends TestCase {
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "PasswordProtection",
            args = {char[].class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "getPassword",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "isDestroyed",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            notes = "",
            method = "destroy",
            args = {}
        )
    })
    public void testGetPassword() throws DestroyFailedException {
        char [] pass = {'a', 'b', 'c'};
        KeyStore.PasswordProtection ksPWP = new KeyStore.PasswordProtection(pass);
        char [] rPass = ksPWP.getPassword();
        assertFalse("PasswordProtection Should not be destroyed", ksPWP.isDestroyed());        
        assertEquals("Incorrect password length", pass.length, rPass.length);
        for (int i = 0; i < pass.length; i++) {
            assertEquals("Incorrect password (item: ".concat(Integer.toString(i))
                    .concat(")"), pass[i], rPass[i]);
        }
        ksPWP.destroy();
        assertTrue("PasswordProtection must be destroyed", ksPWP.isDestroyed());
        try {
            ksPWP.getPassword();
            fail("IllegalStateException must be thrown because PasswordProtection is destroyed");
        } catch (IllegalStateException e) {
        }
        try {
            ksPWP = new KeyStore.PasswordProtection(null);
        } catch (Exception e) {
            fail("Unexpected exception for NULL parameter");
        }
    }
}
