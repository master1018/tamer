@TestTargetClass(TooManyListenersException.class) 
public class TooManyListenersExceptionTest extends junit.framework.TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "TooManyListenersException",
        args = {}
    )
    public void test_Constructor() {
        try {
            throw new TooManyListenersException();
        } catch (TooManyListenersException e) {
            assertNull(
                    "Message thrown with exception constructed with no message",
                    e.getMessage());
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "TooManyListenersException",
        args = {java.lang.String.class}
    )
    public void test_ConstructorLjava_lang_String() {
        try {
            throw new TooManyListenersException("Gah");
        } catch (TooManyListenersException e) {
            assertEquals("Incorrect message thrown with exception", "Gah", e
                    .getMessage());
        }
    }
    protected void setUp() {
    }
    protected void tearDown() {
    }
}
