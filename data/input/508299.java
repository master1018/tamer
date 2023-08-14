@TestTargetClass(UsernameFilterGMail.class)
public class LoginFilter_UsernameFilterGMailTest extends TestCase {
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructor(s) of UsernameFilterGMail.",
            method = "LoginFilter.UsernameFilterGMail",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructor(s) of UsernameFilterGMail.",
            method = "LoginFilter.UsernameFilterGMail",
            args = {boolean.class}
        )
    })
    @ToBeFixed(bug = "1695243", explanation = "miss javadoc")
    public void testConstructor() {
        new UsernameFilterGMail();
        new UsernameFilterGMail(true);
        new UsernameFilterGMail(false);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test isAllowed(char c).",
        method = "isAllowed",
        args = {char.class}
    )
    public void testIsAllowed() {
        UsernameFilterGMail usernameFilterGMail = new UsernameFilterGMail();
        assertTrue(usernameFilterGMail.isAllowed('c'));
        assertTrue(usernameFilterGMail.isAllowed('C'));
        assertTrue(usernameFilterGMail.isAllowed('3'));
        assertFalse(usernameFilterGMail.isAllowed('#'));
        assertFalse(usernameFilterGMail.isAllowed('%'));
        assertTrue(usernameFilterGMail.isAllowed('0'));
        assertTrue(usernameFilterGMail.isAllowed('9'));
        assertTrue(usernameFilterGMail.isAllowed('a'));
        assertTrue(usernameFilterGMail.isAllowed('z'));
        assertTrue(usernameFilterGMail.isAllowed('A'));
        assertTrue(usernameFilterGMail.isAllowed('Z'));
        assertTrue(usernameFilterGMail.isAllowed('.'));
        assertFalse(usernameFilterGMail.isAllowed('/'));
        assertFalse(usernameFilterGMail.isAllowed(':'));
        assertFalse(usernameFilterGMail.isAllowed('`'));
        assertFalse(usernameFilterGMail.isAllowed('{'));
        assertFalse(usernameFilterGMail.isAllowed('@'));
        assertFalse(usernameFilterGMail.isAllowed('['));
    }
}
