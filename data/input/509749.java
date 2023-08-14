@TestTargetClass(NotSerializableException.class) 
public class NotSerializableExceptionTest extends junit.framework.TestCase {
    @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "NotSerializableException",
            args = {}
        )     
    public void test_Constructor() {
        try {
            if (true) 
                throw new NotSerializableException();
            fail("Test 1: NotSerializableException expected.");
        } catch (NotSerializableException e) {
            assertNull("Test 2: Null expected for exceptions constructed without a message.",
                    e.getMessage());
        }
    }
    @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "NotSerializableException",
            args = {java.lang.String.class}
        )     
    public void test_ConstructorLjava_lang_String() {
        try {
            if (true) 
                throw new NotSerializableException("Something went wrong.");
            fail("Test 1: NotSerializableException expected.");
        } catch (NotSerializableException e) {
            assertEquals("Test 2: Incorrect message;",
                    "Something went wrong.", e.getMessage());
        }
    }
}
