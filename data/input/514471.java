@TestTargetClass(FormatterClosedException.class) 
public class FormatterClosedExceptionTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "!SerializationSelf",
        args = {}
    )
    public void testSerializationSelf() throws Exception {
        SerializationTest.verifySelf(new FormatterClosedException());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "!SerializationGolden",
        args = {}
    )
    public void testSerializationCompatibility() throws Exception {
        SerializationTest.verifyGolden(this, new FormatterClosedException());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "FormatterClosedException",
        args = {}
    )
    public void test_constructor() {
        assertNotNull(new FormatterClosedException());
    }
}
