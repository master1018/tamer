public class PreDeregisterDeadlockTest {
    public static interface BlibbyMBean {}
    public static class Blibby implements BlibbyMBean, MBeanRegistration {
        public Blibby(MBeanServer mbs, ObjectName otherName) {
            this.mbs = mbs;
            this.otherName = otherName;
        }
        public ObjectName preRegister(MBeanServer mbs, ObjectName on) {
            return on;
        }
        public void postRegister(Boolean done) {}
        public void preDeregister() {
            if (otherName == null)
                return;
            try {
                Thread t = new Thread() {
                    public void run() {
                        try {
                            mbs.unregisterMBean(otherName);
                        } catch (Throwable e) {
                            e.printStackTrace(System.out);
                            fail(e.toString());
                        }
                    }
                };
                t.start();
                t.join(5000L);
                if (t.isAlive())
                    fail("Deadlock detected");
            } catch (Throwable e) {
                e.printStackTrace(System.out);
                fail(e.toString());
            }
        }
        public void postDeregister() {}
        private final MBeanServer mbs;
        private final ObjectName otherName;
    }
    public static interface BlobbyMBean {}
    public static class Blobby implements BlobbyMBean, MBeanRegistration {
        public Blobby(MBeanServer mbs, Semaphore semaphore) {
            this.mbs = mbs;
            this.semaphore = semaphore;
        }
        public ObjectName preRegister(MBeanServer mbs, ObjectName on) {
            this.objectName = on;
            return on;
        }
        public void postRegister(Boolean done) {}
        public void preDeregister() throws Exception {
            Thread t = new Thread() {
                public void run() {
                    try {
                        mbs.unregisterMBean(objectName);
                        fail("Nested unregister succeeded");
                    } catch (InstanceNotFoundException e) {
                        semaphore.release();
                    } catch (Throwable e) {
                        e.printStackTrace(System.out);
                        fail(e.toString());
                    }
                }
            };
            t.start();
            Thread.sleep(500L);
        }
        public void postDeregister() {}
        private final MBeanServer mbs;
        private ObjectName objectName;
        private final Semaphore semaphore;
    }
    public static void main(String[] args) throws Exception {
        MBeanServer mbs = MBeanServerFactory.newMBeanServer();
        ObjectName on1 = new ObjectName("a:type=Blibby,name=\"1\"");
        ObjectName on2 = new ObjectName("a:type=Blibby,name=\"2\"");
        mbs.registerMBean(new Blibby(mbs, on2), on1);
        mbs.registerMBean(new Blibby(mbs, null), on2);
        mbs.unregisterMBean(on1);
        Semaphore semaphore = new Semaphore(0);
        mbs.registerMBean(new Blobby(mbs, semaphore), on1);
        mbs.unregisterMBean(on1);
        boolean ok = semaphore.tryAcquire(1, 5, TimeUnit.SECONDS);
        if (!ok)
            fail("Second unregister thread did not complete");
        if (failure == null)
            System.out.println("OK: Test passed");
        else
            throw new Exception("TEST FAILED: " + failure);
    }
    private static void fail(String why) {
        System.out.println("FAILED: " + why);
        failure = why;
    }
    private static volatile String failure;
}
