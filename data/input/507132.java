@TestTargetClass(CharConversionException.class) 
public class CharConversionExceptionTest extends junit.framework.TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "CharConversionException",
        args = {}
    )     
    public void test_Constructor() {
        try {
            if (true) 
                throw new CharConversionException();
            fail("Test 1: CharConversionException expected.");
        } catch (CharConversionException e) {
            assertNull("Test 2: Null expected for exceptions constructed without a message.",
                    e.getMessage());
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "CharConversionException",
        args = {java.lang.String.class}
    )     
    public void test_ConstructorLjava_lang_String() {
        try {
            if (true) 
                throw new CharConversionException("Something went wrong.");
            fail("Test 1: CharConversionException expected.");
        } catch (CharConversionException e) {
            assertEquals("Test 2: Incorrect message;",
                    "Something went wrong.", e.getMessage());
        }
    }
}
