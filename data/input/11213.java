public class TestFloatClass {
    public static void main(String[] args) {
        System.setSecurityManager(new SecurityManager());
        new TestEditor(Float.class);
    }
}
