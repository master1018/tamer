@TestTargetClass(NoRouteToHostException.class) 
public class NoRouteToHostExceptionTest extends junit.framework.TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "NoRouteToHostException",
        args = {}
    )
    public void test_Constructor() {
        try {
            if (true)
                throw new NoRouteToHostException();
        } catch (NoRouteToHostException e) {
            return;
        }
        fail("Failed to generate expected exception");
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "NoRouteToHostException",
        args = {java.lang.String.class}
    )
    public void test_ConstructorLjava_lang_String() {
        try {
            if (true)
                throw new NoRouteToHostException("test");
        } catch (NoRouteToHostException e) {
            assertEquals("Threw exception with incorrect message", "test", e.getMessage()
                    );
            return;
        }
        fail("Failed to generate expected exception");
    }
    protected void setUp() {
    }
    protected void tearDown() {
    }
}
