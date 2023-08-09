@TestTargetClass(Authenticator.RequestorType.class) 
public class AuthenticatorRequestorTypeTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "valueOf",
        args = {java.lang.String.class}
    )   
    public void test_valueOfLjava_lang_String() {
        assertEquals(Authenticator.RequestorType.PROXY, 
                Authenticator.RequestorType.valueOf("PROXY"));
        assertEquals(Authenticator.RequestorType.SERVER, 
                Authenticator.RequestorType.valueOf("SERVER"));
        try {
            Authenticator.RequestorType.valueOf("TEST");
            fail("IllegalArgumentException was not thrown.");
        } catch(IllegalArgumentException iae) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "values",
        args = {}
    )    
    public void test_values () {
        Authenticator.RequestorType[] expectedTypes = {
                Authenticator.RequestorType.PROXY,
                Authenticator.RequestorType.SERVER
        };
        Authenticator.RequestorType[] types = 
            Authenticator.RequestorType.values();
        assertEquals(expectedTypes.length, types.length);
        for(int i = 0; i < expectedTypes.length; i++) {
            assertEquals(expectedTypes[i], types[i]);
        }
    }
}
