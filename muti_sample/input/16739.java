public class TestByteClassValue {
    public static void main(String[] args) {
        TestEditor test = new TestEditor(Byte.class);
        test.testValue((byte) 0, "0");
        test.testValue(null, null);
        test.testText("1", (byte) 1);
        test.testText(null, null);
    }
}
