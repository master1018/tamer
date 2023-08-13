public class UninitializedParent {
    private static ClassLoader loader;
    public static void main(String[] args) throws Exception {
        System.setSecurityManager(new SecurityManager());
        try {
            new ClassLoader(null) {
                @Override
                protected void finalize() {
                    loader = this;
                }
            };
        } catch (SecurityException exc) {
        }
        System.gc();
        System.runFinalization();
        if (loader != null) {
            try {
                URLClassLoader child = URLClassLoader.newInstance
                    (new URL[0], loader);
                throw new RuntimeException("Test Failed!");
            } catch (SecurityException se) {
                System.out.println("Test Passed: Exception thrown");
            }
        } else {
            System.out.println("Test Passed: Loader is null");
        }
    }
}
