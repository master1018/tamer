public class NonExistentActivatable
        extends Activatable
        implements ActivateMe, Runnable
{
    public NonExistentActivatable(ActivationID id, MarshalledObject obj)
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
        (new Thread(this,"NonExistentActivatable")).start();
    }
    public void run()
    {
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
                new ActivationDesc("NonExistentActivatable", null, null);
            System.err.println("Registering descriptor");
            ActivateMe obj = (ActivateMe) Activatable.register(desc);
            System.err.println("Activate object via method call");
            obj.ping();
            System.err.println("Unregister object");
            obj.unregister();
            System.err.println("Make object inactive");
            obj.shutdown();
            System.err.println("Reactivate object");
            try {
                obj.ping();
            } catch (NoSuchObjectException e) {
                System.err.println("Test succeeded: " +
                                   "NoSuchObjectException caught");
                return;
            } catch (Exception e) {
                TestLibrary.bomb("Test failed: exception other than NoSuchObjectException",
                     e);
            }
        } catch (Exception e) {
            TestLibrary.bomb("test failed", e);
        } finally {
            ActivationLibrary.rmidCleanup(rmid);
        }
    }
}
