@TestTargetClass(WriteAbortedException.class) 
public class WriteAbortedExceptionTest extends junit.framework.TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "WriteAbortedException",
        args = {java.lang.String.class, java.lang.Exception.class}
    )
    public void test_ConstructorLjava_lang_StringLjava_lang_Exception() {
        try {
            if (true)
                throw new WriteAbortedException("HelloWorld",
                        new WriteAbortedException("ByeWorld", null));
        } catch (WriteAbortedException e) {
            return;
        }
        fail("Failed to generate expected Exception");
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getCause",
        args = {}
    )
    public void test_getCause() {
        try {
            if (true) {
                throw new WriteAbortedException("HelloWorld",
                        new IOException("Something went wrong."));
            }
            fail("Test 1: WriteAbortedException expected.");
        } catch (WriteAbortedException e) {
            Throwable cause = e.getCause();
            assertTrue("Test 2: Incorrect exception cause: " + cause,
                    cause.getClass().equals(IOException.class) && 
                    cause.getMessage().equals("Something went wrong."));
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getMessage",
        args = {}
    )
    public void test_getMessage() {
        try {
            if (true)
                throw new WriteAbortedException("HelloWorld",
                        new WriteAbortedException("ByeWorld", null));
        } catch (WriteAbortedException e) {
            assertTrue("WriteAbortedException::getMessage() failed"
                    + e.getMessage(), e.getMessage().equals(
                    "HelloWorld; java.io.WriteAbortedException: ByeWorld"));
            return;
        }
        fail("Failed to generate expected Exception");
    }
}
