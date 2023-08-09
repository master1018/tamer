@TestTargetClass(InvalidPropertiesFormatException.class) 
public class InvalidPropertiesFormatExceptionTest extends
        junit.framework.TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies serialization/deserialization compatibility.",
        method = "!SerializationSelf",
        args = {}
    )
    public void test_Serialization() throws Exception {
        InvalidPropertiesFormatException ipfe = new InvalidPropertiesFormatException(
                "Hey, this is InvalidPropertiesFormatException");
        try {
            SerializationTest.verifySelf(ipfe);
        } catch (NotSerializableException e) {
        }
    }
}
