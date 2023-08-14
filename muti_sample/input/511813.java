@TestTargetClass(NetPermission.class) 
public class NetPermissionTest extends junit.framework.TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "NetPermission",
        args = {java.lang.String.class}
    )
    public void test_ConstructorLjava_lang_String() {
        NetPermission n = new NetPermission("requestPasswordAuthentication");
        assertEquals("Returned incorrect name", 
                "requestPasswordAuthentication", n.getName());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "NetPermission",
        args = {java.lang.String.class, java.lang.String.class}
    )
    public void test_ConstructorLjava_lang_StringLjava_lang_String() {
        NetPermission n = new NetPermission("requestPasswordAuthentication",
                null);
        assertEquals("Returned incorrect name", 
                "requestPasswordAuthentication", n.getName());
        NetPermission n1 = new NetPermission("requestPasswordAuthentication",
                "");
        assertEquals("", n1.getActions());
    }
    protected void setUp() {
    }
    protected void tearDown() {
    }
}
