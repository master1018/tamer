@TestTargetClass(UnsupportedAddressTypeException.class)
public class UnsupportedAddressTypeExceptionTest extends TestCase {
    public void test_Constructor() {
        UnsupportedAddressTypeException e = new UnsupportedAddressTypeException();
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
            method = "UnsupportedAddressTypeException",
            args = {}
        )
    })    
    public void testSerializationSelf() throws Exception {
        SerializationTest.verifySelf(new UnsupportedAddressTypeException());
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
            method = "UnsupportedAddressTypeException",
            args = {}
        )
    })        
    public void testSerializationCompatibility() throws Exception {
        SerializationTest.verifyGolden(this,
                new UnsupportedAddressTypeException());
    }
}
