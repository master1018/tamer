public class DownloadArrayClass
    extends UnicastRemoteObject
    implements Receiver
{
    public DownloadArrayClass() throws RemoteException {
    }
    public void receive(Object obj) {
        System.err.println("+ receive(): received object " + obj);
    }
    public static void main(String[] args) {
        System.err.println("\nRegression test for bug 4082868\n");
        URL remoteCodebase = null;
        try {
            remoteCodebase =
                TestLibrary.installClassInCodebase("Foo", "remote_codebase");
        } catch (MalformedURLException e) {
            TestLibrary.bomb(e);
        }
        System.err.println("Creating class loader for remote codebase " +
            remoteCodebase);
        ClassLoader remoteCodebaseLoader =
            URLClassLoader.newInstance(new URL[] { remoteCodebase });
        TestLibrary.suggestSecurityManager(null);
        System.err.println("Creating remote object.");
        DownloadArrayClass obj = null;
        try {
            obj = new DownloadArrayClass();
        } catch (RemoteException e) {
            TestLibrary.bomb(e);
        }
        try {
            Receiver stub = (Receiver) RemoteObject.toStub(obj);
            Class fooClass = remoteCodebaseLoader.loadClass("Foo");
            Object arg;
            arg = fooClass.newInstance();
            System.err.println("Passing object of type " + arg.getClass());
            stub.receive(arg);
            arg = Array.newInstance(fooClass, 26);
            System.err.println("Passing object of type " + arg.getClass());
            stub.receive(arg);
            arg = Array.newInstance(fooClass, new int[] { 1, 42, 0 });
            System.err.println("Passing object of type " + arg.getClass());
            stub.receive(arg);
            System.err.println("TEST PASSED: " +
                "arrays of downloaded classes successfully passed");
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
