public class TestMainForDriver {
    public static void main(String[] args) throws Throwable {
        System.setSecurityManager(new SecurityManager());
        try {
            Class.forName("java.sql.DriverManager");
        } catch (ExceptionInInitializerError e) {
            throw e.getException();
        }
    }
}
