@TestTargetClass(UnknownFormatFlagsException.class) 
public class UnknownFormatFlagsExceptionTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "UnknownFormatFlagsException",
        args = {java.lang.String.class}
    )
    public void test_unknownFormatFlagsException() {
        try {
            new UnknownFormatFlagsException(null);
            fail("should throw NullPointerExcepiton");
        } catch (NullPointerException e) {
        }
        assertNotNull(new UnknownFormatFlagsException("String"));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getFlags",
        args = {}
    )
    public void test_getFlags() {
        String s = "MYTESTSTRING";
        UnknownFormatFlagsException UnknownFormatFlagsException = new UnknownFormatFlagsException(
                s);
        assertEquals(s, UnknownFormatFlagsException.getFlags());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getMessage",
        args = {}
    )
    public void test_getMessage() {
        String s = "MYTESTSTRING";
        UnknownFormatFlagsException UnknownFormatFlagsException = new UnknownFormatFlagsException(
                s);
        assertNotNull(UnknownFormatFlagsException.getMessage());
    }
    private static final SerializableAssert exComparator = new SerializableAssert() {
        public void assertDeserialized(Serializable initial,
                Serializable deserialized) {
            SerializationTest.THROWABLE_COMPARATOR.assertDeserialized(initial,
                    deserialized);
            UnknownFormatFlagsException initEx = (UnknownFormatFlagsException) initial;
            UnknownFormatFlagsException desrEx = (UnknownFormatFlagsException) deserialized;
            assertEquals("Flags", initEx.getFlags(), desrEx.getFlags());
        }
    };
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "!SerializationSelf",
        args = {}
    )
    public void testSerializationSelf() throws Exception {
        SerializationTest.verifySelf(new UnknownFormatFlagsException(
                "MYTESTSTRING"), exComparator);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "!SerializationGolden",
        args = {}
    )
    public void testSerializationCompatibility() throws Exception {
        SerializationTest.verifyGolden(this, new UnknownFormatFlagsException(
                "MYTESTSTRING"), exComparator);
    }
}
