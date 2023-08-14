public class RegistryRunner extends UnicastRemoteObject
    implements RemoteExiter
{
    private static Registry registry = null;
    private static RemoteExiter exiter = null;
    public RegistryRunner() throws RemoteException {
    }
    public void exit() throws RemoteException {
        System.err.println("received call to exit");
        System.exit(0);
    }
    public static void requestExit() {
        try {
            RemoteExiter exiter =
                (RemoteExiter)
                Naming.lookup("rmi:
                              TestLibrary.REGISTRY_PORT +
                              "/RemoteExiter");
            try {
                exiter.exit();
            } catch (RemoteException re) {
            }
            exiter = null;
        } catch (java.net.MalformedURLException mfue) {
        } catch (NotBoundException nbe) {
            TestLibrary.bomb("exiter not bound?", nbe);
        } catch (RemoteException re) {
            TestLibrary.bomb("remote exception trying to exit",
                             re);
        }
    }
    public static void main(String[] args) {
        try {
            if (args.length == 0) {
                System.err.println("Usage: <port>");
                System.exit(0);
            }
            int port = TestLibrary.REGISTRY_PORT;
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException nfe) {
            }
            registry = LocateRegistry.createRegistry(port);
            exiter = new RegistryRunner();
            Naming.rebind("rmi:
                          "/RemoteExiter", exiter);
        } catch (Exception e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
            System.exit(-1);
        }
    }
}
