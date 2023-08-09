public class ActivateFails
        extends Activatable
        implements ActivateMe
{
    public ActivateFails(ActivationID id, MarshalledObject obj)
        throws ActivationException, RemoteException
    {
        super(id, 0);
        boolean refuseToActivate = false;
        try {
            refuseToActivate = ((Boolean)obj.get()).booleanValue();
        } catch (Exception impossible) {
        }
        if (refuseToActivate)
            throw new RemoteException("object refuses to activate");
    }
    public void ping()
    {}
    public ShutdownThread shutdown() throws Exception
    {
        ShutdownThread shutdownThread = new ShutdownThread(this, getID());
        shutdownThread.start();
        return(shutdownThread);
    }
    public static void main(String[] args)
    {
        RMID rmid = null;
        ActivateMe obj1, obj2;
        ShutdownThread shutdownThread;
        System.err.println("\nRegression test for bug 4097135\n");
        try {
            TestLibrary.suggestSecurityManager("java.rmi.RMISecurityManager");
            RMID.removeLog();
            rmid = RMID.createRMID();
            rmid.start();
            Properties p = new Properties();
            p.put("java.security.policy",
                  TestParams.defaultGroupPolicy);
            p.put("java.security.manager",
                  TestParams.defaultSecurityManager);
            System.err.println("creating activation descriptor...");
            ActivationGroupDesc groupDesc =
                new ActivationGroupDesc(p, null);
            ActivationGroupID groupID =
                ActivationGroup.getSystem().registerGroup(groupDesc);
            ActivationDesc desc1 =
                new ActivationDesc(groupID, "ActivateFails",
                                   null,
                                   new MarshalledObject(new Boolean(true)));
            ActivationDesc desc2 =
                new ActivationDesc(groupID, "ActivateFails",
                                   null,
                                   new MarshalledObject(new Boolean(false)));
            System.err.println("registering activation descriptor...");
            obj1 = (ActivateMe)Activatable.register(desc1);
            obj2 = (ActivateMe)Activatable.register(desc2);
            System.err.println("invoking method on activatable object...");
            try {
                obj1.ping();
            } catch (ActivateFailedException e) {
                System.err.println("\nsuccess: ActivateFailedException " +
                                   "generated");
                e.getMessage();
            }
            obj2.ping();
            shutdownThread = obj2.shutdown();
            Thread.sleep(2000);
            shutdownThread = null;
        } catch (Exception e) {
            TestLibrary.bomb("\nfailure: unexpected exception " +
                               e.getClass().getName() + ": " + e.getMessage(), e);
        } finally {
            obj1 = obj2 = null;
            ActivationLibrary.rmidCleanup(rmid);
        }
    }
}
