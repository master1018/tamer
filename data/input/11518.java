public class DefaultRegistryPort {
    public static void main(String args[]) {
        Registry registry = null;
        try {
            System.err.println(
                "Starting registry on default port REGISTRY_PORT=" +
                Registry.REGISTRY_PORT);
            registry = LocateRegistry.createRegistry(Registry.REGISTRY_PORT);
            System.err.println("Created registry=" + registry);
        } catch(java.rmi.RemoteException e) {
            try {
                System.err.println(
                    "Failed to create a registry, try using existing one");
                registry = LocateRegistry.getRegistry();
                System.err.println("Found registry=" + registry);
            } catch (Exception ge) {
                TestLibrary.bomb(
                    "Test Failed: cound not find or create a registry");
            }
        }
        try {
            if (registry != null) {
                registry.rebind("myself", registry);
                Remote myself = Naming.lookup("rmi:
                System.err.println("Test PASSED");
            } else {
                TestLibrary.bomb(
                    "Test Failed: cound not find or create a registry");
            }
        } catch(java.rmi.NotBoundException e) {
            TestLibrary.bomb(
                "Test Failed: could not find myself");
        } catch(Exception e) {
            e.printStackTrace();
            TestLibrary.bomb(
                "Test failed: unexpected exception");
        }
    }
}
