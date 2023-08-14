public class NullClassLoader {
    public static void main(String[] args) {
        System.err.println(
            "\nTest creating proxy class with the null class loader.\n");
        try {
            Class p = Proxy.getProxyClass(null,
                new Class[] { Runnable.class, Observer.class });
            System.err.println("proxy class: " + p);
            ClassLoader loader = p.getClassLoader();
            System.err.println("proxy class's class loader: " + loader);
            if (loader != null) {
                throw new RuntimeException(
                    "proxy class not defined in the null class loader");
            }
            System.err.println("\nTEST PASSED");
        } catch (Throwable e) {
            System.err.println("\nTEST FAILED:");
            e.printStackTrace();
            throw new RuntimeException("TEST FAILED: " + e.toString());
        }
    }
}
