@TestTargetClass(NotYetBoundException.class)
public class NotYetBoundExceptionTest extends TestCase {
    public void test_Constructor() {
        NotYetBoundException e = new NotYetBoundException();
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
            method = "NotYetBoundException",
            args = {}
        )
    })
    public void testSerializationSelf() throws Exception {
        SerializationTest.verifySelf(new NotYetBoundException());
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
            method = "NotYetBoundException",
            args = {}
        )
    })
    public void testSerializationCompatibility() throws Exception {
        SerializationTest.verifyGolden(this, new NotYetBoundException());
    }
}
