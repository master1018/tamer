public class BenchServerImpl
    extends UnicastRemoteObject implements BenchServer
{
    HashMap implTable = new HashMap();
    public BenchServerImpl() throws RemoteException {
    }
    public Remote create(BenchServer.RemoteObjectFactory factory)
        throws RemoteException
    {
        Remote impl = factory.create();
        implTable.put(RemoteObject.toStub(impl), new WeakReference(impl));
        return impl;
    }
    public boolean unexport(Remote obj, boolean force) throws RemoteException {
        WeakReference iref = (WeakReference) implTable.get(obj);
        if (iref == null)
            return false;
        Remote impl = (Remote) iref.get();
        if (impl == null)
            return false;
        return UnicastRemoteObject.unexportObject(impl, force);
    }
    public Object execute(BenchServer.Task task) throws Exception {
        return task.execute();
    }
    public void gc() throws RemoteException {
        System.gc();
    }
    public void terminate(int delay) throws RemoteException {
        System.exit(0);
    }
}
