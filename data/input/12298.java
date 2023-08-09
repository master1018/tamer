public class UseCodebaseOnly
    extends UnicastRemoteObject
    implements Receiver
{
    public UseCodebaseOnly() throws RemoteException {
    }
    public void receive(Object obj) {
        System.err.println("+ receive(): received object " + obj);
    }
    public static void main(String[] args) {
        System.err.println("\nRegression test for bug 4174006\n");
        URL localCodebase = null, remoteCodebase = null;
        try {
            remoteCodebase =
                TestLibrary.installClassInCodebase("Foo", "remote_codebase");
            localCodebase =
                TestLibrary.installClassInCodebase("Bar", "local_codebase");
        } catch (MalformedURLException e) {
            TestLibrary.bomb(e);
        }
        TestLibrary.setProperty("java.rmi.server.useCodebaseOnly", "true");
        TestLibrary.setProperty(        
            "java.rmi.server.codebase", localCodebase.toString());
        System.err.println("Creating class loader for remote codebase " +
            remoteCodebase);
        ClassLoader remoteCodebaseLoader =
            URLClassLoader.newInstance(new URL[] { remoteCodebase });
        System.err.println("Creating class loader for local codebase " +
            localCodebase);
        ClassLoader localCodebaseLoader =
            URLClassLoader.newInstance(new URL[] { localCodebase });
        TestLibrary.suggestSecurityManager(null);
        System.err.println("Creating remote object.");
        UseCodebaseOnly obj = null;
        try {
            obj = new UseCodebaseOnly();
        } catch (RemoteException e) {
            TestLibrary.bomb(e);
        }
        try {
            Receiver stub = (Receiver) RemoteObject.toStub(obj);
            System.err.println(
                "Passing class from local codebase (should succeed).");
            Class barClass = localCodebaseLoader.loadClass("Bar");
            Object barObj = barClass.newInstance();
            stub.receive(barObj);
            System.err.println(
                "Passing class from remote codebase (should fail).");
            Class fooClass = remoteCodebaseLoader.loadClass("Foo");
            Object fooObj = fooClass.newInstance();
            try {
                stub.receive(fooObj);
                throw new RuntimeException("TEST FAILED: " +
                    "class from remote codebase sucesssfully unmarshalled");
            } catch (RemoteException e) {
                if ((e instanceof ServerException) &&
                    (e.detail instanceof UnmarshalException) &&
                    (((RemoteException) e.detail).detail instanceof
                        ClassNotFoundException) &&
                    (((RemoteException) e.detail).detail.getMessage().equals(
                        "Foo")))
                {
                    System.err.println("TEST PASSED: ");
                    e.printStackTrace();
                } else {
                    throw e;
                }
            }
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
