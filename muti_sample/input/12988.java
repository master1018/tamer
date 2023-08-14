public class NestedActivate
        extends Activatable
        implements ActivateMe, Runnable
{
    private static Exception exception = null;
    private static boolean done = false;
    private ActivateMe obj = null;
    public NestedActivate(ActivationID id, MarshalledObject mobj)
        throws Exception
    {
        super(id, 0);
        System.err.println("NestedActivate<>: activating object");
        if (mobj != null) {
            System.err.println("NestedActivate<>: ping obj to activate");
            obj = (ActivateMe) mobj.get();
            obj.ping();
            System.err.println("NestedActivate<>: ping completed");
        }
    }
    public void ping()
    {}
    public void unregister() throws Exception {
        super.unregister(super.getID());
    }
    public void shutdown() throws Exception
    {
        (new Thread(this,"NestedActivate")).start();
        if (obj != null)
            obj.shutdown();
    }
    public void run() {
        ActivationLibrary.deactivate(this, getID());
    }
    public static void main(String[] args) {
        System.err.println("\nRegression test for bug 4138056\n");
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
                        ActivationGroupID groupID =
                            ActivationGroup.getSystem().
                            registerGroup(groupDesc);
                        System.err.println("Creating descriptor: object 1");
                        ActivationDesc desc1 =
                            new ActivationDesc(groupID, "NestedActivate",
                                               null, null);
                        System.err.println("Registering descriptor: object 1");
                        ActivateMe obj1 =
                            (ActivateMe) Activatable.register(desc1);
                        System.err.println("Creating descriptor: object 2");
                        ActivationDesc desc2 =
                            new ActivationDesc(groupID, "NestedActivate", null,
                                               new MarshalledObject(obj1));
                        System.err.println("Registering descriptor: object 2");
                        ActivateMe obj2 =
                            (ActivateMe) Activatable.register(desc2);
                        System.err.println("Activating object 2");
                        obj2.ping();
                        System.err.println("Deactivating objects");
                        obj2.shutdown();
                    } catch (Exception e) {
                        exception = e;
                    }
                    done = true;
                }
            };
            t.start();
            t.join(35000);
            if (exception != null) {
                TestLibrary.bomb("test failed", exception);
            } else if (!done) {
                TestLibrary.bomb("test failed: not completed before timeout", null);
            } else {
                System.err.println("Test passed");
            }
        } catch (Exception e) {
            TestLibrary.bomb("test failed", e);
        } finally {
            ActivationLibrary.rmidCleanup(rmid);
        }
    }
}
