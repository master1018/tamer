public class ComputeServerImpl
    extends UnicastRemoteObject
    implements ComputeServer
{
        public ComputeServerImpl() throws RemoteException
        {
    }
    public Object compute(Task task) {
        return task.run();
    }
    public static void main(String args[]) throws Exception
        {
                System.setSecurityManager(new RMISecurityManager());
                Naming.rebind("/ComputeServer", new ComputeServerImpl());
                System.out.println("Ready to receive tasks.");
                System.err.println("DTI_DoneInitializing");
    }
}
