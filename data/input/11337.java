public class ForceLogSnapshot
        implements ActivateMe
{
    final public static int HOW_MANY = 50;
    final public static int NUM_GROUPS = 4;
    final public static int SNAPSHOT_INTERVAL = 10;
    private ActivationID id;
    private Vector responders = new Vector();
    private static final String RESTARTABLE = "restartable";
    private static final String ACTIVATABLE = "activatable";
    private static Object lock = new Object();
    private static boolean[] restartedObjects = new boolean[HOW_MANY];
    private static boolean[] activatedObjects = new boolean[HOW_MANY];
    public ForceLogSnapshot(ActivationID id, MarshalledObject mobj)
        throws ActivationException, RemoteException
    {
        this.id = id;
        int intId = 0;
        Activatable.exportObject(this, id, 0);
        ActivateMe obj;
        String responder;
        try {
            Object[] stuff = (Object[]) mobj.get();
            intId = ((Integer) stuff[0]).intValue();
            responder = (String) stuff[1];
            obj = (ActivateMe) stuff[2];
            System.err.println(responder + " service started");
        } catch (Exception e) {
            System.err.println("unable to obtain stub from marshalled object");
            System.err.println(e.getMessage());
            e.printStackTrace();
            return;
        }
        obj.ping(intId, responder);
    }
    public ForceLogSnapshot() throws RemoteException {
        UnicastRemoteObject.exportObject(this, 0);
    }
    public void ping(int intId, String responder) {
        System.err.println("ForceLogSnapshot: received ping from " +
                           responder);
        if (responder.equals(RESTARTABLE)) {
            synchronized (lock) {
                restartedObjects[intId] = true;
            }
        } else if (responder.equals(ACTIVATABLE)) {
            synchronized (lock) {
                activatedObjects[intId] = true;
            }
        }
    }
    public void crash() {
        System.exit(0);
    }
    public ActivationID getID() {
        return id;
    }
    public static void main(String[] args) {
        System.out.println("\nRegression test for bug 4173960\n");
        TestLibrary.suggestSecurityManager("java.rmi.RMISecurityManager");
        RMID rmid = null;
        ForceLogSnapshot[] unicastObjs = new ForceLogSnapshot[HOW_MANY];
        try {
            String option = " -Dsun.rmi.activation.snapshotInterval=" +
                SNAPSHOT_INTERVAL;
            RMID.removeLog();
            rmid = RMID.createRMID();
            rmid.addOptions(new String[] {option, "-Djava.compiler="});
            rmid.start();
            Properties p = new Properties();
            p.put("java.security.policy",
                  TestParams.defaultGroupPolicy);
            p.put("java.security.manager",
                  TestParams.defaultSecurityManager);
            Object[][] stuff = new Object[HOW_MANY][];
            MarshalledObject restartMobj = null;
            ActivationGroupDesc groupDesc = null;
            MarshalledObject activateMobj = null;
            ActivationGroupID[] groupIDs = new ActivationGroupID[NUM_GROUPS];
            ActivationDesc restartableDesc = null;
            ActivationDesc activatableDesc = null;
            ActivateMe[] restartableObj = new ActivateMe[HOW_MANY];
            ActivateMe[] activatableObj = new ActivateMe[HOW_MANY];
            int group = 0;
            int groupNo = 0;
            for (int i = 0 ; i < HOW_MANY ; i ++ ) {
                System.err.println("Creating descriptors and remote objects");
                unicastObjs[i] = new ForceLogSnapshot();
                stuff[i] = new Object[] { new Integer(i),
                                              RESTARTABLE, unicastObjs[i] };
                restartMobj = new MarshalledObject(stuff[i]);
                stuff[i][1] = ACTIVATABLE;
                activateMobj = new MarshalledObject(stuff[i]);
                groupDesc =
                    new ActivationGroupDesc(p, null);
                if (i < NUM_GROUPS) {
                    groupNo = i;
                    groupIDs[groupNo] =
                        ActivationGroup.getSystem().
                        registerGroup(groupDesc);
                } else {
                    groupNo = (group++)%NUM_GROUPS;
                }
                System.err.println("Objects group number: " + groupNo);
                restartableDesc =
                    new ActivationDesc(groupIDs[groupNo], "ForceLogSnapshot", null,
                                       restartMobj, true);
                activatableDesc =
                    new ActivationDesc(groupIDs[groupNo], "ForceLogSnapshot", null,
                                       activateMobj, false);
                System.err.println("Registering descriptors");
                restartableObj[i] =
                    (ActivateMe) Activatable.register(restartableDesc);
                activatableObj[i] =
                    (ActivateMe) Activatable.register(activatableDesc);
                System.err.println("registered activatable #: " + i);
            }
            int repeatOnce = 1;
            do {
                rmid.restart();
                if (howManyRestarted(restartedObjects, 10) < HOW_MANY) {
                        TestLibrary.bomb("Test1 failed: a service would not " +
                                         "restart");
                }
                System.err.println("Test1 passed: rmid " +
                                   "all service(s) restarted. Performing next test.");
                if (howManyRestarted(activatedObjects, 2) != 0) {
                    TestLibrary.bomb("Test2 failed: activatable service restarted!",
                                     null);
                }
                System.err.println("Test2 passed: rmid did not " +
                                   "restart activatable service(s)");
                if (repeatOnce > 0) {
                    try {
                        System.err.println("\nCrash restartable object");
                        for (int i = 0 ; i < HOW_MANY ; i ++) {
                            restartableObj[i].crash();
                        }
                    } catch (Exception e) {
                    }
                }
            } while (repeatOnce-- > 0);
        } catch (Exception e) {
            TestLibrary.bomb("test failed", e);
        } finally {
            ActivationLibrary.rmidCleanup(rmid);
            for (int i = 0 ; i < HOW_MANY ; i ++) {
                TestLibrary.unexport(unicastObjs[i]);
            }
        }
    }
    private static int howManyRestarted(boolean[] startedObjects, int retries) {
        int succeeded = 0;
        int restarted = 0;
        int atry = 0;
        while ((restarted < HOW_MANY) && (atry < retries)) {
            restarted = 0;
            for (int j = 0 ; j < HOW_MANY ; j ++ ) {
                synchronized(lock) {
                    if (startedObjects[j]) {
                        restarted ++;
                    }
                }
            }
            System.err.println("not all objects restarted, retrying...");
            try {
                Thread.sleep(10000);
            } catch (InterruptedException ie) {
            }
            atry ++;
        }
        return restarted;
    }
}
