@TestTargetClass(UTFDataFormatException.class) 
public class UTFDataFormatExceptionTest extends junit.framework.TestCase {
    @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "UTFDataFormatException",
            args = {}
        )     
    public void test_Constructor() {
        try {
            if (true) 
                throw new UTFDataFormatException();
            fail("Test 1: UTFDataFormatException expected.");
        } catch (UTFDataFormatException e) {
            assertNull("Test 2: Null expected for exceptions constructed without a message.",
                    e.getMessage());
        }
    }
    @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "UTFDataFormatException",
            args = {java.lang.String.class}
        )     
    public void test_ConstructorLjava_lang_String() {
        try {
            if (true) 
                throw new UTFDataFormatException("Something went wrong.");
            fail("Test 1: UTFDataFormatException expected.");
        } catch (UTFDataFormatException e) {
            assertEquals("Test 2: Incorrect message;",
                    "Something went wrong.", e.getMessage());
        }
    }
}
