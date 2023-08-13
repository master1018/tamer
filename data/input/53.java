public class ExceptionDiagnosisTest {
    private static volatile String failure;
    public static interface BdelloidMXBean {
        public Rotifer getRotifer();
    }
    public static class Bdelloid implements BdelloidMXBean {
        public Rotifer getRotifer() {
            return null;
        }
    }
    public static class Rotifer {
        public File getFile() {
            return null;
        }
    }
    public static interface IndirectHashMapMXBean {
        public HashMapContainer getContainer();
    }
    public static class IndirectHashMap implements IndirectHashMapMXBean {
        public HashMapContainer getContainer() {
            return null;
        }
    }
    public static class HashMapContainer {
        public HashMap<String, String> getHashMap() {return null;}
    }
    public static interface BlimMXBean {
        public BlimContainer getBlimContainer();
    }
    public static class BlimImpl implements BlimMXBean {
        public BlimContainer getBlimContainer() {
            return null;
        }
    }
    public static class BlimContainer {
        public Blim getBlim() {return null;}
        public void setBlim(Blim blim) {}
    }
    public static class Blim {
        public Blam getBlam() {return null;}
        public void setBlam(Blam blam) {}
    }
    public static class Blam {
        public Blam(int x) {}
        public int getX() {return 0;}
    }
    public static interface CaseProbMXBean {
        public CaseProb getCaseProb();
    }
    public static class CaseProbImpl implements CaseProbMXBean {
        public CaseProb getCaseProb() {return null;}
    }
    public static class CaseProb {
        @ConstructorProperties({"urlPath"})
        public CaseProb(String urlPath) {}
        public String getURLPath() {return null;}
    }
    public static void main(String[] args) throws Exception {
        testMXBeans(new Bdelloid(), BdelloidMXBean.class, Rotifer.class, File.class);
        testMXBeans(new IndirectHashMap(),
                IndirectHashMapMXBean.class, HashMapContainer.class,
                HashMapContainer.class.getMethod("getHashMap").getGenericReturnType());
        testProxies(new BlimImpl(), BlimMXBean.class, BlimMXBean.class,
                BlimContainer.class, Blim.class, Blam.class);
        testCaseProb();
        if (failure == null)
            System.out.println("TEST PASSED");
        else
            throw new Exception("TEST FAILED: " + failure);
    }
    private static void testMXBeans(Object mbean, Type... expectedTypes)
            throws Exception {
        try {
            MBeanServer mbs = MBeanServerFactory.newMBeanServer();
            ObjectName name = new ObjectName("a:b=c");
            mbs.registerMBean(mbean, name);
            fail("No exception from registerMBean for " + mbean);
        } catch (NotCompliantMBeanException e) {
            checkExceptionChain("MBean " + mbean, e, expectedTypes);
        }
    }
    private static <T> void testProxies(
            Object mbean, Class<T> mxbeanClass, Type... expectedTypes)
            throws Exception {
        MBeanServer mbs = MBeanServerFactory.newMBeanServer();
        ObjectName name = new ObjectName("a:b=c");
        mbs.registerMBean(mbean, name);
        T proxy = JMX.newMXBeanProxy(mbs, name, mxbeanClass);
        List<Method> methods = new ArrayList<Method>();
        for (Method m : mxbeanClass.getMethods()) {
            if (m.getDeclaringClass() == mxbeanClass)
                methods.add(m);
        }
        if (methods.size() != 1) {
            fail("TEST BUG: expected to find exactly one method in " +
                    mxbeanClass.getName() + ": " + methods);
        }
        Method getter = methods.get(0);
        try {
            try {
                getter.invoke(proxy);
                fail("No exception from proxy method " + getter.getName() +
                        " in " + mxbeanClass.getName());
            } catch (InvocationTargetException e) {
                Throwable cause = e.getCause();
                if (cause instanceof Exception)
                    throw (Exception) cause;
                else
                    throw (Error) cause;
            }
        } catch (IllegalArgumentException e) {
            checkExceptionChain(
                    "Proxy for " + mxbeanClass.getName(), e, expectedTypes);
        }
    }
    private static void testCaseProb() throws Exception {
        MBeanServer mbs = MBeanServerFactory.newMBeanServer();
        ObjectName name = new ObjectName("a:b=c");
        mbs.registerMBean(new CaseProbImpl(), name);
        CaseProbMXBean proxy = JMX.newMXBeanProxy(mbs, name, CaseProbMXBean.class);
        try {
            CaseProb prob = proxy.getCaseProb();
            fail("No exception from proxy method getCaseProb");
        } catch (IllegalArgumentException e) {
            String messageChain = messageChain(e);
            if (messageChain.contains("URLPath")) {
                System.out.println("Message chain contains URLPath as required: "
                        + messageChain);
            } else {
                fail("Exception chain for CaseProb does not mention property" +
                        " URLPath differing only in case");
                System.out.println("Full stack trace:");
                e.printStackTrace(System.out);
            }
        }
    }
    private static void checkExceptionChain(
            String what, Throwable e, Type[] expectedTypes) {
        System.out.println("Exceptions in chain for " + what + ":");
        for (Throwable t = e; t != null; t = t.getCause())
            System.out.println(".." + t);
        String messageChain = messageChain(e);
        for (Type type : expectedTypes) {
            String name = (type instanceof Class) ?
                ((Class<?>) type).getName() : type.toString();
            if (!messageChain.contains(name)) {
                fail("Exception chain for " + what + " does not mention " +
                        name);
                System.out.println("Full stack trace:");
                e.printStackTrace(System.out);
            }
        }
        System.out.println();
    }
    private static String messageChain(Throwable t) {
        String msg = "
        for ( ; t != null; t = t.getCause())
            msg += " " + t.getMessage() + " 
        return msg;
    }
    private static void fail(String why) {
        failure = why;
        System.out.println("FAIL: " + why);
    }
}
