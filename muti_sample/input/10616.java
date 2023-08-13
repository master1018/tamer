public class MarshalAfterUnexport
    extends UnicastRemoteObject
    implements Receiver
{
    public MarshalAfterUnexport() throws RemoteException {
    }
    public void receive(Remote obj) {
    }
    public static void main(String[] args) throws Exception {
        Remote impl2 = null;
        try {
            Remote impl = new MarshalAfterUnexport();
            System.err.println("created impl extending URO: " + impl);
            Receiver stub = (Receiver) RemoteObject.toStub(impl);
            System.err.println("stub for impl: " + stub);
            UnicastRemoteObject.unexportObject(impl, true);
            System.err.println("unexported impl");
            impl2 = new MarshalAfterUnexport();
            Receiver stub2 = (Receiver) RemoteObject.toStub(impl2);
            System.err.println("marshalling unexported object:");
            MarshalledObject mobj = new MarshalledObject(impl);
            System.err.println("passing unexported object via RMI-JRMP:");
            stub2.receive(stub);
            System.err.println("TEST PASSED");
        } finally {
            if (impl2 != null) {
                try {
                    UnicastRemoteObject.unexportObject(impl2, true);
                } catch (Throwable t) {
                }
            }
        }
    }
}
interface Receiver extends Remote {
    void receive(Remote obj) throws RemoteException;
}
