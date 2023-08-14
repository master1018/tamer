@TestTargetClass(Proxy.Type.class) 
public class ProxyTypeTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "valueOf",
        args = {java.lang.String.class}
    )
    public void test_valueOf() {
        Proxy.Type [] types = {Proxy.Type.DIRECT, Proxy.Type.HTTP, 
                Proxy.Type.SOCKS};
        String [] strTypes = {"DIRECT", "HTTP", "SOCKS"}; 
        for(int i = 0; i < strTypes.length; i++) {
            assertEquals(types[i], Proxy.Type.valueOf(strTypes[i]));
        }
        String [] incTypes = {"", "direct", "http", "socks", " HTTP"};
        for(String str:incTypes) {
            try {
                Proxy.Type.valueOf(str);
                fail("IllegalArgumentException was not thrown.");
            } catch(IllegalArgumentException iae) {
            }
        }
        try {
            Proxy.Type.valueOf(null);
            fail("NullPointerException was not thrown.");
        } catch(NullPointerException npe) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "values",
        args = {}
    )
    public void test_values() {
        Proxy.Type [] types = { Proxy.Type.DIRECT, Proxy.Type.HTTP, 
                Proxy.Type.SOCKS };
        Proxy.Type [] result = Proxy.Type.values();
        assertEquals(types.length, result.length);
        for(int i = 0; i < result.length; i++) {
         assertEquals(types[i], result[i]);   
        }
    }
}
