@TestTargetClass(IllegalFormatPrecisionException.class) 
public class IllegalFormatPrecisionExceptionTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "IllegalFormatPrecisionException",
        args = {int.class}
    )
    public void test_illegalFormatPrecisionException() {
        IllegalFormatPrecisionException illegalFormatPrecisionException = new IllegalFormatPrecisionException(
                Integer.MIN_VALUE);
        assertEquals(Integer.MIN_VALUE, illegalFormatPrecisionException
                .getPrecision());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getPrecision",
        args = {}
    )
    public void test_getPrecision() {
        int precision = 12345;
        IllegalFormatPrecisionException illegalFormatPrecisionException = new IllegalFormatPrecisionException(
                precision);
        assertEquals(precision, illegalFormatPrecisionException.getPrecision());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getMessage",
        args = {}
    )
    public void test_getMessage() {
        int precision = 12345;
        IllegalFormatPrecisionException illegalFormatPrecisionException = new IllegalFormatPrecisionException(
                precision);
        assertTrue(null != illegalFormatPrecisionException.getMessage());
    }
    private static final SerializableAssert exComparator = new SerializableAssert() {
        public void assertDeserialized(Serializable initial,
                Serializable deserialized) {
            SerializationTest.THROWABLE_COMPARATOR.assertDeserialized(initial,
                    deserialized);
            IllegalFormatPrecisionException initEx = (IllegalFormatPrecisionException) initial;
            IllegalFormatPrecisionException desrEx = (IllegalFormatPrecisionException) deserialized;
            assertEquals("Precision", initEx.getPrecision(), desrEx
                    .getPrecision());
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
                new IllegalFormatPrecisionException(12345), exComparator);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "!SerializationGolden",
        args = {}
    )
    public void testSerializationCompatibility() throws Exception {
        SerializationTest.verifyGolden(this,
                new IllegalFormatPrecisionException(12345), exComparator);
    }
}
