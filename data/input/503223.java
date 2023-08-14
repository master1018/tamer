@TestTargetClass(NotActiveException.class) 
public class NotActiveExceptionTest extends junit.framework.TestCase {
    @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "NotActiveException",
            args = {}
        )     
    public void test_Constructor() {
        try {
            if (true) 
                throw new NotActiveException();
            fail("Test 1: NotActiveException expected.");
        } catch (NotActiveException e) {
            assertNull("Test 2: Null expected for exceptions constructed without a message.",
                    e.getMessage());
        }
    }
    @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "NotActiveException",
            args = {java.lang.String.class}
        )     
    public void test_ConstructorLjava_lang_String() {
        try {
            if (true) 
                throw new NotActiveException("Something went wrong.");
            fail("Test 1: NotActiveException expected.");
        } catch (NotActiveException e) {
            assertEquals("Test 2: Incorrect message;",
                    "Something went wrong.", e.getMessage());
        }
    }
}
