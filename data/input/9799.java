public class UnregisterInactive
        extends Activatable
        implements ActivateMe, Runnable
{
    public UnregisterInactive(ActivationID id, MarshalledObject obj)
        throws ActivationException, RemoteException
    {
        super(id, 0);
    }
    public void ping()
    {}
    public void unregister() throws Exception {
        super.unregister(super.getID());
    }
    public void shutdown() throws Exception
    {
        (new Thread(this,"UnregisterInactive")).start();
    }
    public void run() {
        ActivationLibrary.deactivate(this, getID());
    }
    public static void main(String[] args) {
        System.out.println("\nRegression test for bug 4115331\n");
        TestLibrary.suggestSecurityManager("java.rmi.RMISecurityManager");
        RMID rmid = null;
        try {
            RMID.removeLog();
            rmid = RMID.createRMID();
            rmid.start();
            System.err.println("Creating descriptor");
            Properties p = new Properties();
            p.put("java.security.policy",
                  TestParams.defaultGroupPolicy);
            p.put("java.security.manager",
                  TestParams.defaultSecurityManager);
            ActivationGroupDesc groupDesc =
                new ActivationGroupDesc(p, null);
            ActivationSystem system = ActivationGroup.getSystem();
            ActivationGroupID groupID = system.registerGroup(groupDesc);
            ActivationGroup.createGroup(groupID, groupDesc, 0);
            ActivationDesc desc =
                new ActivationDesc("UnregisterInactive", null, null);
            System.err.println("Registering descriptor");
            ActivateMe obj = (ActivateMe) Activatable.register(desc);
            System.err.println("Activate object via method call");
            obj.ping();
            System.err.println("Unregister object");
            obj.unregister();
            System.err.println("Make object inactive");
            obj.shutdown();
        } catch (Exception e) {
            TestLibrary.bomb("test failed", e);
        } finally {
            ActivationLibrary.rmidCleanup(rmid);
        }
    }
}
