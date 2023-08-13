public class TestBooleanClassValue {
    public static void main(String[] args) {
        TestEditor test = new TestEditor(Boolean.class);
        test.testValue(true, "True");
        test.testValue(null, null);
        test.testText("False", false);
        test.testText(null, null);
    }
}
