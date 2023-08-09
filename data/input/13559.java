public class TestBooleanClass {
    public static void main(String[] args) {
        System.setSecurityManager(new SecurityManager());
        new TestEditor(Boolean.class);
    }
}
