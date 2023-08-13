public class CheckPackageAccess {
    private final static String restrictedClassName = "sun.misc.Ref";
    public static void main(String[] args) {
        System.err.println("\nRegression test for bug 4180392\n");
        System.err.println("Setting RMISecurityManager.");
        System.setSecurityManager(new RMISecurityManager());
        try {
            System.err.println("Attempting to acquire restricted class " +
                restrictedClassName);
            Class restrictedClass = Class.forName(restrictedClassName);
            throw new RuntimeException(
                "TEST FAILED: successfully acquired restricted class " +
                    restrictedClass);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(
                "TEST FAILED: couldn't find (but was allowed to look for) " +
                    "restricted class " + restrictedClassName);
        } catch (SecurityException e) {
            System.err.println("TEST PASSED: ");
            e.printStackTrace();
        }
    }
}
