@TestTargetClass(PasswordAuthentication.class) 
public class PasswordAuthenticationTest extends junit.framework.TestCase {
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "PasswordAuthentication",
            args = {java.lang.String.class, char[].class}
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
            method = "getUserName",
            args = {}
        )
    })
    public void test_ConstructorLjava_lang_String$C() {
        char[] password = new char[] { 'd', 'r', 'o', 'w', 's', 's', 'a', 'p' };
        final String name = "Joe Blow";
        PasswordAuthentication pa = new PasswordAuthentication(name, password);
        char[] returnedPassword = pa.getPassword();
        assertTrue("Incorrect name", pa.getUserName().equals(name));
        assertTrue("Password was not cloned", returnedPassword != password);
        assertTrue("Passwords not equal length",
                returnedPassword.length == password.length);
        for (int counter = password.length - 1; counter >= 0; counter--)
            assertTrue("Passwords not equal",
                    returnedPassword[counter] == password[counter]);
        try {
            new PasswordAuthentication(name, null);
            fail("NullPointerException was not thrown.");
        } catch(NullPointerException npe) {
        }
        pa = new PasswordAuthentication(null, password);
        assertNull(pa.getUserName());
        assertEquals(password.length, pa.getPassword().length);
      }
    protected void setUp() {
    }
    protected void tearDown() {
    }
}
