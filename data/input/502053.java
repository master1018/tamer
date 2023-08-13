@TestTargetClass(Format.Field.class) 
public class FormatFieldTest extends TestCase {
    private class MockFormatField extends Format.Field {
        private static final long serialVersionUID = 1L;
        public MockFormatField(String name) {
            super(name);
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "Field",
        args = {java.lang.String.class}
    )
    public void test_Constructor() {
        try {
            new MockFormatField("test");
        } catch (Exception e) {
            fail("Unexpected exception " + e.toString());
        }
    }
}
