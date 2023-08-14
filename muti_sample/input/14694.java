class Callback extends UnicastRemoteObject implements CallbackInterface {
  public static int num_deactivated = 0;
  public Callback() throws RemoteException { super(); }
  public void inc() throws RemoteException {
    incNumDeactivated();
  }
  public synchronized int getNumDeactivated() throws RemoteException {
    return(num_deactivated);
  }
  public synchronized void incNumDeactivated() {
    num_deactivated++;
  }
}
public class UnregisterGroup
        extends Activatable
        implements ActivateMe, Runnable
{
    private static Exception exception = null;
    private static String error = null;
    private static boolean done = false;
    private static ActivateMe lastResortExitObj = null;
    private static final int NUM_OBJECTS = 10;
    private static int PORT = 2006;
    public UnregisterGroup(ActivationID id, MarshalledObject mobj)
        throws Exception
    {
        super(id, 0);
    }
    public void ping()
    {}
    public void unregister() throws Exception {
        super.unregister(super.getID());
    }
    public void shutdown() throws Exception {
        (new Thread(this,"UnregisterGroup")).start();
    }
    public void justGoAway() {
        System.exit(0);
    }
    public void run() {
        ActivationLibrary.deactivate(this, getID());
        System.err.println("\tActivationLibrary.deactivate returned");
        try {
            CallbackInterface cobj =
                (CallbackInterface)Naming.lookup("
            cobj.inc();
        } catch (Exception e) {
            System.err.println("cobj.inc exception");
            e.printStackTrace();
        }
    }
    public static void main(String[] args) {
        Registry registry;
        System.err.println("\nRegression test for bug 4134233\n");
        TestLibrary.suggestSecurityManager("java.rmi.RMISecurityManager");
        RMID rmid = null;
        try {
            RMID.removeLog();
            rmid = RMID.createRMID();
            rmid.start();
            final Properties p = new Properties();
            p.put("java.security.policy",
                  TestParams.defaultGroupPolicy);
            p.put("java.security.manager",
                  TestParams.defaultSecurityManager);
            Thread t = new Thread() {
                public void run () {
                    try {
                        System.err.println("Creating group descriptor");
                        ActivationGroupDesc groupDesc =
                            new ActivationGroupDesc(p, null);
                        ActivationSystem system = ActivationGroup.getSystem();
                        ActivationGroupID groupID =
                            system.registerGroup(groupDesc);
                        ActivateMe[] obj = new ActivateMe[NUM_OBJECTS];
                        for (int i = 0; i < NUM_OBJECTS; i++) {
                            System.err.println("Creating descriptor: " + i);
                            ActivationDesc desc =
                                new ActivationDesc(groupID, "UnregisterGroup",
                                                   null, null);
                            System.err.println("Registering descriptor: " + i);
                            obj[i] = (ActivateMe) Activatable.register(desc);
                            System.err.println("Activating object: " + i);
                            obj[i].ping();
                        }
                        lastResortExitObj = obj[0];
                        System.err.println("Unregistering group");
                        system.unregisterGroup(groupID);
                        try {
                            System.err.println("Get the group descriptor");
                            system.getActivationGroupDesc(groupID);
                            error = "test failed: group still registered";
                        } catch (UnknownGroupException e) {
                            System.err.println("Test passed: " +
                                               "group unregistered");
                        }
                        for (int i = 0; i < NUM_OBJECTS; i++) {
                            System.err.println("Deactivating object: " + i);
                            obj[i].shutdown();
                            obj[i] = null;
                        }
                        lastResortExitObj = null;
                    } catch (Exception e) {
                        exception = e;
                    }
                    done = true;
                }
            };
            t.start();
            t.join(120000);
            if (exception != null) {
                TestLibrary.bomb("test failed", exception);
            } else if (error != null) {
                TestLibrary.bomb(error, null);
            } else if (!done) {
                TestLibrary.bomb("test failed: not completed before timeout", null);
            } else {
                System.err.println("Test passed");
            }
        } catch (Exception e) {
            TestLibrary.bomb("test failed", e);
        } finally {
            if (lastResortExitObj != null) {
                try {
                    lastResortExitObj.justGoAway();
                } catch (Exception munch) {
                }
            }
            try {
                registry = LocateRegistry.createRegistry(PORT);
                Callback robj = new Callback();
                registry.bind("Callback", robj);
                int maxwait=30;
                int nd = robj.getNumDeactivated();
                while ((nd < NUM_OBJECTS) && (maxwait> 0)) {
                    System.err.println("num_deactivated="+nd);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ie) {}
                    maxwait--;
                    nd = robj.getNumDeactivated();
                }
            } catch (Exception ce) {
                System.err.println("E:"+ce);
                ce.printStackTrace();
            }
            ActivationLibrary.rmidCleanup(rmid);
        }
    }
}
