public class RmiIsNoScheme implements Remote, Serializable {
    private static final int REGISTRY_PORT = 2002;
    private RmiIsNoScheme() {}
    public static void main(String[] args) {
        System.err.println("\nRegression test for bug 4626311\n");
        try {
            LocateRegistry.createRegistry(REGISTRY_PORT);
            Naming.rebind("
                          new RmiIsNoScheme());
            String name = Naming.list("
            System.err.println("name = " + name);
            if (name.startsWith("rmi:", 0) == false) {
                System.err.println("TEST PASSED: rmi scheme not present");
            } else {
                throw new RuntimeException("TEST FAILED: rmi scheme present!");
            }
        } catch (Exception e) {
            TestLibrary.bomb(e);
        }
    }
}
