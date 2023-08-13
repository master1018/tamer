public class EchoImpl
    extends Activatable
    implements Echo, Runnable
{
    private static final byte[] pattern = { (byte) 'A' };
    public EchoImpl(String protocol)
        throws ActivationException, RemoteException
    {
        super(null, makeMarshalledObject(protocol), false, 0,
              new MultiSocketFactory.ClientFactory(protocol, pattern),
              new MultiSocketFactory.ServerFactory(protocol, pattern));
    }
    public EchoImpl(ActivationID id, MarshalledObject obj)
        throws RemoteException
    {
        super(id, 0,
              new MultiSocketFactory.ClientFactory(getProtocol(obj), pattern),
              new MultiSocketFactory.ServerFactory(getProtocol(obj), pattern));
    }
    private static MarshalledObject makeMarshalledObject(String protocol) {
        MarshalledObject obj = null;
        try {
            obj = new MarshalledObject(protocol);
        } catch (Exception willNotHappen) {
        }
        return obj;
    }
    private static String getProtocol(MarshalledObject obj) {
        String protocol = "";
        try {
            protocol = (String) obj.get();
        } catch (Exception willNotHappen) {
        }
        return protocol;
    }
    public byte[] echoNot(byte[] data) {
        byte[] result = new byte[data.length];
        for (int i = 0; i < data.length; i++)
            result[i] = (byte) ~data[i];
        return result;
    }
    public void shutdown() throws Exception
    {
        (new Thread(this,"Echo.shutdown")).start();
    }
    public void run()
    {
        ActivationLibrary.deactivate(this, getID());
    }
    public static void main(String[] args) {
        Object dummy = new Object();
        System.setSecurityManager(new RMISecurityManager());
        try {
            String protocol = "";
            if (args.length >= 1)
                protocol = args[0];
            System.out.println("EchoServer: creating remote object");
            ActivationGroupDesc groupDesc =
                new ActivationGroupDesc(null, null);
            ActivationSystem system = ActivationGroup.getSystem();
            ActivationGroupID groupID = system.registerGroup(groupDesc);
            ActivationGroup.createGroup(groupID, groupDesc, 0);
            EchoImpl impl = new EchoImpl(protocol);
            System.out.println("EchoServer: binding in registry");
            Naming.rebind("
                          "/EchoServer", impl);
            System.out.println("EchoServer ready.");
        } catch (Exception e) {
            System.err.println("EXCEPTION OCCURRED:");
            e.printStackTrace();
        }
    }
}
