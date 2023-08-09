@TestTargetClass(UnknownServiceException.class) 
public class UnknownServiceExceptionTest extends junit.framework.TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "UnknownServiceException",
        args = {}
    )
    public void test_Constructor() {
        try {
            new URL("file:
        } catch (UnknownServiceException e) {
            return;
        } catch (Exception e) {
            fail("Wrong exception during test : " + e.getMessage());
        }
        fail("Exception not thrown");
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "UnknownServiceException",
        args = {java.lang.String.class}
    )
    public void test_ConstructorLjava_lang_String() {
        try {
            if (true)
                throw new UnknownServiceException("HelloWorld");
        } catch (UnknownServiceException e) {
            assertTrue("Wrong exception message: " + e.toString(), e
                    .getMessage().equals("HelloWorld"));
            return;
        }
        fail("Constructor failed");
    }
    protected void setUp() {
    }
    protected void tearDown() {
    }
}
