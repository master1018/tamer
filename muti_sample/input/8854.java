public class TestColorClassValue {
    public static void main(String[] args) {
        TestEditor test = new TestEditor(Color.class);
        test.testValue(Color.GREEN, "0,255,0");
        test.testValue(null, null);
        test.testText("0,0,0", Color.BLACK);
        test.testText(null, null);
    }
}
