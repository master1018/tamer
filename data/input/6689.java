public class RestartCrashedService
        implements ActivateMe
{
    private ActivationID id;
    private static Object lock = new Object();
    private Vector responders = new Vector();
    private static final String RESTARTABLE = "restartable";
    private static final String ACTIVATABLE = "activatable";
    public RestartCrashedService(ActivationID id, MarshalledObject mobj)
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
        obj.ping(responder);
    }
    public RestartCrashedService() throws RemoteException {
        UnicastRemoteObject.exportObject(this, 0);
    }
    public void ping(String responder) {
        System.err.println("RestartCrashedService: received ping from " + responder);
        synchronized (lock) {
            responders.add(responder);
            lock.notify();
        }
    }
    public boolean receivedPing(String responder) {
        return responders.contains(responder);
    }
    public void resetResponders() {
        responders.clear();
    }
    public ActivateMe getUnicastVersion() throws RemoteException {
        return new RestartCrashedService();
    }
    public void crash() {
        System.exit(0);
    }
    public ActivationID getID() {
        return id;
    }
    public static void main(String[] args) {
        System.out.println("\nRegression test for bug 4095165, 4140736\n");
        TestLibrary.suggestSecurityManager("java.rmi.RMISecurityManager");
        RMID rmid = null;
        RestartCrashedService unicastObj = null;
        try {
            RMID.removeLog();
            rmid = RMID.createRMID();
            rmid.start();
            final Properties p = new Properties();
            p.put("java.security.policy",
                  TestParams.defaultGroupPolicy);
            p.put("java.security.manager",
                  TestParams.defaultSecurityManager);
            unicastObj = new RestartCrashedService();
            System.err.println("Creating descriptors");
            Object[] stuff = new Object[] { RESTARTABLE, unicastObj };
            MarshalledObject restartMobj = new MarshalledObject(stuff);
            ActivationGroupDesc groupDesc =
                new ActivationGroupDesc(p, null);
            stuff[0] = ACTIVATABLE;
            MarshalledObject activateMobj = new MarshalledObject(stuff);
            ActivationGroupID groupID =
                ActivationGroup.getSystem().registerGroup(groupDesc);
            ActivationDesc restartableDesc =
                new ActivationDesc(groupID, "RestartCrashedService", null,
                                   restartMobj, true);
            ActivationDesc activatableDesc =
                new ActivationDesc(groupID, "RestartCrashedService", null,
                                   activateMobj, false);
            System.err.println("Registering descriptors");
            ActivateMe restartableObj =
                (ActivateMe) Activatable.register(restartableDesc);
            ActivateMe activatableObj =
                (ActivateMe) Activatable.register(activatableDesc);
            rmid.restart();
            int repeat = 1;
            do {
                for (int i = 0; i < 15; i++) {
                    synchronized (lock) {
                        if (unicastObj.receivedPing(RESTARTABLE) != true) {
                            lock.wait(5000);
                            if (unicastObj.receivedPing(RESTARTABLE) == true) {
                                System.err.println("Test1 passed: rmid " +
                                                   "restarted service");
                                break;
                            }
                        } else {
                            break;
                        }
                    }
                }
                if (unicastObj.receivedPing(RESTARTABLE) != true)
                    TestLibrary.bomb("Test1 failed: service not restarted by timeout",
                         null);
                synchronized (lock) {
                    if (unicastObj.receivedPing(ACTIVATABLE) != true) {
                        lock.wait(5000);
                        if (unicastObj.receivedPing(ACTIVATABLE) != true) {
                            System.err.println("Test2 passed: rmid did not " +
                                               "restart activatable service");
                        } else {
                            TestLibrary.bomb("Test2 failed: activatable service restarted",
                                 null);
                        }
                    } else {
                        TestLibrary.bomb("Test2 failed: activatable service restarted!",
                             null);
                    }
                }
                if (repeat > 0) {
                    try {
                        System.err.println("\nCrash restartable object");
                        unicastObj.resetResponders();
                        restartableObj.crash();
                    } catch (Exception e) {
                    }
                }
            } while (repeat-- > 0);
        } catch (Exception e) {
            TestLibrary.bomb("test failed", e);
        } finally {
            ActivationLibrary.rmidCleanup(rmid);
            TestLibrary.unexport(unicastObj);
        }
    }
}
