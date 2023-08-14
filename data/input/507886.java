@TestTargetClass(InvalidMarkException.class)
public class InvalidMarkExceptionTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies serialization/deserialization compatibility.",
        method = "!SerializationSelf",
        args = {}
    )    
    public void testSerializationSelf() throws Exception {
        SerializationTest.verifySelf(new InvalidMarkException());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies serialization/deserialization compatibility.",
        method = "!SerializationGolden",
        args = {}
    )    
    public void testSerializationCompatibility() throws Exception {
        SerializationTest.verifyGolden(this, new InvalidMarkException());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "InvalidMarkException",
        args = {}
    )
    public void test_Constructor() {
        InvalidMarkException exception = new InvalidMarkException();
        assertNull(exception.getMessage());
        assertNull(exception.getLocalizedMessage());
        assertNull(exception.getCause());
    }
}
