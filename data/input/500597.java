@TestTargetClass(ClosedChannelException.class)
public class ClosedChannelExceptionTest extends TestCase {
    public void test_Constructor() {
        ClosedChannelException e = new ClosedChannelException();
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
            method = "ClosedChannelException",
            args = {}
        )
    })     
    public void testSerializationSelf() throws Exception {
        SerializationTest.verifySelf(new ClosedChannelException());
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
            method = "ClosedChannelException",
            args = {}
        )
    })     
    public void testSerializationCompatibility() throws Exception {
        SerializationTest.verifyGolden(this, new ClosedChannelException());
    }
}
