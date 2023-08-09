public class IgnoreNullSecurityManager {
    public static void main(String argv[]) throws Exception {
        System.setSecurityManager(null);
    }
}
