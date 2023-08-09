public class LeaseLeakClient {
    public static void main(String args[]) {
        TestLibrary.suggestSecurityManager("java.rmi.RMISecurityManager");
        try {
            LeaseLeak leaseLeak = null;
            Registry registry =
                java.rmi.registry.LocateRegistry.getRegistry(
                    TestLibrary.REGISTRY_PORT);
            leaseLeak = (LeaseLeak) registry.lookup("/LeaseLeak");
            leaseLeak.ping();
        } catch(Exception e) {
            System.err.println("LeaseLeakClient Error: "+e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }
}
