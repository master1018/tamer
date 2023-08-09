public class GenericTypeTest {
    public static class Gen<T> {
        public T getThing() {
            return null;
        }
    }
    public static interface GenTMXBean {
        public <T> Gen<T> getFoo();
    }
    public static interface GenStringMXBean {
        public Gen<String> getFoo();
    }
    public static class GenMethod {
        public <T> Gen<T> getWhatever() {
            return null;
        }
    }
    public static interface GenMethodMXBean {
        public GenMethod getFoo();
    }
    public static interface TypeVarMXBean {
        public <T> List<T> getFoo();
    }
    private static final Class<?>[] illegalMXBeans = {
        GenTMXBean.class, GenStringMXBean.class, GenMethodMXBean.class,
        TypeVarMXBean.class,
    };
    public static class ConcreteOverride extends Gen<String> {
        @Override
        public String getThing() {
            return super.getThing();
        }
    }
    public static interface ConcreteOverrideMXBean {
        public ConcreteOverride getFoo();
    }
    private static final Class<?>[] legalMXBeans = {
         ConcreteOverrideMXBean.class,
    };
    private static class NullInvocationHandler implements InvocationHandler {
        public Object invoke(Object proxy, Method method, Object[] args)
        throws Throwable {
            return null;
        }
    }
    private static final InvocationHandler nullInvocationHandler =
            new NullInvocationHandler();
    private static <T> T makeNullMXBean(Class<T> intf) throws Exception {
        return intf.cast(Proxy.newProxyInstance(intf.getClassLoader(),
                new Class<?>[] {intf},
                nullInvocationHandler));
    }
    private static String failure;
    public static void main(String[] args) throws Exception {
        MBeanServer mbs = MBeanServerFactory.newMBeanServer();
        for (boolean legal : new boolean[] {false, true}) {
            Class<?>[] intfs = legal ? legalMXBeans : illegalMXBeans;
            for (Class<?> intf : intfs) {
                String intfName = intf.getName();
                System.out.println("Testing " + intfName);
                Object mxbean = makeNullMXBean(intf);
                ObjectName name = new ObjectName("test:type=" + intfName);
                try {
                    mbs.registerMBean(mxbean, name);
                    if (!legal)
                        fail("Registering " + intfName + " should not succeed");
                } catch (NotCompliantMBeanException e) {
                    if (legal)
                        failX("Registering " + intfName + " should succeed", e);
                } catch (Exception e) {
                    if (legal)
                        failX("Registering " + intfName + " should succeed", e);
                    else {
                        failX("Registering " + intfName + " should produce " +
                                "NotCompliantMBeanException", e);
                    }
                }
            }
        }
        if (failure != null)
            throw new Exception("TEST FAILED: " + failure);
        System.out.println("Test passed");
    }
    private static void fail(String msg) {
        System.out.println("FAILED: " + msg);
        failure = msg;
    }
    private static void failX(String msg, Throwable x) {
        fail(msg + ": " + x);
        x.printStackTrace(System.out);
    }
}
