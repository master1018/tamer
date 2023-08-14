@TestTargetClass(MissingFormatArgumentException.class) 
public class MissingFormatArgumentExceptionTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "MissingFormatArgumentException",
        args = {java.lang.String.class}
    )
    public void test_missingFormatArgumentException() {
        assertNotNull(new MissingFormatArgumentException("String"));
        try {
            new MissingFormatArgumentException(null);
            fail("should throw NullPointerExcepiton.");
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
        MissingFormatArgumentException missingFormatArgumentException = new MissingFormatArgumentException(
                s);
        assertEquals(s, missingFormatArgumentException.getFormatSpecifier());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getMessage",
        args = {}
    )
    public void test_getMessage() {
        String s = "MYTESTSTRING";
        MissingFormatArgumentException missingFormatArgumentException = new MissingFormatArgumentException(
                s);
        assertTrue(null != missingFormatArgumentException.getMessage());
    }
    private static final SerializableAssert exComparator = new SerializableAssert() {
        public void assertDeserialized(Serializable initial,
                Serializable deserialized) {
            SerializationTest.THROWABLE_COMPARATOR.assertDeserialized(initial,
                    deserialized);
            MissingFormatArgumentException initEx = (MissingFormatArgumentException) initial;
            MissingFormatArgumentException desrEx = (MissingFormatArgumentException) deserialized;
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
        SerializationTest.verifySelf(new MissingFormatArgumentException(
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
                new MissingFormatArgumentException("MYTESTSTRING"),
                exComparator);
    }
}
