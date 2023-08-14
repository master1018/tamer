public class DownloadActivationGroup
        implements Ping, Runnable
{
    private ActivationID id;
    public DownloadActivationGroup(ActivationID id, MarshalledObject mobj)
        throws ActivationException, RemoteException
    {
        this.id = id;
        Activatable.exportObject(this, id, 0);
        System.err.println("object activated in group");
    }
    public DownloadActivationGroup() throws RemoteException {
        UnicastRemoteObject.exportObject(this, 0);
    }
    public void ping() {
        System.err.println("received ping");
    }
    public void shutdown() throws Exception
    {
        (new Thread(this,"DownloadActivationGroup")).start();
    }
    public void run() {
        ActivationLibrary.deactivate(this, getID());
    }
    public ActivationID getID() {
        return id;
    }
    public static void main(String[] args) {
        RMID rmid = null;
        System.out.println("\nRegression test for bug 4510355\n");
        try {
            TestLibrary.suggestSecurityManager("java.lang.SecurityManager");
            System.err.println("install class file in codebase");
            URL groupURL = TestLibrary.installClassInCodebase(
                                  "MyActivationGroupImpl", "group");
            System.err.println("class file installed");
            RMID.removeLog();
            rmid = RMID.createRMID();
            String execPolicyOption = "-Dsun.rmi.activation.execPolicy=none";
            rmid.addOptions(new String[] { execPolicyOption });
            rmid.start();
            System.err.println("register group");
            Properties p = new Properties();
            p.put("java.security.policy", TestParams.defaultGroupPolicy);
            ActivationGroupDesc groupDesc =
                new ActivationGroupDesc("MyActivationGroupImpl",
                                        groupURL.toExternalForm(),
                                        null, p, null);
            ActivationGroupID groupID =
                ActivationGroup.getSystem().registerGroup(groupDesc);
            System.err.println("register activatable object");
            ActivationDesc desc =
                new ActivationDesc(groupID, "DownloadActivationGroup",
                                   null, null);
            Ping obj = (Ping) Activatable.register(desc);
            System.err.println(
                "ping object (forces download of group's class)");
            obj.ping();
            System.err.println(
                "TEST PASSED: group's class downloaded successfully");
            System.err.println("shutdown object");
            obj.shutdown();
            System.err.println("TEST PASSED");
        } catch (Exception e) {
            TestLibrary.bomb(e);
        } finally {
            ActivationLibrary.rmidCleanup(rmid);
        }
    }
}
interface Ping extends Remote {
    public void ping() throws RemoteException;
    public void shutdown() throws Exception;
}
