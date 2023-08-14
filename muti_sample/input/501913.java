@TestTargetClass(UnknownFormatConversionException.class) 
public class UnknownFormatConversionExceptionTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "UnknownFormatConversionException",
        args = {java.lang.String.class}
    )
    public void test_unknownFormatConversionException() {
        try {
            new UnknownFormatConversionException(null);
        } catch (NullPointerException e) {
            fail("should not throw NullPointerExcepiton");
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getConversion",
        args = {}
    )
    public void test_getConversion() {
        String s = "MYTESTSTRING";
        UnknownFormatConversionException UnknownFormatConversionException = new UnknownFormatConversionException(
                s);
        assertEquals(s, UnknownFormatConversionException.getConversion());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getMessage",
        args = {}
    )
    public void test_getMessage() {
        String s = "MYTESTSTRING";
        UnknownFormatConversionException UnknownFormatConversionException = new UnknownFormatConversionException(
                s);
        assertTrue(null != UnknownFormatConversionException.getMessage());
    }
    private static final SerializableAssert exComparator = new SerializableAssert() {
        public void assertDeserialized(Serializable initial,
                Serializable deserialized) {
            SerializationTest.THROWABLE_COMPARATOR.assertDeserialized(initial,
                    deserialized);
            UnknownFormatConversionException initEx = (UnknownFormatConversionException) initial;
            UnknownFormatConversionException desrEx = (UnknownFormatConversionException) deserialized;
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
        SerializationTest.verifySelf(new UnknownFormatConversionException(
                "MYTESTSTRING"), exComparator);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "!SerializationGolden",
        args = {}
    )
    public void testSerializationCompatibility() throws Exception {
        SerializationTest.verifyGolden(this,
                new UnknownFormatConversionException("MYTESTSTRING"),
                exComparator);
    }
}
