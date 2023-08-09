public class CheckRegisterInLog
        extends Activatable
        implements ActivateMe, Runnable
{
    public CheckRegisterInLog(ActivationID id, MarshalledObject obj)
        throws ActivationException, RemoteException
    {
        super(id, 0);
    }
    public void ping()
    {}
    public void shutdown() throws Exception
    {
        (new Thread(this,"CheckRegisterInLog")).start();
    }
    public void run() {
        ActivationLibrary.deactivate(this, getID());
    }
    public static void main(String[] args)  {
        Object dummy = new Object();
        RMID rmid = null;
        ActivateMe obj;
        System.out.println("\nRegression test for bug 4110548\n");
        CheckRegisterInLog server;
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
            System.err.println("Creating group descriptor");
            ActivationGroupDesc groupDesc =
                new ActivationGroupDesc(p, null);
            System.err.println("Registering group");
            ActivationSystem system = ActivationGroup.getSystem();
            ActivationGroupID groupID = system.registerGroup(groupDesc);
            System.err.println("Creating descriptor");
            ActivationDesc desc =
                new ActivationDesc(groupID, "CheckRegisterInLog",
                                   null, null);
            System.err.println("Registering descriptor");
            obj = (ActivateMe)Activatable.register(desc);
            rmid.restart();
            try {
                Thread.sleep(5000);
            } catch (InterruptedException ie) {
            }
            System.err.println("Activate the object via method call");
            obj.ping();
            System.err.println("Deactivate object via method call");
            obj.shutdown();
            System.err.println("\nsuccess: CheckRegisterInLog test passed ");
        } catch (Exception e) {
            System.err.println("\nfailure: unexpected exception " +
                               e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace(System.err);
            throw new RuntimeException("CheckRegisterInLog got exception " +
                                       e.getMessage());
        } finally {
            ActivationLibrary.rmidCleanup(rmid);
        }
    }
}
