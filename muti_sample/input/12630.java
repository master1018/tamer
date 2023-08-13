public class TestIntegerType {
    public static void main(String[] args) {
        System.setSecurityManager(new SecurityManager());
        new TestEditor(Integer.TYPE);
    }
}
