public class TestDoubleTypeValue {
    public static void main(String[] args) {
        TestEditor test = new TestEditor(Double.TYPE);
        test.testValue(0.0, "0.0");
        test.testValue(null, null);
        test.testText("1.1", 1.1);
        test.testText(null, null);
    }
}
