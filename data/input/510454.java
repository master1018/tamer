@TestTargetClass(java.io.InvalidObjectException.class) 
public class InvalidObjectExceptionTest extends junit.framework.TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies the InvalidObjectException(java.lang.String) constructor.",
        method = "InvalidObjectException",
        args = {java.lang.String.class}
    )     
    public void test_ConstructorLjava_lang_String() {
        try {
            if (true) throw new InvalidObjectException("This object is not valid.");
            fail("Exception not thrown.");
        } catch (InvalidObjectException e) {
            assertEquals("The exception message is not equal to the one " +
                         "passed to the constructor.",
                         "This object is not valid.", e.getMessage());
        }
    }
}
