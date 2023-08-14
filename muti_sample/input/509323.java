@TestTargetClass(FileNotFoundException.class) 
public class FileNotFoundExceptionTest extends junit.framework.TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "FileNotFoundException",
        args = {}
    )        
    public void test_Constructor() {
        try {
            if (true) 
                throw new FileNotFoundException();
            fail("Test 1: FileNotFoundException expected.");
        } catch (FileNotFoundException e) {
            assertNull("Test 2: Null expected for exceptions constructed without a message.",
                    e.getMessage());
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "FileNotFoundException",
        args = {java.lang.String.class}
    )        
    public void test_ConstructorLjava_lang_String() {
        try {
            if (true) 
                throw new FileNotFoundException("Something went wrong.");
            fail("Test 1: FileNotFoundException expected.");
        } catch (FileNotFoundException e) {
            assertEquals("Test 2: Incorrect message;",
                    "Something went wrong.", e.getMessage());
        }
    }
}
