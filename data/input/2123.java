public class TestFontClass {
    public static void main(String[] args) {
        System.setSecurityManager(new SecurityManager());
        new TestEditor(Font.class);
    }
}
