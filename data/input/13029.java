public class TestBooleanType {
    public static void main(String[] args) {
        System.setSecurityManager(new SecurityManager());
        new TestEditor(Boolean.TYPE);
    }
}
