@TestTargetClass(SyncFailedException.class) 
public class SyncFailedExceptionTest extends junit.framework.TestCase {
    @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "SyncFailedException",
            args = {java.lang.String.class}
        )     
    public void test_ConstructorLjava_lang_String() {
        try {
            if (true) 
                throw new SyncFailedException("Something went wrong.");
            fail("Test 1: SyncFailedException expected.");
        } catch (SyncFailedException e) {
            assertEquals("Test 2: Incorrect message;",
                    "Something went wrong.", e.getMessage());
        }
    }
}
