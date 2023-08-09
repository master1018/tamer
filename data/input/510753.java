@TestTargetClass(UsernameFilterGeneric.class)
public class LoginFilter_UsernameFilterGenericTest extends TestCase {
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "LoginFilter.UsernameFilterGeneric",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "LoginFilter.UsernameFilterGeneric",
            args = {boolean.class}
        )
    })
    @ToBeFixed(bug = "1695243", explanation = "miss javadoc")
    public void testConstructor() {
        new UsernameFilterGeneric();
        new UsernameFilterGeneric(true);
        new UsernameFilterGeneric(false);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test isAllowed(char c).",
        method = "isAllowed",
        args = {char.class}
    )
    public void testIsAllowed() {
        UsernameFilterGeneric usernameFilterGeneric = new UsernameFilterGeneric();
        assertTrue(usernameFilterGeneric.isAllowed('b'));
        assertTrue(usernameFilterGeneric.isAllowed('B'));
        assertTrue(usernameFilterGeneric.isAllowed('2'));
        assertFalse(usernameFilterGeneric.isAllowed('*'));
        assertFalse(usernameFilterGeneric.isAllowed('!'));
        assertTrue(usernameFilterGeneric.isAllowed('0'));
        assertTrue(usernameFilterGeneric.isAllowed('9'));
        assertTrue(usernameFilterGeneric.isAllowed('a'));
        assertTrue(usernameFilterGeneric.isAllowed('z'));
        assertTrue(usernameFilterGeneric.isAllowed('A'));
        assertTrue(usernameFilterGeneric.isAllowed('Z'));
        assertTrue(usernameFilterGeneric.isAllowed('@'));
        assertTrue(usernameFilterGeneric.isAllowed('_'));
        assertTrue(usernameFilterGeneric.isAllowed('-'));
        assertTrue(usernameFilterGeneric.isAllowed('.'));
        assertFalse(usernameFilterGeneric.isAllowed('/'));
        assertFalse(usernameFilterGeneric.isAllowed(':'));
        assertFalse(usernameFilterGeneric.isAllowed('`'));
        assertFalse(usernameFilterGeneric.isAllowed('{'));
        assertFalse(usernameFilterGeneric.isAllowed('?'));
        assertFalse(usernameFilterGeneric.isAllowed('['));
    }
}
