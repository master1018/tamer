public class TestShortTypeValue {
    public static void main(String[] args) {
        TestEditor test = new TestEditor(Short.TYPE);
        test.testValue((short) 0, "0");
        test.testValue(null, null);
        test.testText("1", (short) 1);
        test.testText(null, null);
    }
}
