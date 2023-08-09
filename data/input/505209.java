@TestTargetClass(IllegalFormatWidthException.class) 
public class IllegalFormatWidthExceptionTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "IllegalFormatWidthException",
        args = {int.class}
    )
    public void test_illegalFormatWidthException() {
        int width = Integer.MAX_VALUE;
        IllegalFormatWidthException illegalFormatWidthException = new IllegalFormatWidthException(
                width);
        assertEquals(width, illegalFormatWidthException.getWidth());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getWidth",
        args = {}
    )
    public void test_getWidth() {
        int width = 12345;
        IllegalFormatWidthException illegalFormatWidthException = new IllegalFormatWidthException(
                width);
        assertEquals(width, illegalFormatWidthException.getWidth());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getMessage",
        args = {}
    )
    public void test_getMessage() {
        int width = 12345;
        IllegalFormatWidthException illegalFormatWidthException = new IllegalFormatWidthException(
                width);
        assertTrue(null != illegalFormatWidthException.getMessage());
    }
    private static final SerializableAssert exComparator = new SerializableAssert() {
        public void assertDeserialized(Serializable initial,
                Serializable deserialized) {
            SerializationTest.THROWABLE_COMPARATOR.assertDeserialized(initial,
                    deserialized);
            IllegalFormatWidthException initEx = (IllegalFormatWidthException) initial;
            IllegalFormatWidthException desrEx = (IllegalFormatWidthException) deserialized;
            assertEquals("Width", initEx.getWidth(), desrEx.getWidth());
        }
    };
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "!SerializationSelf",
        args = {}
    )
    public void testSerializationSelf() throws Exception {
        SerializationTest.verifySelf(new IllegalFormatWidthException(12345),
                exComparator);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "!SerializationGolden",
        args = {}
    )
    public void testSerializationCompatibility() throws Exception {
        SerializationTest.verifyGolden(this, new IllegalFormatWidthException(
                12345), exComparator);
    }
}
