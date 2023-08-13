@TestTargetClass(ClosedByInterruptException.class)
public class ClosedByInterruptExceptionTest extends TestCase {
    public void test_Constructor() {
        ClosedByInterruptException e = new ClosedByInterruptException();
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
            method = "ClosedByInterruptException",
            args = {}
        )
    })    
    public void testSerializationSelf() throws Exception {
        SerializationTest.verifySelf(new ClosedByInterruptException());
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
            method = "ClosedByInterruptException",
            args = {}
        )
    })    
    public void testSerializationCompatibility() throws Exception {
        SerializationTest.verifyGolden(this, new ClosedByInterruptException());
    }
}
