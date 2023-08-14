@TestTargetClass(UnmappableCharacterException.class)
public class UnmappableCharacterExceptionTest extends TestCase {
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "UnmappableCharacterException",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "getMessage",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "getInputLength",
            args = {}
        )
    })
    public void testConstructor() {
        UnmappableCharacterException ex = new UnmappableCharacterException(3);
        assertTrue(ex instanceof CharacterCodingException);
        assertNull(ex.getCause());
        assertEquals(ex.getInputLength(), 3);
        assertTrue(ex.getMessage().indexOf("3") != -1);
        ex = new UnmappableCharacterException(-3);
        assertNull(ex.getCause());
        assertEquals(ex.getInputLength(), -3);
        assertTrue(ex.getMessage().indexOf("-3") != -1);
        ex = new UnmappableCharacterException(0);
        assertNull(ex.getCause());
        assertEquals(ex.getInputLength(), 0);
        assertTrue(ex.getMessage().indexOf("0") != -1);
    }
    private static final SerializableAssert COMPARATOR = new SerializableAssert() {
        public void assertDeserialized(Serializable initial,
                Serializable deserialized) {
            SerializationTest.THROWABLE_COMPARATOR.assertDeserialized(initial,
                    deserialized);
            UnmappableCharacterException initEx = (UnmappableCharacterException) initial;
            UnmappableCharacterException desrEx = (UnmappableCharacterException) deserialized;
            assertEquals("InputLength", initEx.getInputLength(), desrEx
                    .getInputLength());
        }
    };
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies serialization.",
        method = "!SerializationSelf",
        args = {}
    )
    public void testSerializationSelf() throws Exception {
        SerializationTest.verifySelf(new UnmappableCharacterException(11),
                COMPARATOR);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies serialization.",
        method = "!SerializationGolden",
        args = {}
    )
    public void testSerializationCompatibility() throws Exception {
        SerializationTest.verifyGolden(this, new UnmappableCharacterException(
                11), COMPARATOR);
    }
}
