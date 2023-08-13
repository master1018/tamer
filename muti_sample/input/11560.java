public class TestLongClassValue {
    public static void main(String[] args) {
        TestEditor test = new TestEditor(Long.class);
        test.testValue(0l, "0");
        test.testValue(null, null);
        test.testText("1", 1l);
        test.testText(null, null);
    }
}
