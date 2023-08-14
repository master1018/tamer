public class TestFloatTypeValue {
    public static void main(String[] args) {
        TestEditor test = new TestEditor(Float.TYPE);
        test.testValue(0.0f, "0.0");
        test.testValue(null, null);
        test.testText("1.1", 1.1f);
        test.testText(null, null);
    }
}
