public class CreatePrivateActivatable
{
    private static class PrivateActivatable extends Activatable
        implements ActivateMe, Runnable
    {
        private PrivateActivatable(ActivationID id, MarshalledObject obj)
            throws ActivationException, RemoteException
        {
            super(id, 0);
        }
        public void ping()
        {}
        public void shutdown() throws Exception
        {
            (new Thread(this, "CreatePrivateActivatable$PrivateActivatable")).start();
        }
        public void run() {
            ActivationLibrary.deactivate(this, getID());
        }
    }
    public static void main(String[] args)  {
        Object dummy = new Object();
        RMID rmid = null;
        ActivateMe obj;
        System.err.println("\nRegression test for bug 4164971\n");
        System.err.println("java.security.policy = " +
                           System.getProperty("java.security.policy", "no policy"));
        CreatePrivateActivatable server;
        try {
            TestLibrary.suggestSecurityManager(TestParams.defaultSecurityManager);
            RMID.removeLog();
            rmid = RMID.createRMID();
            rmid.start();
            Properties p = new Properties();
            p.put("java.security.policy",
                  TestParams.defaultGroupPolicy);
            p.put("java.security.manager",
                  TestParams.defaultSecurityManager);
            ActivationGroupDesc groupDesc =
                new ActivationGroupDesc(p, null);
            ActivationSystem system = ActivationGroup.getSystem();
            ActivationGroupID groupID = system.registerGroup(groupDesc);
            System.err.println("Creating descriptor");
            ActivationDesc desc =
                new ActivationDesc(groupID,
                    "CreatePrivateActivatable$PrivateActivatable",
                     null, null);
            System.err.println("Registering descriptor");
            obj = (ActivateMe) Activatable.register(desc);
            System.err.println("Activate object via method call");
            obj.ping();
            System.err.println("Deactivate object via method call");
            obj.shutdown();
            System.err.println("\nsuccess: CreatePrivateActivatable test passed ");
        } catch (Exception e) {
            if (e instanceof java.security.PrivilegedActionException) {
                e = ((java.security.PrivilegedActionException)e).getException();
            }
            TestLibrary.bomb("\nfailure: unexpected exception " +
                             e.getClass().getName(), e);
        } finally {
            ActivationLibrary.rmidCleanup(rmid);
            obj = null;
        }
    }
}
