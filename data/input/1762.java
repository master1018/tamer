public class PostRegisterDeadlockTest2 {
    private static String failed;
    public static interface EmptyMBean {}
    public static class Empty implements EmptyMBean, MBeanRegistration {
        public ObjectName preRegister(MBeanServer mbs, ObjectName on) {
            this.mbs = mbs;
            this.on = on;
            return on;
        }
        public void postRegister(Boolean done) {
            Thread t = new Thread() {
                public void run() {
                    if (!mbs.isRegistered(on))
                        failed = "Not registered!";
                }
            };
            t.start();
            try {
                t.join(5000L);
            } catch (InterruptedException e) {
                failed = "Interrupted: " + e;
            }
            if (t.isAlive())
                failed = "Deadlock detected";
        }
        public void preDeregister() {}
        public void postDeregister() {}
        private MBeanServer mbs;
        private ObjectName on;
    }
    public static void main(String[] args) throws Exception {
        MBeanServer mbs = MBeanServerFactory.newMBeanServer();
        ObjectName on = new ObjectName("a:b=c");
        mbs.registerMBean(new Empty(), on);
        try {
            mbs.registerMBean(new Empty(), on);
            throw new Exception("FAILED: did not get expected exception");
        } catch (InstanceAlreadyExistsException e) {
            System.out.println("OK: got expected exception");
        }
        if (failed != null)
            throw new Exception("FAILED: " + failed);
        System.out.println("TEST PASSED");
    }
}
