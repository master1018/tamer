@TestTargetClass(CharacterCodingException.class)
public class CharacterCodingExceptionTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "CharacterCodingException",
        args = {}
    )
    public void testConstructor() {
        CharacterCodingException ex = new CharacterCodingException();
        assertTrue(ex instanceof IOException);
        assertNull(ex.getCause());
        assertNull(ex.getMessage());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies serialization.",
        method = "!SerializationSelf",
        args = {}
    )
    public void testSerializationSelf() throws Exception {
        SerializationTest.verifySelf(new CharacterCodingException());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies serialization.",
        method = "!SerializationGolden",
        args = {}
    )
    public void testSerializationCompatibility() throws Exception {
        SerializationTest.verifyGolden(this, new CharacterCodingException());
    }
}
