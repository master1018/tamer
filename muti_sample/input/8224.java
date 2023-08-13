public class TestStringClass {
    public static void main(String[] args) {
        System.setSecurityManager(new SecurityManager());
        new TestEditor(String.class);
    }
}
