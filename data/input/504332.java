@TestTargetClass(NonReadableChannelException.class)
public class NonReadableChannelExceptionTest extends TestCase {
    public void test_Constructor() {
        NonReadableChannelException e = new NonReadableChannelException();
        assertNull(e.getMessage());
        assertNull(e.getLocalizedMessage());
        assertNull(e.getCause());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Verifies serialization/deserialization compatibility.",
            method = "!SerializationSelf",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies serialization/deserialization compatibility.",
            method = "NonReadableChannelException",
            args = {}
        )
    })
    public void testSerializationSelf() throws Exception {
        SerializationTest.verifySelf(new NonReadableChannelException());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Verifies serialization/deserialization compatibility.",
            method = "!SerializationGolden",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies serialization/deserialization compatibility.",
            method = "NonReadableChannelException",
            args = {}
        )
    })
    public void testSerializationCompatibility() throws Exception {
        SerializationTest.verifyGolden(this, new NonReadableChannelException());
    }
}
