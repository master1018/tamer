public class PostRegisterDeadlockTest {
    public static interface BlibbyMBean {}
    public static class Blibby implements BlibbyMBean, MBeanRegistration {
        public Blibby(MBeanServer mbs, ObjectName otherName) {
            this.mbs = mbs;
            this.otherName = otherName;
        }
        public ObjectName preRegister(MBeanServer mbs, ObjectName on) {
            return on;
        }
        public void preDeregister() {}
        public void postRegister(Boolean done) {
            if (otherName == null) return;
            try {
                Thread t = new Thread() {
                    public void run() {
                        try {
                            try {
                                mbs.unregisterMBean(otherName);
                            } catch (InstanceNotFoundException x) {
                                 System.out.println(otherName+
                                         " was unregistered by main thread.");
                            }
                        } catch (Throwable e) {
                            e.printStackTrace(System.out);
                            fail(e.toString());
                        }
                    }
                };
                t.start();
                t.join(5000L);
                if (t.isAlive()) {
                    if (t.getState().equals(State.BLOCKED))
                        fail("Deadlock detected");
                    else
                        fail("Test not conclusive: "+
                             "Thread is alive but not blocked.");
                }
            } catch (Throwable e) {
                e.printStackTrace(System.out);
                fail(e.toString());
            }
        }
        public void postDeregister() {}
        private final MBeanServer mbs;
        private final ObjectName otherName;
    }
    public static void main(String[] args) throws Exception {
        String previous = null;
        MBeanServer mbs = MBeanServerFactory.newMBeanServer();
        ObjectName on1 = new ObjectName("a:type=Blibby,name=\"1\"");
        ObjectName on2 = new ObjectName("a:type=Blibby,name=\"2\"");
        System.out.println("\n****  TEST #1 ****\n");
        System.out.println("Registering Blibby #1 with name: " + on1);
        mbs.registerMBean(new Blibby(mbs, null), on1);
        try {
            System.out.println("Registering Blibby #2 with same name: " + on1);
            mbs.registerMBean(new Blibby(mbs, on1), on1);
        } catch (InstanceAlreadyExistsException x) {
            System.out.println("Received expected exception: " + x);
        }
        if (mbs.isRegistered(on1)) {
            try {
                mbs.unregisterMBean(on1);
                if (failure == null)
                    fail(on1+" should have been unregistered");
            } catch (InstanceNotFoundException x) {
                System.out.println(on1+" was unregistered by mbean thread.");
            }
        }  else {
            System.out.println(on1+" was correctly unregistered.");
        }
        if (failure == previous)
            System.out.println("\n****  TEST #1 PASSED ****\n");
        previous = failure;
        System.out.println("\n****  TEST #2 ****\n");
        System.out.println("Registering Blibby #1 with name: " + on1);
        mbs.registerMBean(new Blibby(mbs, null), on1);
        System.out.println("Registering Blibby #2 with other name: " + on2);
        mbs.registerMBean(new Blibby(mbs, on1), on2);
        if (mbs.isRegistered(on1)) {
            try {
                mbs.unregisterMBean(on1);
                if (failure == null)
                    fail(on1+" should have been unregistered");
            } catch (InstanceNotFoundException x) {
                System.out.println(on1+" was unregistered by mbean thread.");
            }
        }  else {
            System.out.println(on1+" was correctly unregistered.");
        }
        System.out.println("unregistering "+on2);
        mbs.unregisterMBean(on2);
        if (failure == previous)
            System.out.println("\n****  TEST #2 PASSED ****\n");
        previous = failure;
        if (failure == null)
            System.out.println("OK: Test passed");
        else
            throw new Exception("TEST FAILED: " + failure);
    }
    private static void fail(String why) {
        System.out.println("FAILED: " + why);
        failure = (failure == null)?why:(failure+",\n"+why);
    }
    private static volatile String failure;
}
