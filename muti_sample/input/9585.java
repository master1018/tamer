public class HttpSocketTest extends UnicastRemoteObject
    implements MyRemoteInterface
{
    private static final String NAME = "HttpSocketTest";
    private static final String REGNAME =
        "
    public HttpSocketTest() throws RemoteException{}
    private Remote ro;
    public static void main(String[] args)
        throws Exception
    {
        Registry registry = null;
        TestLibrary.suggestSecurityManager(null);
        System.err.println("installing socket factory");
        RMISocketFactory.setSocketFactory(new RMIHttpToPortSocketFactory());
        try {
            System.err.println("Starting registry");
            registry = LocateRegistry.createRegistry(TestLibrary.REGISTRY_PORT);
        } catch (Exception e) {
            TestLibrary.bomb(e);
        }
        try {
            registry.rebind( NAME, new HttpSocketTest() );
            MyRemoteInterface httpTest =
                (MyRemoteInterface)Naming.lookup( REGNAME );
            httpTest.setRemoteObject( new HttpSocketTest() );
            Remote r = httpTest.getRemoteObject();
        } catch (Exception e) {
            TestLibrary.bomb(e);
        }
    }
    public void setRemoteObject( Remote ro ) throws RemoteException {
        this.ro = ro;
    }
    public Remote getRemoteObject() throws RemoteException {
        return( this.ro );
    }
}
