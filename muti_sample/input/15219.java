public class InvalidProperty {
    public static void main(String[] args) throws Exception {
        ServiceConfiguration.installServiceConfigurationFile();
        System.setProperty(
            "java.rmi.server.RMIClassLoaderSpi", "NonexistentProvider");
        String classname = "Foo";
        TestLibrary.suggestSecurityManager(null);
        try {
            System.err.println("first attempt:");
            Object ret;
            try {
                ret = RMIClassLoader.loadClass(classname);
            } catch (Exception e) {
                throw new RuntimeException(
                    "RMIClassLoader.loadClass threw exception", e);
            }
            throw new RuntimeException(
                "RMIClassLoader.loadClass returned " + ret);
        } catch (Error e) {
            System.err.println("RMIClassLoader.loadClass threw an Error:");
            e.printStackTrace();
        }
        try {
            System.err.println("second attempt:");
            Object ret;
            try {
                ret = RMIClassLoader.loadClass(classname);
            } catch (Exception e) {
                throw new RuntimeException(
                    "RMIClassLoader.loadClass threw exception", e);
            }
            throw new RuntimeException(
                "RMIClassLoader.loadClass returned " + ret);
        } catch (Error e) {
            System.err.println("RMIClassLoader.loadClass threw an Error:");
            e.printStackTrace();
        }
        System.err.println("TEST PASSED");
    }
}
