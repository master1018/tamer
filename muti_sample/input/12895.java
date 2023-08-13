class MyRemoteObject extends UnicastRemoteObject
    implements MyRemoteInterface {
    public MyRemoteObject () throws RemoteException {}
    public void ping () throws RemoteException {
        throw new RemoteException("This is a test remote exception");
    }
}
public class ClientStackTrace {
    Object dummy = new Object();
    public static void main(String[] args) {
        TestLibrary.suggestSecurityManager("java.rmi.RMISecurityManager");
        Object dummy = new Object();
        MyRemoteObject myRobj = null;
        MyRemoteInterface myStub = null;
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            PrintStream ps = new PrintStream(bos);
            System.err.println("\nRegression test for bug 4010355\n");
            myRobj = new MyRemoteObject();
            try {
                myStub = (MyRemoteInterface) RemoteObject.toStub(myRobj);
                myStub.ping();
            } catch (RemoteException re) {
                re.printStackTrace(ps);
                String trace = bos.toString();
                if (trace.indexOf("exceptionReceivedFromServer") <0 ) {
                    throw new RuntimeException("No client stack trace on " +
                                               "thrown remote exception");
                } else {
                    System.err.println("test passed with stack trace: " +
                                       trace);
                }
            }
            deactivate(myRobj);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("test failed");
            throw new RuntimeException(e.getMessage());
        } finally {
            myRobj = null;
            myStub = null;
        }
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
