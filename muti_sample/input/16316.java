public class ExtLoadedImplTest {
    public static void main(String[] args) {
        System.err.println("\nRegression test for bug 4500504\n");
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
            p.put("java.ext.dirs", "ext");
            System.err.println("Creating descriptors");
            ActivationGroupDesc groupDesc =
                new ActivationGroupDesc(p, null);
            ActivationGroupID groupID =
                ActivationGroup.getSystem().registerGroup(groupDesc);
            ActivationDesc objDesc =
                new ActivationDesc(groupID, "ExtLoadedImpl", null, null);
            System.err.println("Registering descriptors");
            CheckLoader obj = (CheckLoader) Activatable.register(objDesc);
            boolean result = obj.isCorrectContextLoader();
            System.err.println("\nTEST " +
                               ((result) ? "PASSED" : "FAILED") + "\n");
            if (!result) {
                throw new RuntimeException("TEST FAILED");
            }
        } catch (Exception e) {
            TestLibrary.bomb("test failed", e);
        } finally {
            ActivationLibrary.rmidCleanup(rmid);
        }
    }
}
