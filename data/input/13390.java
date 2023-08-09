public class TestLongTypeValue {
    public static void main(String[] args) {
        TestEditor test = new TestEditor(Long.TYPE);
        test.testValue(0l, "0");
        test.testValue(null, null);
        test.testText("1", 1l);
        test.testText(null, null);
    }
}
