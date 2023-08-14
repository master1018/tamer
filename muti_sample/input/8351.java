public class TestDoubleType {
    public static void main(String[] args) {
        System.setSecurityManager(new SecurityManager());
        new TestEditor(Double.TYPE);
    }
}
