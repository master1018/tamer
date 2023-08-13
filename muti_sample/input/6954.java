public class CheckImplClassLoader {
    private static Object dummy = new Object();
    private static MyRMI myRMI = null;
    private static ActivationGroup group = null;
    public static void main(String args[]) {
        Object dummy1 = new Object();
        RMID rmid = null;
        System.err.println("\nRegression test for bug/rfe 4289544\n");
        try {
            URL implcb = TestLibrary.installClassInCodebase("ActivatableImpl",
                                                            "implcb");
            TestLibrary.installClassInCodebase("ActivatableImpl_Stub",
                                               "implcb");
            TestLibrary.suggestSecurityManager(
                TestParams.defaultSecurityManager);
            RMID.removeLog();
            rmid = RMID.createRMID();
            rmid.start();
            System.err.println("Create activation group in this VM");
            ActivationGroupDesc groupDesc =
                new ActivationGroupDesc(null, null);
            ActivationSystem system = ActivationGroup.getSystem();
            ActivationGroupID groupID = system.registerGroup(groupDesc);
            group = ActivationGroup.createGroup(groupID, groupDesc, 0);
            ActivationDesc desc = new ActivationDesc("ActivatableImpl",
                                                     implcb.toString(), null);
            myRMI = (MyRMI) Activatable.register(desc);
            System.err.println("Checking that impl has correct " +
                               "context class loader");
            if (!myRMI.classLoaderOk()) {
                TestLibrary.bomb("incorrect context class loader for " +
                                 "activation constructor");
            }
            System.err.println("Deactivate object via method call");
            myRMI.shutdown();
            System.err.println("\nsuccess: CheckImplClassLoader test passed ");
        } catch (Exception e) {
            TestLibrary.bomb("\nfailure: unexpected exception ", e);
        } finally {
            try {
                Thread.sleep(4000);
            } catch (InterruptedException e) {
            }
            myRMI = null;
            System.err.println("rmid shut down");
            ActivationLibrary.rmidCleanup(rmid);
            TestLibrary.unexport(group);
        }
    }
}
