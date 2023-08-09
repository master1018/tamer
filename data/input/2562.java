public class DownloadParameterClass {
    public interface FooReceiver extends Remote {
        public void receiveFoo(Object obj) throws RemoteException;
    }
    public static void main(String[] args) {
        System.err.println("\nRegression test for bug 4149366\n");
        URL codebase1 = null;
        URL codebase2 = null;
        try {
            codebase1 = TestLibrary.installClassInCodebase("FooReceiverImpl", "codebase1");
            TestLibrary.installClassInCodebase("FooReceiverImpl_Stub", "codebase1");
            TestLibrary.installClassInCodebase("Foo", "codebase1");
            codebase2 = TestLibrary.installClassInCodebase("Bar", "codebase2");
        } catch (MalformedURLException e) {
            TestLibrary.bomb("failed to install test classes", e);
        }
        TestLibrary.suggestSecurityManager("java.rmi.RMISecurityManager");
        RMID rmid = null;
        try {
            RMID.removeLog();
            rmid = RMID.createRMID();
            rmid.start();
            Properties p = new Properties();
            p.put("java.security.policy",
                  TestParams.defaultGroupPolicy);
            p.put("java.security.manager",
                  TestParams.defaultSecurityManager);
            System.err.println("Creating descriptors");
            ActivationGroupDesc groupDesc =
                new ActivationGroupDesc(p, null);
            ActivationGroupID groupID =
                ActivationGroup.getSystem().registerGroup(groupDesc);
            ActivationDesc objDesc =
                new ActivationDesc(groupID, "FooReceiverImpl",
                                   codebase1.toString(), null, false);
            System.err.println("Registering descriptors");
            FooReceiver obj = (FooReceiver) Activatable.register(objDesc);
            Class subtype = RMIClassLoader.loadClass(
                codebase2 + " " + codebase1, "Bar");
            Object subtypeInstance = subtype.newInstance();
            obj.receiveFoo(subtypeInstance);
            System.err.println("\nTEST PASSED\n");
        } catch (Exception e) {
            TestLibrary.bomb("test failed", e);
        } finally {
            ActivationLibrary.rmidCleanup(rmid);
        }
    }
}
