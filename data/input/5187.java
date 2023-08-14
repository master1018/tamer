public class RestartLatecomer
        implements ActivateMe, Runnable
{
    private ActivationID id;
    private static Object lock = new Object();
    private Vector responders = new Vector();
    private static final String RESTARTABLE = "restartable";
    private static final String ACTIVATABLE = "activatable";
    public RestartLatecomer(ActivationID id, MarshalledObject mobj)
        throws ActivationException, RemoteException
    {
        this.id = id;
        Activatable.exportObject(this, id, 0);
        ActivateMe obj;
        String responder;
        try {
            Object[] stuff = (Object[]) mobj.get();
            responder = (String) stuff[0];
            System.err.println(responder + " service started");
            obj = (ActivateMe) stuff[1];
        } catch (Exception e) {
            System.err.println("unable to obtain stub from marshalled object");
            return;
        }
        obj.callback(responder);
    }
    public RestartLatecomer() throws RemoteException {
        UnicastRemoteObject.exportObject(this, 0);
    }
    private void waitFor(String responder) throws Exception {
        synchronized (lock) {
            for (int i = 0; i < 15; i++) {
                if (responders.contains(responder) != true) {
                    lock.wait(5000);
                    if (responders.contains(responder) == true) {
                        return;
                    }
                } else {
                    return;
                }
            }
        }
        throw new RuntimeException(
            "TEST FAILED: service not restarted by timeout");
    }
    private void clearResponders() {
        synchronized (lock) {
            responders.clear();
        }
    }
    public void callback(String responder) {
        System.err.println(
            "RestartLatecomer: received callback from " + responder);
        synchronized (lock) {
            responders.add(responder);
            lock.notifyAll();
        }
    }
    public void ping() {
        System.err.println("RestartLatecomer: recevied ping");
    }
    public void shutdown() {
        System.err.println("RestartLatecomer: received shutdown request");
        (new Thread(this,"RestartLatecomer")).start();
    }
    public ActivationID getID() {
        return id;
    }
    public void run() {
        System.exit(0);
    }
    public static void main(String[] args) {
        System.out.println("\nRegression test for bug 4526514\n");
        TestLibrary.suggestSecurityManager("java.rmi.RMISecurityManager");
        RMID rmid = null;
        RestartLatecomer callbackObj = null;
        try {
            RMID.removeLog();
            rmid = RMID.createRMID();
            rmid.start();
            Properties p = new Properties();
            p.put("java.security.policy",
                  TestParams.defaultGroupPolicy);
            p.put("java.security.manager",
                  TestParams.defaultSecurityManager);
            callbackObj = new RestartLatecomer();
            System.err.println("Creating descriptors");
            Object[] stuff = new Object[] { RESTARTABLE, callbackObj };
            MarshalledObject restartMobj = new MarshalledObject(stuff);
            ActivationGroupDesc groupDesc =
                new ActivationGroupDesc(p, null);
            stuff[0] = ACTIVATABLE;
            MarshalledObject activateMobj = new MarshalledObject(stuff);
            ActivationGroupID groupID =
                ActivationGroup.getSystem().registerGroup(groupDesc);
            ActivationDesc activatableDesc =
                new ActivationDesc(groupID, "RestartLatecomer", null,
                                   activateMobj, false);
            ActivationDesc restartableDesc =
                new ActivationDesc(groupID, "RestartLatecomer", null,
                                   restartMobj, true);
            System.err.println("Register activatable object's descriptor");
            ActivateMe activatableObj =
                (ActivateMe) Activatable.register(activatableDesc);
            System.err.println("Activate object (starts group VM)");
            activatableObj.ping();
            callbackObj.waitFor(ACTIVATABLE);
            callbackObj.clearResponders();
            System.err.println("Callback from activatable object received");
            System.err.println("Register restartable object's descriptor");
            ActivateMe restartableObj =
                (ActivateMe) Activatable.register(restartableDesc);
            System.err.println("Shutdown object (exits group VM)");
            try {
                activatableObj.shutdown();
            } catch (RemoteException ignore) {
            }
            System.err.println("Pause for shutdown to happen...");
            Thread.sleep(5000);
            callbackObj.waitFor(RESTARTABLE);
            System.err.println(
                "TEST PASSED: rmid restarted latecomer service");
        } catch (Exception e) {
            TestLibrary.bomb(e);
        } finally {
            ActivationLibrary.rmidCleanup(rmid);
            TestLibrary.unexport(callbackObj);
        }
    }
}
interface ActivateMe extends Remote {
    public void ping() throws RemoteException;
    public void callback(String responder) throws RemoteException;
    public void shutdown() throws RemoteException;
}
