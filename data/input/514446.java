@TestTargetClass(UnsupportedEncodingException.class) 
public class UnsupportedEncodingExceptionTest extends junit.framework.TestCase {
    @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "UnsupportedEncodingException",
            args = {}
        )     
    public void test_Constructor() {
        try {
            if (true) 
                throw new UnsupportedEncodingException();
            fail("Test 1: UnsupportedEncodingException expected.");
        } catch (UnsupportedEncodingException e) {
            assertNull("Test 2: Null expected for exceptions constructed without a message.",
                    e.getMessage());
        }
    }
    @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "UnsupportedEncodingException",
            args = {java.lang.String.class}
        )     
    public void test_ConstructorLjava_lang_String() {
        try {
            if (true) 
                throw new UnsupportedEncodingException("Something went wrong.");
            fail("Test 1: UnsupportedEncodingException expected.");
        } catch (UnsupportedEncodingException e) {
            assertEquals("Test 2: Incorrect message;",
                    "Something went wrong.", e.getMessage());
        }
    }
}
