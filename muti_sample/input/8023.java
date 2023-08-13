public class MBeanServerForwarderInvocationHandler
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
        if (methodName.equals("getAttribute") && exception != null) {
            throw exception;
        }
        return method.invoke(mbs, args);
    }
    public void setGetAttributeException(Exception exception) {
        this.exception = exception;
    }
    private Exception exception;
    private MBeanServer mbs;
}
