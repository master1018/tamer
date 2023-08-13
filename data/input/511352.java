@TestTargetClass(IllegalFormatCodePointException.class) 
public class IllegalFormatCodePointExceptionTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "IllegalFormatCodePointException",
        args = {int.class}
    )
    public void test_illegalFormatCodePointException() {
        IllegalFormatCodePointException illegalFormatCodePointException = new IllegalFormatCodePointException(
                -1);
        assertTrue(null != illegalFormatCodePointException);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getCodePoint",
        args = {}
    )
    public void test_getCodePoint() {
        int codePoint = 12345;
        IllegalFormatCodePointException illegalFormatCodePointException = new IllegalFormatCodePointException(
                codePoint);
        assertEquals(codePoint, illegalFormatCodePointException.getCodePoint());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getMessage",
        args = {}
    )
    public void test_getMessage() {
        int codePoint = 12345;
        IllegalFormatCodePointException illegalFormatCodePointException = new IllegalFormatCodePointException(
                codePoint);
        assertTrue(null != illegalFormatCodePointException.getMessage());
    }
    private static final SerializableAssert exComparator = new SerializableAssert() {
        public void assertDeserialized(Serializable initial,
                Serializable deserialized) {
            SerializationTest.THROWABLE_COMPARATOR.assertDeserialized(initial,
                    deserialized);
            IllegalFormatCodePointException initEx = (IllegalFormatCodePointException) initial;
            IllegalFormatCodePointException desrEx = (IllegalFormatCodePointException) deserialized;
            assertEquals("CodePoint", initEx.getCodePoint(), desrEx
                    .getCodePoint());
        }
    };
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "!SerializationSelf",
        args = {}
    )
    public void testSerializationSelf() throws Exception {
        SerializationTest.verifySelf(
                new IllegalFormatCodePointException(12345), exComparator);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "!SerializationGolden",
        args = {}
    )
    public void testSerializationCompatibility() throws Exception {
        SerializationTest.verifyGolden(this,
                new IllegalFormatCodePointException(12345), exComparator);
    }
}
