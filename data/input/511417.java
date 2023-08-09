@TestTargetClass(StreamCorruptedException.class) 
public class StreamCorruptedExceptionTest extends junit.framework.TestCase {
    @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "StreamCorruptedException",
            args = {}
        )     
    public void test_Constructor() {
        try {
            if (true) 
                throw new StreamCorruptedException();
            fail("Test 1: StreamCorruptedException expected.");
        } catch (StreamCorruptedException e) {
            assertNull("Test 2: Null expected for exceptions constructed without a message.",
                    e.getMessage());
        }
    }
    @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "StreamCorruptedException",
            args = {java.lang.String.class}
        )     
    public void test_ConstructorLjava_lang_String() {
        try {
            if (true) 
                throw new StreamCorruptedException("Something went wrong.");
            fail("Test 1: StreamCorruptedException expected.");
        } catch (StreamCorruptedException e) {
            assertEquals("Test 2: Incorrect message;",
                    "Something went wrong.", e.getMessage());
        }
    }
}
