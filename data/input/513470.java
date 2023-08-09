@TestTargetClass(ConnectionPendingException.class)
public class ConnectionPendingExceptionTest extends TestCase {
    public void test_Constructor() {
        ConnectionPendingException e = new ConnectionPendingException();
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
            method = "ConnectionPendingException",
            args = {}
        )
    })    
    public void testSerializationSelf() throws Exception {
        SerializationTest.verifySelf(new ConnectionPendingException());
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
            method = "ConnectionPendingException",
            args = {}
        )
    })    
    public void testSerializationCompatibility() throws Exception {
        SerializationTest.verifyGolden(this, new ConnectionPendingException());
    }
}
