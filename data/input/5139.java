public class TestImpl extends UnicastRemoteObject
    implements Test {
    static Thread locker = null;
    static TestImpl foo = null;
    static TestImpl bar = null;
    TestImpl() throws RemoteException {
    }
    public String echo(String msg) throws RemoteException {
        if (locker == null) {
            locker = lockTargetExpireLeases(foo, DGCDeadLock.HOLD_TARGET_TIME);
        }
        return "Message received: " + msg;
    }
    static public void main(String[] args) {
        Registry registry = null;
        try {
            registry = java.rmi.registry.LocateRegistry.
                createRegistry(TestLibrary.REGISTRY_PORT);
            foo = new TestImpl();
            Naming.rebind("rmi:
                          TestLibrary.REGISTRY_PORT
                          + "/Foo", foo);
            try {
                bar = new TestImpl();
                Naming.rebind("rmi:
                              TestLibrary.REGISTRY_PORT
                              + "/Bar", bar);
            } catch (Exception e) {
                throw new RemoteException(e.getMessage());
            }
            Thread.sleep(DGCDeadLock.TEST_FAIL_TIME);
            System.err.println("object vm exiting...");
            System.exit(0);
        } catch (Exception e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        } finally {
            TestLibrary.unexport(registry);
            registry = null;
        }
    }
    static Thread lockTargetExpireLeases(Remote toLock, int timeOut) {
        Thread t = new Thread((Runnable) new TargetLocker(toLock, timeOut));
        t.start();
        return t;
    }
    static class TargetLocker implements Runnable {
        Remote toLock = null;
        int timeOut = 0;
        TargetLocker(Remote toLock, int timeOut) {
            this.toLock = toLock;
            this.timeOut = timeOut;
        }
        public void run() {
            try {
                Thread.currentThread().sleep(4000);
                java.security.AccessController.
                    doPrivileged(new LockTargetCheckLeases(toLock,
                                                           timeOut));
            } catch (Exception e) {
                System.err.println(e.getMessage());
                e.printStackTrace();
                System.exit(1);
            }
        }
    }
    static class LockTargetCheckLeases
        implements java.security.PrivilegedAction {
        Remote toLock = null;
        int timeOut = 0;
        LockTargetCheckLeases(Remote toLock, int timeOut) {
            this.toLock = toLock;
            this.timeOut = timeOut;
        }
        public Object run() {
            try {
                Class args[] = new Class[1];
                Class objTableClass = Class.forName
                    ("sun.rmi.transport.ObjectTable");
                args[0] = Class.forName("java.rmi.Remote");
                Method objTableGetTarget =
                    objTableClass.getDeclaredMethod("getTarget", args );
                objTableGetTarget.setAccessible(true);
                Target lockTarget =
                    ((Target) objTableGetTarget.invoke
                     (null , new Object [] {toLock} ));
                expireLeases(lockTarget);
                synchronized (lockTarget) {
                    System.err.println("Locked the relevant target, sleeping " +
                                       timeOut/1000 + " seconds");
                    Thread.currentThread().sleep(timeOut);
                    System.err.println("Target unlocked");
                }
            } catch (Exception e) {
                System.err.println(e.getMessage());
                e.printStackTrace();
                System.exit(1);
            }
            return null;
        }
    }
    static void expireLeases(Target t) throws Exception {
        final Target target = t;
        java.security.AccessController.doPrivileged(
            new java.security.PrivilegedAction() {
            public Object run() {
                try {
                    Class DGCClass = Class.forName("sun.rmi.transport.DGCImpl");
                    Method getDGCImpl =
                        DGCClass.getDeclaredMethod("getDGCImpl", null );
                    getDGCImpl.setAccessible(true);
                    DGC dgcImpl = ((DGC) getDGCImpl.invoke(null, null));
                    Field reflectedLeaseTable =
                        dgcImpl.getClass().getDeclaredField("leaseTable");
                    reflectedLeaseTable.setAccessible(true);
                    Map leaseTable = (Map) reflectedLeaseTable.get(dgcImpl);
                    synchronized (leaseTable) {
                        Iterator en = leaseTable.values().iterator();
                        while (en.hasNext()) {
                            Object info = en.next();
                            Field notifySetField =
                                info.getClass().getDeclaredField("notifySet");
                            notifySetField.setAccessible(true);
                            HashSet notifySet = (HashSet) notifySetField.get(info);
                            Iterator iter = notifySet.iterator();
                            while (iter.hasNext()) {
                                Target notified = (Target) iter.next();
                                if (notified == target) {
                                    Field expirationField = info.getClass().
                                        getDeclaredField("expiration");
                                    expirationField.setAccessible(true);
                                    expirationField.setLong(info, 0);
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    System.err.println(e.getMessage());
                    e.printStackTrace();
                    System.exit(1);
                }
                return null;
            }
        });
    }
}
