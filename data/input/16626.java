public class MBeanServerMXBeanUnsupportedTest {
    public static class MBeanServerBuilderImpl extends MBeanServerBuilder {
        private final MBeanServerBuilder inner;
        public MBeanServerBuilderImpl() {
            inner = new MBeanServerBuilder();
        }
        public MBeanServer newMBeanServer(
                String defaultDomain,
                MBeanServer outer,
                MBeanServerDelegate delegate) {
            final MBeanServerForwarder mbsf =
                    MBeanServerForwarderInvocationHandler.newProxyInstance();
            final MBeanServer innerMBeanServer =
                    inner.newMBeanServer(defaultDomain,
                    (outer == null ? mbsf : outer),
                    delegate);
            mbsf.setMBeanServer(innerMBeanServer);
            return mbsf;
        }
    }
    public static class MBeanServerForwarderInvocationHandler
            implements InvocationHandler {
        public static MBeanServerForwarder newProxyInstance() {
            final InvocationHandler handler =
                    new MBeanServerForwarderInvocationHandler();
            final Class[] interfaces =
                    new Class[] {MBeanServerForwarder.class};
            Object proxy = Proxy.newProxyInstance(
                    MBeanServerForwarder.class.getClassLoader(),
                    interfaces,
                    handler);
            return MBeanServerForwarder.class.cast(proxy);
        }
        public Object invoke(Object proxy, Method method, Object[] args)
                throws Throwable {
            final String methodName = method.getName();
            if (methodName.equals("getMBeanServer")) {
                return mbs;
            }
            if (methodName.equals("setMBeanServer")) {
                if (args[0] == null)
                    throw new IllegalArgumentException("Null MBeanServer");
                if (mbs != null)
                    throw new IllegalArgumentException("MBeanServer object " +
                            "already initialized");
                mbs = (MBeanServer) args[0];
                return null;
            }
            if (methodName.equals("registerMBean")) {
                Object mbean = args[0];
                ObjectName name = (ObjectName) args[1];
                String domain = name.getDomain();
                System.out.println("registerMBean: class=" +
                        mbean.getClass().getName() + "\tname=" + name);
                Object result = method.invoke(mbs, args);
                if (domain.equals("java.lang") ||
                    domain.equals("java.util.logging") ||
                    domain.equals("com.sun.management")) {
                    String mxbean = (String)
                        mbs.getMBeanInfo(name).getDescriptor().getFieldValue("mxbean");
                    if (mxbean == null || !mxbean.equals("true")) {
                        throw new RuntimeException(
                                "Platform MBeans must be MXBeans!");
                    }
                    if (!(mbean instanceof StandardMBean)) {
                        throw new RuntimeException(
                                "MXBeans must be wrapped in StandardMBean!");
                    }
                }
                return result;
            }
            return method.invoke(mbs, args);
        }
        private MBeanServer mbs;
    }
    public static void main(String args[]) throws Exception {
        System.setProperty("javax.management.builder.initial",
                MBeanServerBuilderImpl.class.getName());
        try {
            ManagementFactory.getPlatformMBeanServer();
        } catch (RuntimeException e) {
            System.out.println(">>> Unhappy Bye, Bye!");
            throw e;
        }
        System.out.println(">>> Happy Bye, Bye!");
    }
}
