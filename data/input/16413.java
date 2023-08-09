public class ElucidateNoSuchMethod
        extends Activatable
        implements ActivateMe, Runnable
{
    ElucidateNoSuchMethod(ActivationID id, int port)
        throws RemoteException
    {
        super(id, port);
    }
    public void ping() {}
    public void shutdown() throws Exception {
        (new Thread(this,"ElucidateNoSuchMethod")).start();
    }
    public void run() {
        ActivationLibrary.deactivate(this, getID());
    }
    public static void main(String[] args) {
        System.out.println("\nRegression test for 4128620 \n");
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
            System.err.println("Create activation group in this VM");
            ActivationGroupDesc groupDesc =
                new ActivationGroupDesc(p, null);
            ActivationSystem system = ActivationGroup.getSystem();
            ActivationGroupID groupID = system.registerGroup(groupDesc);
            ActivationGroup.createGroup(groupID, groupDesc, 0);
            System.err.println("Creating descriptor");
            ActivationDesc desc =
                new ActivationDesc("ElucidateNoSuchMethod", null, null);
            System.err.println("Registering descriptor");
            ActivateMe obj = (ActivateMe) Activatable.register(desc);
            System.err.println("Activate object via method call");
            try {
                obj.ping();
            } catch (ActivateFailedException afe) {
                ActivationException a = (ActivationException) afe.detail;
                if (((a.detail instanceof NoSuchMethodException) ||
                     (a.detail instanceof NoSuchMethodError)) &&
                        (a.getMessage().indexOf
                      ("must provide an activation constructor") > -1)) {
                    System.err.println("\ntest passed for 4128620\n");
                } else {
                    TestLibrary.bomb("test failed", afe);
                }
            }
        } catch (Exception e) {
            TestLibrary.bomb("test failed", e);
        } finally {
            ActivationLibrary.rmidCleanup(rmid);
        }
    }
}
