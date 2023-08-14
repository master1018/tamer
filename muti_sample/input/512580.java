@TestTargetClass(IllegalFormatFlagsException.class) 
public class IllegalFormatFlagsExceptionTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "IllegalFormatFlagsException",
        args = {java.lang.String.class}
    )
    public void test_illegalFormatFlagsException() {
        try {
            new IllegalFormatFlagsException(null);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
        }
        assertNotNull(new IllegalFormatFlagsException("String"));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getFlags",
        args = {}
    )
    public void test_getFlags() {
        String flags = "TESTFLAGS";
        IllegalFormatFlagsException illegalFormatFlagsException = new IllegalFormatFlagsException(
                flags);
        assertEquals(flags, illegalFormatFlagsException.getFlags());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getMessage",
        args = {}
    )
    public void test_getMessage() {
        String flags = "TESTFLAGS";
        IllegalFormatFlagsException illegalFormatFlagsException = new IllegalFormatFlagsException(
                flags);
        assertTrue(null != illegalFormatFlagsException.getMessage());
    }
    private static final SerializableAssert exComparator = new SerializableAssert() {
        public void assertDeserialized(Serializable initial,
                Serializable deserialized) {
            SerializationTest.THROWABLE_COMPARATOR.assertDeserialized(initial,
                    deserialized);
            IllegalFormatFlagsException initEx = (IllegalFormatFlagsException) initial;
            IllegalFormatFlagsException desrEx = (IllegalFormatFlagsException) deserialized;
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
        SerializationTest.verifySelf(new IllegalFormatFlagsException(
                "TESTFLAGS"), exComparator);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "!SerializationGolden",
        args = {}
    )
    public void testSerializationCompatibility() throws Exception {
        SerializationTest.verifyGolden(this, new IllegalFormatFlagsException(
                "TESTFLAGS"), exComparator);
    }
}
