public class MarshalForeignStub
    extends UnicastRemoteObject
    implements Receiver
{
    public static class ForeignStub implements Remote, Serializable {
    }
    public MarshalForeignStub() throws RemoteException {
    }
    public void receive(Object obj) {
        System.err.println("+ receive(): received object " + obj);
    }
    public static void main(String[] args) {
        System.err.println("\nRegression test for bug 4117427\n");
        TestLibrary.suggestSecurityManager(null);
        System.err.println("Creating remote object.");
        MarshalForeignStub obj = null;
        try {
            obj = new MarshalForeignStub();
        } catch (RemoteException e) {
            TestLibrary.bomb(e);
        }
        try {
            Receiver stub = (Receiver) RemoteObject.toStub(obj);
            System.err.println(
                "Passing a foreign stub to remote object.");
            stub.receive(new ForeignStub());
            System.err.println("TEST SUCCEEDED");
        } catch (Exception e) {
            System.err.println("TEST FAILED: ");
            e.printStackTrace();
            throw new RuntimeException("TEST FAILED: " + e.toString());
        } finally {
            try {
                UnicastRemoteObject.unexportObject(obj, true);
            } catch (Throwable e) {
            }
        }
    }
}
