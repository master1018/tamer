@TestTargetClass(java.net.UnknownHostException.class) 
public class UnknownHostExceptionTest extends junit.framework.TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "UnknownHostException",
        args = {}
    )
    public void test_Constructor() {
        try {
            try {
                java.net.InetAddress.getByName("a.b.c.x.y.z.com");
            } catch (java.net.UnknownHostException e) {
                return;
            }
            fail("Failed to generate Exception");
        } catch (Exception e) {
            fail("Exception during test : " + e.getMessage());
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "UnknownHostException",
        args = {java.lang.String.class}
    )
    public void test_ConstructorLjava_lang_String() {
        try {
            try {
                java.net.InetAddress.getByName("a.b.c.x.y.z.com");
            } catch (java.net.UnknownHostException e) {
                return;
            }
            fail("Failed to generate Exception");
        } catch (Exception e) {
            fail("Exception during test : " + e.getMessage());
        }
    }
    protected void setUp() {
    }
    protected void tearDown() {
    }
}
