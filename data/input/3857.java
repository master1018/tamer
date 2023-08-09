public class MXBeanBehavior {
    public static void main(String[] args) throws Exception {
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        Set<ObjectName> names = mbs.queryNames(new ObjectName("java.*:*"),
                                               null);
        names.addAll(mbs.queryNames(new ObjectName("com.sun.management*:*"),
                                    null));
        for (ObjectName name : names)
            test(mbs, name);
        ObjectName refName = new ObjectName("d:type=CompilationRef");
        mbs.registerMBean(new CompilationImpl(), refName);
        CompilationRefMXBean refProxy =
            JMX.newMXBeanProxy(mbs, refName, CompilationRefMXBean.class);
        refProxy.getCompilationMXBean();
        refProxy.setCompilationMXBean(ManagementFactory.getCompilationMXBean());
        ObjectName on =
            (ObjectName) mbs.getAttribute(refName, "CompilationMXBean");
        checkEqual(on, new ObjectName(ManagementFactory.COMPILATION_MXBEAN_NAME),
                   "Referenced object name");
        mbs.setAttribute(refName, new Attribute("CompilationMXBean", on));
        System.out.println("TEST PASSED");
    }
    private static void test(MBeanServer mbs, ObjectName name) throws Exception {
        System.out.println("Testing: " + name);
        MBeanInfo mbi = mbs.getMBeanInfo(name);
        Descriptor mbid = mbi.getDescriptor();
        Object[] values = mbid.getFieldValues("immutableInfo",
                                              "interfaceClassName",
                                              "mxbean");
        checkEqual(values[0], "true", name + " immutableInfo field");
        checkEqual(values[2], "true", name + " mxbean field");
        String interfaceClassName = (String) values[1];
        if (!mbs.isInstanceOf(name, interfaceClassName)) {
            throw new RuntimeException(name + " not instance of " +
                                       interfaceClassName);
        }
        Class interfaceClass = Class.forName(interfaceClassName);
        for (MBeanAttributeInfo mbai : mbi.getAttributes()) {
            Descriptor mbaid = mbai.getDescriptor();
            Object[] avalues = mbaid.getFieldValues("openType",
                                                    "originalType");
            if (avalues[0] == null || avalues[1] == null) {
                throw new RuntimeException("Null attribute descriptor fields: " +
                                           Arrays.toString(avalues));
            }
            if (mbai.isReadable()) {
                String mname = (mbai.isIs() ? "is" : "get") + mbai.getName();
                Method m = interfaceClass.getMethod(mname);
                Type t = m.getGenericReturnType();
                String ret =
                    (t instanceof Class) ? ((Class) t).getName() : t.toString();
                if (!ret.equals(avalues[1])) {
                    final String msg =
                        name + " attribute " + mbai.getName() + " has wrong " +
                        "originalType: " + avalues[1] + " vs " + ret;
                    throw new RuntimeException(msg);
                }
            }
        }
    }
    private static void checkEqual(Object x, Object y, String what) {
        final boolean eq;
        if (x == y)
            eq = true;
        else if (x == null)
            eq = false;
        else
            eq = x.equals(y);
        if (!eq)
            throw new RuntimeException(what + " should be " + y + ", is " + x);
    }
    public static interface CompilationRefMXBean {
        public CompilationMXBean getCompilationMXBean();
        public void setCompilationMXBean(CompilationMXBean mxb);
    }
    public static class CompilationImpl implements CompilationRefMXBean {
        public CompilationMXBean getCompilationMXBean() {
            return ManagementFactory.getCompilationMXBean();
        }
        public void setCompilationMXBean(CompilationMXBean mxb) {
            if (mxb == ManagementFactory.getCompilationMXBean())
                return;
            MBeanServerInvocationHandler mbsih = (MBeanServerInvocationHandler)
                Proxy.getInvocationHandler(mxb);
            ObjectName expectedName;
            try {
                expectedName =
                    new ObjectName(ManagementFactory.COMPILATION_MXBEAN_NAME);
            } catch (MalformedObjectNameException e) {
                throw new RuntimeException(e);
            }
            checkEqual(mbsih.getObjectName(), expectedName,
                       "Proxy name in setCompilationMXBean");
        }
    }
}
