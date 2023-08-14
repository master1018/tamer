public class UseCustomSocketFactory {
    Hello hello = null;
    public static void main(String[] args) {
        Registry registry = null;
        HelloImpl impl = null;
        System.out.println("\nRegression test for bug 4148850\n");
        TestLibrary.suggestSecurityManager("java.rmi.RMISecurityManager");
        try {
            impl = new HelloImpl();
            registry = LocateRegistry.
                createRegistry(TestLibrary.REGISTRY_PORT,
                               new Compress.CompressRMIClientSocketFactory(),
                               new Compress.CompressRMIServerSocketFactory());
            registry.rebind("/HelloServer", impl);
            checkStub(registry, "RMIServerSocket");
        } catch (Exception e) {
            TestLibrary.bomb("creating registry", e);
        }
        JavaVM serverVM = new JavaVM("HelloImpl", "-Djava.security.policy=" +
                                     TestParams.defaultPolicy, "");
        try {
            serverVM.start();
            synchronized (impl) {
                System.out.println("waiting for remote notification");
                if (!HelloImpl.clientCalledSuccessfully) {
                    impl.wait(75 * 1000);
                }
                if (!HelloImpl.clientCalledSuccessfully) {
                    throw new RuntimeException("Client did not execute call in time...");
                }
            }
            System.err.println("\nRegression test for bug 4148850 passed.\n ");
        } catch (Exception e) {
            TestLibrary.bomb("test failed", e);
        } finally {
            serverVM.destroy();
            try {
                registry.unbind("/HelloServer");
            } catch (Exception e) {
                TestLibrary.bomb("unbinding HelloServer", e);
            }
            TestLibrary.unexport(registry);
            TestLibrary.unexport(impl);
            impl = null;
            registry = null;
        }
    }
    static void checkStub(Object stub, String toCheck) throws RemoteException {
        System.err.println("Ensuring that the stub contains a socket factory string: " +
                           toCheck);
        System.err.println(stub);
        if (stub.toString().indexOf(toCheck) < 0) {
            throw new RemoteException("RemoteStub.toString() did not contain instance of "
                                      + toCheck);
        }
    }
}
