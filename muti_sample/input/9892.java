public class TestEnumClassNull {
    public static void main(String[] args) {
        new TestEditor(HexLetter.class).testJava(null);
    }
    public enum HexLetter {A,B,C,D,E,F}
}
