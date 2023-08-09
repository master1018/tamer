public class InactiveGroup
        implements ActivateMe, Runnable
{
    private ActivationID id;
    public InactiveGroup(ActivationID id, MarshalledObject obj)
        throws ActivationException, RemoteException
    {
        this.id = id;
        Activatable.exportObject(this, id, 0);
    }
    public InactiveGroup() throws RemoteException {
        UnicastRemoteObject.exportObject(this, 0);
    }
    public void ping()
    {}
    public ActivateMe getUnicastVersion() throws RemoteException {
        return new InactiveGroup();
    }
    public ActivationID getID() {
        return id;
    }
    public void shutdown() throws Exception
    {
        (new Thread(this,"InactiveGroup")).start();
    }
    public void run()
    {
        ActivationLibrary.deactivate(this, getID());
    }
    public static void main(String[] args) {
        System.out.println("\nRegression test for bug 4116082\n");
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
            System.err.println("Creating descriptor");
            ActivationGroupDesc groupDesc =
                new ActivationGroupDesc(p, null);
            ActivationGroupID groupID =
                ActivationGroup.getSystem().registerGroup(groupDesc);
            ActivationDesc desc =
                new ActivationDesc(groupID, "InactiveGroup", null, null);
            System.err.println("Registering descriptor");
            ActivateMe activatableObj = (ActivateMe) Activatable.register(desc);
            System.err.println("Activate object via method call");
            activatableObj.ping();
            System.err.println("Obtain unicast object");
            ActivateMe unicastObj = activatableObj.getUnicastVersion();
            System.err.println("Make activatable object inactive");
            activatableObj.shutdown();
            System.err.println("Ping unicast object for existence");
            for (int i = 0; i < 10; i++) {
                unicastObj.ping();
                Thread.sleep(500);
            }
            System.err.println("Reactivate activatable obj");
            activatableObj.ping();
            try {
                System.err.println("Ping unicast object again");
                unicastObj.ping();
            } catch (Exception thisShouldFail) {
                System.err.println("Test passed: couldn't reach unicast obj: " +
                                   thisShouldFail.getMessage());
                return;
            }
            TestLibrary.bomb("Test failed: unicast obj accessible after group reactivates",
                 null);
        } catch (Exception e) {
            TestLibrary.bomb("test failed", e);
        } finally {
            ActivationLibrary.rmidCleanup(rmid);
        }
    }
}
