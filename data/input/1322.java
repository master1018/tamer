public class CheckUnmarshalOnStopThread
    extends UnicastRemoteObject
    implements CheckUnmarshal
{
    final static int RUNTIME_PILL = 1;
    public static int typeToThrow = 0;
    CheckUnmarshalOnStopThread() throws RemoteException { }
    public PoisonPill getPoisonPill() throws RemoteException {
        return new PoisonPill(new Integer(0));
    }
    public Object ping() throws RemoteException {
        return (Object) new Integer(0);
    }
    public void passRuntimeExceptionParameter(
        RuntimeExceptionParameter rep) throws RemoteException
    {
    }
    public static void main(String [] args) {
        Object dummy = new Object();
        CheckUnmarshal cu = null;
        CheckUnmarshalOnStopThread cuonst = null;
        System.err.println("\nregression test for bugs: " +
                           "4118600 and 4177704\n");
        try {
            cuonst = new CheckUnmarshalOnStopThread();
            cu = (CheckUnmarshal) UnicastRemoteObject.toStub(cuonst);
            System.err.println("testing to see if RMI will handle errors");
            ensureConnectionsAreFreed(cu, true);
            System.err.println("testing to see if RMI will handle " +
                               "runtime exceptions");
            typeToThrow = RUNTIME_PILL;
            ensureConnectionsAreFreed(cu, true);
            System.err.println("testing to see if RMI will handle " +
                               "runtime exceptions thrown during " +
                               "parameter marshalling");
            ensureConnectionsAreFreed(cu, false);
            System.err.println
                ("\nsuccess: CheckUnmarshalOnStopThread test passed ");
        } catch (Exception e) {
            TestLibrary.bomb(e);
        } finally {
            cu = null;
            deactivate(cuonst);
        }
    }
    static void ensureConnectionsAreFreed(CheckUnmarshal cu, boolean getPill)
        throws Exception
    {
        for (int i = 0 ; i < 250 ; i++) {
            try {
                Object test = cu.ping();
                if (getPill) {
                    cu.getPoisonPill();
                } else {
                    cu.passRuntimeExceptionParameter(
                        new RuntimeExceptionParameter());
                }
            } catch (Error e) {
            } catch (RuntimeException e) {
            }
        }
        System.err.println("remote calls passed, received no " +
                           "unmarshal exceptions\n\n");
    }
    static void deactivate(RemoteServer r) {
        try {
            System.err.println("deactivating object.");
            UnicastRemoteObject.unexportObject(r, true);
        } catch (Exception e) {
            e.getMessage();
            e.printStackTrace();
        }
    }
}
