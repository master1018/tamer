public class MultipleRegistries implements RemoteInterface {
    private static final String NAME = "MultipleRegistries";
    public Object passObject(Object obj) {
        return obj;
    }
    public static void main(String[] args) throws Exception {
        RemoteInterface server = null;
        RemoteInterface proxy = null;
        try {
            System.err.println("export object");
            server = new MultipleRegistries();
            proxy =
                (RemoteInterface) UnicastRemoteObject.exportObject(server, 0);
            System.err.println("proxy = " + proxy);
            System.err.println("export registries");
            Registry registryImpl1 = LocateRegistry.createRegistry(2030);
            Registry registryImpl2 = LocateRegistry.createRegistry(2040);
            System.err.println("bind remote object in registries");
            Registry registry1 = LocateRegistry.getRegistry(2030);
            Registry registry2 = LocateRegistry.getRegistry(2040);
            registry1.bind(NAME, proxy);
            registry2.bind(NAME, proxy);
            System.err.println("lookup remote object in registries");
            RemoteInterface remote1 = (RemoteInterface) registry1.lookup(NAME);
            RemoteInterface remote2 = (RemoteInterface) registry2.lookup(NAME);
            System.err.println("invoke methods on remote objects");
            remote1.passObject(remote1);
            remote2.passObject(remote2);
            System.err.println("TEST PASSED");
        } finally {
            if (proxy != null) {
                UnicastRemoteObject.unexportObject(server, true);
            }
        }
    }
}
interface RemoteInterface extends Remote {
    Object passObject(Object obj) throws RemoteException;
}
