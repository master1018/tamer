public class CheckLeaseLeak extends UnicastRemoteObject implements LeaseLeak {
    public CheckLeaseLeak() throws RemoteException { }
    public void ping () throws RemoteException { }
    private final static int DGC_ID = 2;
    private final static int ITERATIONS = 10;
    private final static int numberPingCalls = 0;
    private final static int CHECK_INTERVAL = 400;
    private final static int LEASE_VALUE = 20;
    public static void main (String[] args) {
        CheckLeaseLeak leakServer = null;
        int numLeft =0;
        TestLibrary.setInteger("sun.rmi.dgc.checkInterval",
                               CHECK_INTERVAL);
        TestLibrary.setInteger("java.rmi.dgc.leaseValue",
                               LEASE_VALUE);
        try {
            Registry registry =
                java.rmi.registry.LocateRegistry.
                    createRegistry(TestLibrary.REGISTRY_PORT);
            leakServer = new CheckLeaseLeak();
            registry.rebind("/LeaseLeak", leakServer);
            for (int i = 0 ; i < ITERATIONS ; i ++ ) {
                System.err.println("Created client: " + i);
                JavaVM jvm = new JavaVM("LeaseLeakClient",
                                        " -Djava.security.policy=" +
                                        TestParams.defaultPolicy, "");
                jvm.start();
                if (jvm.getVM().waitFor() == 1 ) {
                    TestLibrary.bomb("Client process failed");
                }
            }
            numLeft = getDGCLeaseTableSize();
            Thread.sleep(3000);
        } catch(Exception e) {
            TestLibrary.bomb("CheckLeaseLeak Error: ", e);
        } finally {
            if (leakServer != null) {
                TestLibrary.unexport(leakServer);
                leakServer = null;
            }
        }
        if (numLeft > 2) {
            TestLibrary.bomb("Too many objects in DGCImpl.leaseTable: "+
                            numLeft);
        } else {
            System.err.println("Check leaseInfo leak passed with " +
                               numLeft
                                   + " object(s) in the leaseTable");
        }
    }
    private static int getDGCLeaseTableSize () {
        int numLeaseInfosLeft = 0;
        Map leaseTable = null;
        final Remote[] dgcImpl = new Remote[1];
        Field f;
        try {
            f = (Field) java.security.AccessController.doPrivileged
                (new java.security.PrivilegedExceptionAction() {
                    public Object run() throws Exception {
                        ObjID dgcID = new ObjID(DGC_ID);
                        Class oeClass =
                            Class.forName("sun.rmi.transport.ObjectEndpoint");
                        Class[] constrParams =
                            new Class[]{ ObjID.class, Transport.class };
                        Constructor oeConstructor =
                            oeClass.getDeclaredConstructor(constrParams);
                        oeConstructor.setAccessible(true);
                        Object oe =
                            oeConstructor.newInstance(
                                new Object[]{ dgcID, null });
                        Class objTableClass =
                            Class.forName("sun.rmi.transport.ObjectTable");
                        Class getTargetParams[] = new Class[] { oeClass };
                        Method objTableGetTarget =
                            objTableClass.getDeclaredMethod("getTarget",
                                                            getTargetParams);
                        objTableGetTarget.setAccessible(true);
                        Target dgcTarget = (Target)
                            objTableGetTarget.invoke(null, new Object[]{ oe });
                        Method targetGetImpl =
                            dgcTarget.getClass().getDeclaredMethod
                            ("getImpl", null);
                        targetGetImpl.setAccessible(true);
                        dgcImpl[0] =
                            (Remote) targetGetImpl.invoke(dgcTarget, null);
                        Field reflectedLeaseTable =
                            dgcImpl[0].getClass().getDeclaredField
                            ("leaseTable");
                        reflectedLeaseTable.setAccessible(true);
                        return reflectedLeaseTable;
                    }
            });
            leaseTable = (Map) f.get(dgcImpl[0]);
            numLeaseInfosLeft = leaseTable.size();
        } catch(Exception e) {
            if (e instanceof java.security.PrivilegedActionException)
                e = ((java.security.PrivilegedActionException) e).
                    getException();
            TestLibrary.bomb(e);
        }
        return numLeaseInfosLeft;
    }
}
