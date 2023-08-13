@TestTargetClass(NoConnectionPendingException.class)
public class NoConnectionPendingExceptionTest extends TestCase {
    public void test_Constructor() {
        NoConnectionPendingException e = new NoConnectionPendingException();
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
            method = "NoConnectionPendingException",
            args = {}
        )
    })
    public void testSerializationSelf() throws Exception {
        SerializationTest.verifySelf(new NoConnectionPendingException());
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
            method = "NoConnectionPendingException",
            args = {}
        )
    })
    public void testSerializationCompatibility() throws Exception {
        SerializationTest
                .verifyGolden(this, new NoConnectionPendingException());
    }
}
