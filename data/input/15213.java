public class StubClassesPermitted
    extends Activatable implements Runnable, CanCreateStubs
{
    public static boolean sameGroup = false;
    private static CanCreateStubs canCreateStubs = null;
    private static Registry registry = null;
    public static void main(String args[]) {
        sameGroup = true;
        RMID rmid = null;
        System.err.println("\nRegression test for bug/rfe 4179055\n");
        try {
            TestLibrary.suggestSecurityManager("java.lang.SecurityManager");
            registry = java.rmi.registry.LocateRegistry.
                createRegistry(TestLibrary.REGISTRY_PORT);
            String smClassName =
                System.getSecurityManager().getClass().getName();
            if (!smClassName.equals("java.lang.SecurityManager")) {
                TestLibrary.bomb("Test must run with java.lang.SecurityManager");
            }
            RMID.removeLog();
            rmid = RMID.createRMID();
            rmid.start();
            Properties p = new Properties();
            p.put("java.security.policy",
                  TestParams.defaultGroupPolicy);
            p.put("java.security.manager",
                  "java.lang.SecurityManager");
            System.err.println("Create activation group, in a new VM");
            ActivationGroupDesc groupDesc =
                new ActivationGroupDesc(p, null);
            ActivationSystem system = ActivationGroup.getSystem();
            ActivationGroupID groupID = system.registerGroup(groupDesc);
            System.err.println("register activatable");
            ActivationDesc desc = new ActivationDesc
                (groupID, "StubClassesPermitted", null, null);
            canCreateStubs = (CanCreateStubs) Activatable.register(desc);
            System.err.println("getting the registry");
            registry = canCreateStubs.getRegistry();
            try {
                System.err.println("accessing forbidden class");
                Object secureRandom = canCreateStubs.getForbiddenClass();
                TestLibrary.bomb("test allowed to access forbidden class," +
                                 " sun.security.provider.SecureRandom");
            } catch (java.security.AccessControlException e) {
                ByteArrayOutputStream bout = new ByteArrayOutputStream();
                PrintStream ps = new PrintStream(bout);
                e.printStackTrace(ps);
                ps.flush();
                String trace = new String(bout.toByteArray());
                if ((trace.indexOf("exceptionReceivedFromServer") >= 0) ||
                    trace.equals(""))
                {
                    throw e;
                }
                System.err.println("received expected local access control exception");
            }
            System.err.println("returning group desc");
            canCreateStubs.returnGroupID();
            System.err.println
                ("Deactivate object via method call");
            canCreateStubs.shutdown();
            System.err.println
                ("\nsuccess: StubClassesPermitted test passed ");
        } catch (Exception e) {
            TestLibrary.bomb("\nfailure: unexpected exception ", e);
        } finally {
            try {
                Thread.sleep(4000);
            } catch (InterruptedException e) {
            }
            canCreateStubs = null;
            ActivationLibrary.rmidCleanup(rmid);
            System.err.println("rmid shut down");
        }
    }
    static ActivationGroupID GroupID = null;
    public StubClassesPermitted
        (ActivationID id, MarshalledObject mo) throws RemoteException
    {
        super(id, 0);
        registry = java.rmi.registry.LocateRegistry.
            getRegistry(TestLibrary.REGISTRY_PORT);
    }
    public void shutdown() throws Exception {
        (new Thread(this,"StubClassesPermitted")).start();
    }
    public void run() {
        ActivationLibrary.deactivate(this, getID());
    }
    public Registry getRegistry() throws RemoteException {
        if (sameGroup) {
            System.out.println("in same group");
        } else {
            System.out.println("not in same group");
        }
        return registry;
    }
    public Object getForbiddenClass() throws RemoteException {
        System.err.println("creating sun class");
        return new sun.security.provider.SecureRandom();
    }
    public ActivationGroupID returnGroupID() throws RemoteException {
        return ActivationGroup.currentGroupID();
    }
}
