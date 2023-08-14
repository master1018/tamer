public class TestEnumClass {
    public static void main(String[] args) {
        System.setSecurityManager(new SecurityManager());
        new TestEditor(HexLetter.class);
    }
    public enum HexLetter {A,B,C,D,E,F}
}
