@TestTargetClass(DuplicateFormatFlagsException.class) 
public class DuplicateFormatFlagsExceptionTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "DuplicateFormatFlagsException",
        args = {java.lang.String.class}
    )
    public void test_duplicateFormatFlagsException() {
        try {
            new DuplicateFormatFlagsException(null);
            fail("should throw NullPointerException.");
        } catch (NullPointerException e) {
        }
        assertNotNull(new DuplicateFormatFlagsException("String"));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getFlags",
        args = {}
    )
    public void test_getFlags() {
        String strFlags = "MYTESTFLAGS";
        DuplicateFormatFlagsException duplicateFormatException = new DuplicateFormatFlagsException(
                strFlags);
        assertEquals(strFlags, duplicateFormatException.getFlags());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getMessage",
        args = {}
    )
    public void test_getMessage() {
        String strFlags = "MYTESTFLAGS";
        DuplicateFormatFlagsException duplicateFormatException = new DuplicateFormatFlagsException(
                strFlags);
        assertTrue(null != duplicateFormatException.getFlags());
    }
    private static final SerializableAssert exComparator = new SerializableAssert() {
        public void assertDeserialized(Serializable initial,
                Serializable deserialized) {
            SerializationTest.THROWABLE_COMPARATOR.assertDeserialized(initial,
                    deserialized);
            DuplicateFormatFlagsException initEx = (DuplicateFormatFlagsException) initial;
            DuplicateFormatFlagsException desrEx = (DuplicateFormatFlagsException) deserialized;
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
        SerializationTest.verifySelf(new DuplicateFormatFlagsException(
                "TESTDESC"), exComparator);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "!SerializationGolden",
        args = {}
    )
    public void testSerializationCompatibility() throws Exception {
        SerializationTest.verifyGolden(this, new DuplicateFormatFlagsException(
                "TESTDESC"), exComparator);
    }
}
