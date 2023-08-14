@TestTargetClass(IllegalCharsetNameException.class)
public class IllegalCharsetNameExceptionTest extends TestCase {
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "IllegalCharsetNameException",
            args = {java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "getCharsetName",
            args = {}
        )
    })
    public void testConstructor() {
        IllegalCharsetNameException ex = new IllegalCharsetNameException(
                "impossible");
        assertTrue(ex instanceof IllegalArgumentException);
        assertNull(ex.getCause());
        assertEquals(ex.getCharsetName(), "impossible");
        assertTrue(ex.getMessage().indexOf("impossible") != -1);
        ex = new IllegalCharsetNameException("ascii");
        assertNull(ex.getCause());
        assertEquals(ex.getCharsetName(), "ascii");
        assertTrue(ex.getMessage().indexOf("ascii") != -1);
        ex = new IllegalCharsetNameException("");
        assertNull(ex.getCause());
        assertEquals(ex.getCharsetName(), "");
        ex.getMessage();
        ex = new IllegalCharsetNameException(null);
        assertNull(ex.getCause());
        assertNull(ex.getCharsetName());
        assertTrue(ex.getMessage().indexOf("null") != -1);
    }
    private static final SerializableAssert COMPARATOR = new SerializableAssert() {
        public void assertDeserialized(Serializable initial,
                Serializable deserialized) {
            IllegalCharsetNameException initEx = (IllegalCharsetNameException) initial;
            IllegalCharsetNameException desrEx = (IllegalCharsetNameException) deserialized;
            assertEquals("CharsetName", initEx.getCharsetName(), desrEx
                    .getCharsetName());
        }
    };
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies serialization.",
        method = "!SerializationSelf",
        args = {}
    )
    public void testSerializationSelf() throws Exception {
        SerializationTest.verifySelf(new IllegalCharsetNameException(
                "charsetName"), COMPARATOR);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies serialization.",
        method = "!SerializationGolden",
        args = {}
    )
    public void testSerializationCompatibility() throws Exception {
        SerializationTest.verifyGolden(this, new IllegalCharsetNameException(
                "charsetName"), COMPARATOR);
    }
}
