@TestTargetClass(OverlappingFileLockException.class)
public class OverlappingFileLockExceptionTest extends TestCase {
    public void test_Constructor() {
        OverlappingFileLockException e = new OverlappingFileLockException();
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
            method = "OverlappingFileLockException",
            args = {}
        )
    })
    public void testSerializationSelf() throws Exception {
        SerializationTest.verifySelf(new OverlappingFileLockException());
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
            method = "OverlappingFileLockException",
            args = {}
        )
    })
    public void testSerializationCompatibility() throws Exception {
        SerializationTest
                .verifyGolden(this, new OverlappingFileLockException());
    }
}
