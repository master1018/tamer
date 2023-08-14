@TestTargetClass(InputMismatchException.class) 
public class InputMismatchExceptionTest extends TestCase {
    private static final String ERROR_MESSAGE = "for serialization test"; 
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "InputMismatchException",
        args = {}
    )
    @SuppressWarnings("cast")
    public void test_Constructor() {
        InputMismatchException exception = new InputMismatchException();
        assertNotNull(exception);
        assertTrue(exception instanceof NoSuchElementException);
        assertTrue(exception instanceof Serializable);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "InputMismatchException",
        args = {java.lang.String.class}
    )
    public void test_ConstructorLjava_lang_String() {
        InputMismatchException exception = new InputMismatchException(
                ERROR_MESSAGE);
        assertNotNull(exception);
        assertEquals(ERROR_MESSAGE, exception.getMessage());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "!SerializationSelf",
        args = {}
    )
    public void testSerializationSelf() throws Exception {
        SerializationTest.verifySelf(new InputMismatchException(ERROR_MESSAGE));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "!SerializationGolden",
        args = {}
    )
    public void testSerializationCompatibility() throws Exception {
        SerializationTest.verifyGolden(this, new InputMismatchException(
                ERROR_MESSAGE));
    }
}
