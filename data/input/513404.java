@TestTargetClass(IllegalBlockingModeException.class)
public class IllegalBlockingModeExceptionTest extends TestCase {
    public void test_Constructor() {
        IllegalBlockingModeException e = new IllegalBlockingModeException();
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
            method = "IllegalBlockingModeException",
            args = {}
        )
    })
    public void testSerializationSelf() throws Exception {
        SerializationTest.verifySelf(new IllegalBlockingModeException());
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
            method = "IllegalBlockingModeException",
            args = {}
        )
    })
    public void testSerializationCompatibility() throws Exception {
        SerializationTest
                .verifyGolden(this, new IllegalBlockingModeException());
    }
}
