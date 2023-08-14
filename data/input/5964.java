public class MyObjectImpl extends UnicastRemoteObject implements MyObject {
    private int clientNum = -1;
    private byte[] data = null;
    private boolean AliveMyObjectsCounterWasIncremented = false;
    public MyObjectImpl() throws RemoteException {
        super();
    }
    public MyObjectImpl(int c, int size) 
                        throws RemoteException {
        super();
        this.clientNum = c;
        this.data = new byte[size];
        AliveMyObjectsCounterWasIncremented = true;
    }
    public void method1(MyObject obj) throws RemoteException {
    }
    public void method2(MyObject[] objs) throws RemoteException {
    }
    public void method3() throws RemoteException {
    }
    protected void finalize() throws Throwable {
        if(AliveMyObjectsCounterWasIncremented)
            ; 
        super.finalize();
    }
}
