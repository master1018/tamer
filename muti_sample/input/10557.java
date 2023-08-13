public class TestShortClassValue {
    public static void main(String[] args) {
        TestEditor test = new TestEditor(Short.class);
        test.testValue((short) 0, "0");
        test.testValue(null, null);
        test.testText("1", (short) 1);
        test.testText(null, null);
    }
}
