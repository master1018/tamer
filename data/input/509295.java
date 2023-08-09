@TestTargetClass(FormatFlagsConversionMismatchException.class)     
public class FormatFlagsConversionMismatchExceptionTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies NullPointerException.",
        method = "FormatFlagsConversionMismatchException",
        args = {java.lang.String.class, char.class}
    )
    public void test_formatFlagsConversionMismatchException() {
        try {
            new FormatFlagsConversionMismatchException(null, ' ');
            fail("should throw NullPointerException.");
        } catch (NullPointerException e) {
        }
        assertNotNull(new FormatFlagsConversionMismatchException("String", ' '));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getFlags",
        args = {}
    )
    public void test_getFlags() {
        String flags = "MYTESTFLAGS";
        char conversion = 'T';
        FormatFlagsConversionMismatchException formatFlagsConversionMismatchException = new FormatFlagsConversionMismatchException(
                flags, conversion);
        assertEquals(flags, formatFlagsConversionMismatchException.getFlags());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getConversion",
        args = {}
    )
    public void test_getConversion() {
        String flags = "MYTESTFLAGS";
        char conversion = 'T';
        FormatFlagsConversionMismatchException 
                formatFlagsConversionMismatchException = 
                                    new FormatFlagsConversionMismatchException(
                flags, conversion);
        assertEquals(conversion, formatFlagsConversionMismatchException
                .getConversion());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getMessage",
        args = {}
    )
    public void test_getMessage() {
        String flags = "MYTESTFLAGS";
        char conversion = 'T';
        FormatFlagsConversionMismatchException formatFlagsConversionMismatchException = new FormatFlagsConversionMismatchException(
                flags, conversion);
        assertTrue(null != formatFlagsConversionMismatchException.getMessage());
    }
    private static final SerializableAssert exComparator = new SerializableAssert() {
        public void assertDeserialized(Serializable initial,
                Serializable deserialized) {
            SerializationTest.THROWABLE_COMPARATOR.assertDeserialized(initial,
                    deserialized);
            FormatFlagsConversionMismatchException initEx = (FormatFlagsConversionMismatchException) initial;
            FormatFlagsConversionMismatchException desrEx = (FormatFlagsConversionMismatchException) deserialized;
            assertEquals("Flags", initEx.getFlags(), desrEx.getFlags());
            assertEquals("Conversion", initEx.getConversion(), desrEx
                    .getConversion());
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
                new FormatFlagsConversionMismatchException("MYTESTFLAGS", 'T'),
                exComparator);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "!SerializationGolden",
        args = {}
    )
    public void testSerializationCompatibility() throws Exception {
        SerializationTest.verifyGolden(this,
                new FormatFlagsConversionMismatchException("MYTESTFLAGS", 'T'),
                exComparator);
    }
}
