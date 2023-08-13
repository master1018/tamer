public class ChangeHostName
    extends UnicastRemoteObject
    implements Receiver
{
    public ChangeHostName() throws RemoteException {
    }
    public void receive(Remote obj) {
        System.err.println("received: " + obj.toString());
    }
    public static void main(String[] args) throws Exception {
        InetAddress localAddress = InetAddress.getLocalHost();
        String[] hostlist = new String[] {
            localAddress.getHostAddress(), localAddress.getHostName() };
        for (int i = 0; i < hostlist.length; i++) {
            System.setProperty("java.rmi.server.hostname", hostlist[i]);
            Remote impl = new ChangeHostName();
            System.err.println("\ncreated impl extending URO: " + impl);
            Receiver stub = (Receiver) RemoteObject.toStub(impl);
            System.err.println("stub for impl: " + stub);
            System.err.println("invoking method on stub");
            stub.receive(stub);
            UnicastRemoteObject.unexportObject(impl, true);
            System.err.println("unexported impl");
            if (stub.toString().indexOf(hostlist[i]) >= 0) {
                System.err.println("stub's ref contains hostname: " +
                                   hostlist[i]);
            } else {
                throw new RuntimeException(
                    "TEST FAILED: stub's ref doesn't contain hostname: " +
                    hostlist[i]);
            }
        }
        System.err.println("TEST PASSED");
    }
}
interface Receiver extends Remote {
    void receive(Remote obj) throws RemoteException;
}
