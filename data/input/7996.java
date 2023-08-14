public class PreRegisterTest {
    static final ObjectName oldName, newName;
    static {
        try {
            oldName = new ObjectName("a:type=old");
            newName = new ObjectName("a:type=new");
        } catch (MalformedObjectNameException e) {
            e.printStackTrace();
            throw new Error();
        }
    }
    public static class X implements XMBean, MBeanRegistration {
        public ObjectName preRegister(MBeanServer mbs, ObjectName name) {
            return newName;
        }
        public void postRegister(Boolean done) {}
        public void preDeregister() {}
        public void postDeregister() {}
    }
    public static interface XMBean {
    }
    public static void main(String[] args) throws Exception {
        System.out.println("Testing preRegister ObjectName substitution");
        MBeanServer mbs = MBeanServerFactory.newMBeanServer();
        mbs.createMBean(X.class.getName(), oldName);
        Set names = mbs.queryNames(null, null);
        System.out.println("MBean names after createMBean: " + names);
        boolean ok = true;
        if (names.contains(oldName)) {
            ok = false;
            System.out.println("TEST FAILS: previous name was used");
        }
        if (!names.contains(newName)) {
            ok = false;
            System.out.println("TEST FAILS: substitute name was not used");
        }
        if (ok) {
            System.out.println("Test passes: ObjectName correctly " +
                               "substituted");
        } else {
            System.out.println("TEST FAILS: ObjectName not correctly " +
                               "substituted");
            System.exit(1);
        }
    }
}
