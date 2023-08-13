@TestTargetClass(IllegalFormatConversionException.class) 
public class IllegalFormatConversionExceptionTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "IllegalFormatConversionException",
        args = {char.class, java.lang.Class.class}
    )
    public void test_illegalFormatConversionException() {
        try {
            new IllegalFormatConversionException(' ', null);
            fail("should throw NullPointerExcetpion.");
        } catch (NullPointerException e) {
        }
        assertNotNull(new IllegalFormatConversionException(' ', String.class));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getArgumentClass",
        args = {}
    )
    public void test_getArgumentClass() {
        char c = '*';
        Class<String> argClass = String.class;
        IllegalFormatConversionException illegalFormatConversionException = new IllegalFormatConversionException(
                c, argClass);
        assertEquals(argClass, illegalFormatConversionException
                .getArgumentClass());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getConversion",
        args = {}
    )
    public void test_getConversion() {
        char c = '*';
        Class<String> argClass = String.class;
        IllegalFormatConversionException illegalFormatConversionException = new IllegalFormatConversionException(
                c, argClass);
        assertEquals(c, illegalFormatConversionException.getConversion());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getMessage",
        args = {}
    )
    public void test_getMessage() {
        char c = '*';
        Class<String> argClass = String.class;
        IllegalFormatConversionException illegalFormatConversionException = new IllegalFormatConversionException(
                c, argClass);
        assertTrue(null != illegalFormatConversionException.getMessage());
    }
    private static final SerializableAssert exComparator = new SerializableAssert() {
        public void assertDeserialized(Serializable initial,
                Serializable deserialized) {
            SerializationTest.THROWABLE_COMPARATOR.assertDeserialized(initial,
                    deserialized);
            IllegalFormatConversionException initEx = (IllegalFormatConversionException) initial;
            IllegalFormatConversionException desrEx = (IllegalFormatConversionException) deserialized;
            assertEquals("ArgumentClass", initEx.getArgumentClass(), desrEx
                    .getArgumentClass());
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
        SerializationTest.verifySelf(new IllegalFormatConversionException('*',
                String.class), exComparator);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "!SerializationGolden",
        args = {}
    )
    public void testSerializationCompatibility() throws Exception {
        SerializationTest.verifyGolden(this,
                new IllegalFormatConversionException('*', String.class),
                exComparator);
    }
}
