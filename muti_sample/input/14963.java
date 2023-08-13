public class TestBooleanTypeValue {
    public static void main(String[] args) {
        TestEditor test = new TestEditor(Boolean.TYPE);
        test.testValue(true, "True");
        test.testValue(null, null);
        test.testText("False", false);
        test.testText(null, null);
    }
}
