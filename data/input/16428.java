public class PreRegisterNameTest {
    public static interface SpumeMXBean {}
    public static class Spume implements SpumeMXBean, MBeanRegistration {
        private ObjectName realName;
        public Spume(ObjectName realName) {
            this.realName = realName;
        }
        public void preDeregister() throws Exception {
        }
        public void postDeregister() {
        }
        public void postRegister(Boolean registrationDone) {
        }
        public ObjectName preRegister(MBeanServer server, ObjectName name) {
            return realName;
        }
    }
    public static interface ThingMBean {
        public boolean getNoddy();
    }
    public static class Thing implements ThingMBean, MBeanRegistration {
        private ObjectName realName;
        public Thing(ObjectName realName) {
            this.realName = realName;
        }
        public ObjectName preRegister(MBeanServer mbs, ObjectName name) {
            return realName;
        }
        public void postRegister(Boolean done) {}
        public void preDeregister() {}
        public void postDeregister() {}
        public boolean getNoddy() {
            return true;
        }
    }
    public static class XThing extends StandardMBean implements ThingMBean {
        private ObjectName realName;
        public XThing(ObjectName realName) {
            super(ThingMBean.class, false);
            this.realName = realName;
        }
        @Override
        public ObjectName preRegister(MBeanServer server, ObjectName name) {
            return realName;
        }
        public boolean getNoddy() {
            return false;
        }
    }
    public static class XSpume extends StandardMBean implements SpumeMXBean {
        private ObjectName realName;
        public XSpume(ObjectName realName) {
            super(SpumeMXBean.class, true);
            this.realName = realName;
        }
        @Override
        public ObjectName preRegister(MBeanServer server, ObjectName name)
        throws Exception {
            super.preRegister(server, realName);
            return realName;
        }
    }
    public static void main(String[] args) throws Exception {
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        for (Class<?> c : new Class<?>[] {
                Spume.class, Thing.class, XSpume.class, XThing.class
             }) {
            for (ObjectName n : new ObjectName[] {null, new ObjectName("a:b=c")}) {
                System.out.println("Class " + c.getName() + " with name " + n +
                        "...");
                ObjectName realName = new ObjectName("a:type=" + c.getName());
                Constructor<?> constr = c.getConstructor(ObjectName.class);
                Object mbean = constr.newInstance(realName);
                ObjectInstance oi;
                String what =
                    "Registering MBean of type " + c.getName() + " under name " +
                    "<" + n + ">: ";
                try {
                    oi = mbs.registerMBean(mbean, n);
                } catch (Exception e) {
                    e.printStackTrace();
                    fail(what + " got " + e);
                    continue;
                }
                ObjectName registeredName = oi.getObjectName();
                if (!registeredName.equals(realName))
                    fail(what + " registered as " + registeredName);
                if (!mbs.isRegistered(realName)) {
                    fail(what + " not registered as expected");
                }
                mbs.unregisterMBean(registeredName);
            }
        }
        System.err.flush();
        if (failures == 0)
            System.out.println("TEST PASSED");
        else
            throw new Exception("TEST FAILED: " + failure);
    }
    private static void fail(String msg) {
        System.err.println("FAILED: " + msg);
        failure = msg;
        failures++;
    }
    private static int failures;
    private static String failure;
}
