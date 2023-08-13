@TestTargetClass(UnsupportedCharsetException.class)
public class UnsupportedCharsetExceptionTest extends TestCase {
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "UnsupportedCharsetException",
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
        UnsupportedCharsetException ex = new UnsupportedCharsetException(
                "impossible");
        assertTrue(ex instanceof IllegalArgumentException);
        assertNull(ex.getCause());
        assertEquals(ex.getCharsetName(), "impossible");
        assertTrue(ex.getMessage().indexOf("impossible") != -1);
        ex = new UnsupportedCharsetException("ascii");
        assertNull(ex.getCause());
        assertEquals(ex.getCharsetName(), "ascii");
        assertTrue(ex.getMessage().indexOf("ascii") != -1);
        ex = new UnsupportedCharsetException("");
        assertNull(ex.getCause());
        assertEquals(ex.getCharsetName(), "");
        ex.getMessage();
        ex = new UnsupportedCharsetException(null);
        assertNull(ex.getCause());
        assertNull(ex.getCharsetName());
        assertTrue(ex.getMessage().indexOf("null") != -1);
    }
    private static final SerializableAssert COMPARATOR = new SerializableAssert() {
        public void assertDeserialized(Serializable initial,
                Serializable deserialized) {
            UnsupportedCharsetException initEx = (UnsupportedCharsetException) initial;
            UnsupportedCharsetException desrEx = (UnsupportedCharsetException) deserialized;
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
        SerializationTest.verifySelf(new UnsupportedCharsetException(
                "charsetName"), COMPARATOR);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies serialization.",
        method = "!SerializationGolden",
        args = {}
    )
    public void testSerializationCompatibility() throws Exception {
        SerializationTest.verifyGolden(this, new UnsupportedCharsetException(
                "charsetName"), COMPARATOR);
    }
}
