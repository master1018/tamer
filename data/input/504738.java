@TestTargetClass(IOException.class) 
public class IOExceptionTest extends junit.framework.TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "IOException",
        args = {}
    )       
    public void test_Constructor() {
        try {
            if (true) 
                throw new IOException();
            fail("Test 1: IOException expected.");
        } catch (IOException e) {
            assertNull("Test 2: Null expected for exceptions constructed without a message.",
                    e.getMessage());
        }
   }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "IOException",
        args = {java.lang.String.class}
    )      
    public void test_ConstructorLjava_lang_String() {
        try {
            if (true) 
                throw new IOException("Something went wrong.");
            fail("Test 1: IOException expected.");
        } catch (IOException e) {
            assertEquals("Test 2: Incorrect message;",
                    "Something went wrong.", e.getMessage());
        }
    }
}
