@TestTargetClass(NotYetConnectedException.class)
public class NotYetConnectedExceptionTest extends TestCase {
    public void test_Constructor() {
        NotYetConnectedException e = new NotYetConnectedException();
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
            method = "NotYetConnectedException",
            args = {}
        )
    })
    public void testSerializationSelf() throws Exception {
        SerializationTest.verifySelf(new NotYetConnectedException());
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
            method = "NotYetConnectedException",
            args = {}
        )
    })
    public void testSerializationCompatibility() throws Exception {
        SerializationTest.verifyGolden(this, new NotYetConnectedException());
    }
}
