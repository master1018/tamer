public class RandomMXBeanTest {
    public static interface StupidMXBean {
        public int ZERO = Integer.parseInt("0");
        public int getZero();
        public int identity(int x);
    }
    public static class StupidImpl implements StupidMXBean {
        public int getZero() {
            return 0;
        }
        public int identity(int x) {
            return x;
        }
    }
    public static interface ReferMXBean {
        public StupidMXBean getStupid();
    }
    public static class ReferImpl implements ReferMXBean {
        private final StupidMXBean stupid;
        ReferImpl(StupidMXBean stupid) {
            this.stupid = stupid;
        }
        public StupidMXBean getStupid() {
            return stupid;
        }
    }
    private static class WrapInvocationHandler implements InvocationHandler {
        private final Object wrapped;
        WrapInvocationHandler(Object wrapped) {
            this.wrapped = wrapped;
        }
        public Object invoke(Object proxy, Method method, Object[] args)
        throws Throwable {
            return method.invoke(wrapped, args);
        }
    }
    private static class DullInvocationHandler implements InvocationHandler {
        private static Map<Class<?>, Object> zeroMap =
                new HashMap<Class<?>, Object>();
        static {
            zeroMap.put(byte.class, (byte) 0);
            zeroMap.put(int.class, 0);
            zeroMap.put(short.class, (short) 0);
            zeroMap.put(long.class, 0L);
            zeroMap.put(float.class, 0F);
            zeroMap.put(double.class, 0.0);
            zeroMap.put(boolean.class, false);
            zeroMap.put(char.class, '\0');
        }
        public static Object zeroFor(Class<?> c) {
            if (c.isPrimitive())
                return zeroMap.get(c);
            else
                return null;
        }
        public Object invoke(Object proxy, Method method, Object[] args)
        throws Throwable {
            Class<?> retType = method.getReturnType();
            if (!retType.isPrimitive())
                return null;
            return zeroMap.get(retType);
        }
    }
    public static void main(String[] args) throws Exception {
        MBeanServer mbs = MBeanServerFactory.newMBeanServer();
        ObjectName name = new ObjectName("a:b=c");
        StupidMXBean stupid = new StupidImpl();
        mbs.registerMBean(stupid, name);
        ObjectName referName = new ObjectName("a:c=d");
        mbs.registerMBean(new ReferImpl(stupid), referName);
        System.out.println(mbs.getMBeanInfo(name));
        StupidMXBean stupid2 = (StupidMXBean)
                Proxy.newProxyInstance(StupidMXBean.class.getClassLoader(),
                    new Class<?>[] {StupidMXBean.class},
                    new WrapInvocationHandler(stupid));
        ObjectName stupidName2 = new ObjectName("a:d=e");
        mbs.registerMBean(stupid2, stupidName2);
        Field zero = StupidMXBean.class.getField("ZERO");
        System.out.println("Zero field = " + zero.get(null));
        test(mbs, MerlinMXBean.class);
        test(mbs, TigerMXBean.class);
        StupidMXBean proxy = JMX.newMXBeanProxy(mbs, name, StupidMXBean.class);
        System.out.println("Zero = " + proxy.getZero());
        System.out.println("One = " + proxy.identity(1));
        ReferMXBean referProxy =
                JMX.newMXBeanProxy(mbs, referName, ReferMXBean.class);
        StupidMXBean stupidProxy2 = referProxy.getStupid();
        System.out.println("Same proxy: " + (proxy == stupidProxy2));
        Method[] methods = StupidMXBean.class.getMethods();
        for (Method method : methods) {
            if (method.getParameterTypes().length == 0)
                method.invoke(proxy, new Object[0]);
        }
    }
    private static <T> void test(MBeanServer mbs, Class<T> c) throws Exception {
        System.out.println("Testing " + c.getName());
        T merlin = c.cast(
            Proxy.newProxyInstance(c.getClassLoader(),
                new Class<?>[] {c},
                new DullInvocationHandler()));
        ObjectName merlinName = new ObjectName("a:type=" + c.getName());
        mbs.registerMBean(merlin, merlinName);
        System.out.println(mbs.getMBeanInfo(merlinName));
        T merlinProxy = JMX.newMXBeanProxy(mbs, merlinName, c);
        Method[] merlinMethods = c.getMethods();
        for (Method m : merlinMethods) {
            Class<?>[] types = m.getParameterTypes();
            Object[] params = new Object[types.length];
            for (int i = 0; i < types.length; i++)
                params[i] = DullInvocationHandler.zeroFor(types[i]);
            System.out.println("Invoking " + m.getName());
            m.invoke(merlinProxy, (Object[]) params);
        }
    }
}
