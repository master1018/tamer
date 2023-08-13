@TestTargetClass(MissingFormatWidthException.class) 
public class MissingFormatWidthExceptionTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "MissingFormatWidthException",
        args = {java.lang.String.class}
    )
    public void test_missingFormatWidthException() {
        assertNotNull(new MissingFormatWidthException("String"));
        try {
            new MissingFormatWidthException(null);
            fail("should throw NullPointerExcepiton");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getFormatSpecifier",
        args = {}
    )
    public void test_getFormatSpecifier() {
        String s = "MYTESTSTRING";
        MissingFormatWidthException missingFormatWidthException = new MissingFormatWidthException(
                s);
        assertEquals(s, missingFormatWidthException.getFormatSpecifier());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getMessage",
        args = {}
    )
    public void test_getMessage() {
        String s = "MYTESTSTRING";
        MissingFormatWidthException missingFormatWidthException = new MissingFormatWidthException(
                s);
        assertTrue(null != missingFormatWidthException.getMessage());
    }
    private static final SerializableAssert exComparator = new SerializableAssert() {
        public void assertDeserialized(Serializable initial,
                Serializable deserialized) {
            SerializationTest.THROWABLE_COMPARATOR.assertDeserialized(initial,
                    deserialized);
            MissingFormatWidthException initEx = (MissingFormatWidthException) initial;
            MissingFormatWidthException desrEx = (MissingFormatWidthException) deserialized;
            assertEquals("FormatSpecifier", initEx.getFormatSpecifier(), desrEx
                    .getFormatSpecifier());
        }
    };
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "!SerializationSelf",
        args = {}
    )
    public void testSerializationSelf() throws Exception {
        SerializationTest.verifySelf(new MissingFormatWidthException(
                "MYTESTSTRING"), exComparator);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "!SerializationGolden",
        args = {}
    )
    public void testSerializationCompatibility() throws Exception {
        SerializationTest.verifyGolden(this, new MissingFormatWidthException(
                "MYTESTSTRING"), exComparator);
    }
}
