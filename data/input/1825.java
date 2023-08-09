public class TestEnumClassValue {
    public static void main(String[] args) {
        TestEditor test = new TestEditor(HexLetter.class);
        test.testValue(HexLetter.A, "A");
        test.testValue(null, null);
        test.testText("F", HexLetter.F);
        test.testText(null, null);
    }
    public enum HexLetter {A,B,C,D,E,F}
}
