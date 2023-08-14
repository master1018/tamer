public class ContextWithNullProperties {
    public static void main(String[] args) throws Exception {
        try {
            LocateRegistry.createRegistry(1099);
        } catch (RemoteException e) {
        }
        System.out.println("Connecting to the default Registry...");
        RegistryContext ctx = new RegistryContext(null, -1, null);
    }
}
