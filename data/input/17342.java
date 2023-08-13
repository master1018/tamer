public class ActivatableImpl extends Activatable implements MyRMI {
    private boolean classLoaderOk = false;
    public ActivatableImpl(ActivationID id, MarshalledObject mobj)
        throws RemoteException
    {
        super(id, 0);
        ClassLoader thisLoader = ActivatableImpl.class.getClassLoader();
        ClassLoader ccl = Thread.currentThread().getContextClassLoader();
        System.err.println("implLoader: " + thisLoader);
        System.err.println("ccl: " + ccl);
        classLoaderOk = (thisLoader == ccl);
    }
    public boolean classLoaderOk() throws RemoteException {
        return classLoaderOk;
    }
    public void shutdown() throws Exception {
        ActivationLibrary.deactivate(this, getID());
    }
}
