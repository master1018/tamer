@TestTargetClass(MalformedInputException.class)
public class MalformedInputExceptionTest extends TestCase {
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "MalformedInputException",
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
        MalformedInputException ex = new MalformedInputException(3);
        assertTrue(ex instanceof CharacterCodingException);
        assertNull(ex.getCause());
        assertEquals(ex.getInputLength(), 3);
        assertTrue(ex.getMessage().indexOf("3") != -1);
        ex = new MalformedInputException(-3);
        assertNull(ex.getCause());
        assertEquals(ex.getInputLength(), -3);
        assertTrue(ex.getMessage().indexOf("-3") != -1);
        ex = new MalformedInputException(0);
        assertNull(ex.getCause());
        assertEquals(ex.getInputLength(), 0);
        assertTrue(ex.getMessage().indexOf("0") != -1);
    }
    private static final SerializableAssert COMPARATOR = new SerializableAssert() {
        public void assertDeserialized(Serializable initial,
                Serializable deserialized) {
            SerializationTest.THROWABLE_COMPARATOR.assertDeserialized(initial,
                    deserialized);
            MalformedInputException initEx = (MalformedInputException) initial;
            MalformedInputException desrEx = (MalformedInputException) deserialized;
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
        SerializationTest.verifySelf(new MalformedInputException(11),
                COMPARATOR);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies serialization.",
        method = "!SerializationGolden",
        args = {}
    )
    public void testSerializationCompatibility() throws Exception {
        SerializationTest.verifyGolden(this, new MalformedInputException(11),
                COMPARATOR);
    }
}
