public class RmiRegistrySslTest {
    static final String ok = "OK: Found jmxrmi entry in RMIRegistry!";
    static final String ko = "KO: Did not find jmxrmi entry in RMIRegistry!";
    static final String ko2 = "KO: Did not get expected exception!";
    static final String okException = "OK: Got expected exception!";
    static final String koException = "KO: Got unexpected exception!";
    public static void main(String args[]) throws Exception {
        System.out.println("RmiRegistry lookup...");
        String testID = System.getProperty("testID");
        if ("Test1".equals(testID)) {
            try {
                Registry registry = LocateRegistry.getRegistry(4444);
                String[] list = registry.list();
                if ("jmxrmi".equals(list[0])) {
                    System.out.println(ok);
                } else {
                    System.out.println(ko);
                    throw new IllegalArgumentException(ko);
                }
            } catch (Exception e) {
                System.out.println(koException);
                e.printStackTrace(System.out);
                throw e;
            }
        }
        if ("Test2".equals(testID)) {
            try {
                Registry registry = LocateRegistry.getRegistry(4444);
                String[] list = registry.list();
                throw new IllegalArgumentException(ko2);
            } catch (Exception e) {
                System.out.println(okException);
                e.printStackTrace(System.out);
                return;
            }
        }
        if ("Test3".equals(testID)) {
            try {
                Registry registry = LocateRegistry.getRegistry(
                    null, 4444, new SslRMIClientSocketFactory());
                String[] list = registry.list();
                if ("jmxrmi".equals(list[0])) {
                    System.out.println(ok);
                } else {
                    System.out.println(ko);
                    throw new IllegalArgumentException(ko);
                }
            } catch (Exception e) {
                System.out.println(koException);
                e.printStackTrace(System.out);
                throw e;
            }
        }
    }
}
