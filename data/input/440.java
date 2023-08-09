public class TestStringClassValue {
    public static void main(String[] args) {
        TestEditor test = new TestEditor(String.class);
        test.testValue("string", "string");
        test.testValue(null, null);
        test.testText("line", "line");
        test.testText(null, null);
    }
}
