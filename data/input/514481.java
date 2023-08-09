@TestTargetClass(NonWritableChannelException.class)
public class NonWritableChannelExceptionTest extends TestCase {
    public void test_Constructor() {
        NonWritableChannelException e = new NonWritableChannelException();
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
            method = "NonWritableChannelException",
            args = {}
        )
    })
    public void testSerializationSelf() throws Exception {
        SerializationTest.verifySelf(new NonWritableChannelException());
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
            method = "NonWritableChannelException",
            args = {}
        )
    })
    public void testSerializationCompatibility() throws Exception {
        SerializationTest.verifyGolden(this, new NonWritableChannelException());
    }
}
