public class TestUtils {
    private static final Logger LOG =
            Logger.getLogger(TestUtils.class.getName());
    private TestUtils() {
    }
    public static ObjectName getObjectName(Object proxy) {
        if (!(proxy instanceof Proxy))
            throw new IllegalArgumentException("not a "+Proxy.class.getName());
        final Proxy p = (Proxy) proxy;
        final InvocationHandler handler =
                Proxy.getInvocationHandler(proxy);
        if (handler instanceof MBeanServerInvocationHandler)
            return ((MBeanServerInvocationHandler)handler).getObjectName();
        throw new IllegalArgumentException("not a JMX Proxy");
    }
    public static <T> T makeNotificationEmitter(T proxy,
                        Class<T> mbeanInterface) {
        if (proxy instanceof NotificationEmitter)
            return proxy;
        if (proxy == null) return null;
        if (!(proxy instanceof Proxy))
            throw new IllegalArgumentException("not a "+Proxy.class.getName());
        final Proxy p = (Proxy) proxy;
        final InvocationHandler handler =
                Proxy.getInvocationHandler(proxy);
        if (!(handler instanceof MBeanServerInvocationHandler))
            throw new IllegalArgumentException("not a JMX Proxy");
        final MBeanServerInvocationHandler h =
                (MBeanServerInvocationHandler)handler;
        final ObjectName name = h.getObjectName();
        final MBeanServerConnection mbs = h.getMBeanServerConnection();
        final boolean isMXBean = h.isMXBean();
        final T newProxy;
        if (isMXBean)
            newProxy = JMX.newMXBeanProxy(mbs,name,mbeanInterface,true);
        else
            newProxy = JMX.newMBeanProxy(mbs,name,mbeanInterface,true);
        return newProxy;
    }
}
