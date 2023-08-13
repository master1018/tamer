public class JMX {
    static final JMX proof = new JMX();
    private JMX() {}
    public static final String DEFAULT_VALUE_FIELD = "defaultValue";
    public static final String IMMUTABLE_INFO_FIELD = "immutableInfo";
    public static final String INTERFACE_CLASS_NAME_FIELD = "interfaceClassName";
    public static final String LEGAL_VALUES_FIELD = "legalValues";
    public static final String MAX_VALUE_FIELD = "maxValue";
    public static final String MIN_VALUE_FIELD = "minValue";
    public static final String MXBEAN_FIELD = "mxbean";
    public static final String OPEN_TYPE_FIELD = "openType";
    public static final String ORIGINAL_TYPE_FIELD = "originalType";
    public static <T> T newMBeanProxy(MBeanServerConnection connection,
                                      ObjectName objectName,
                                      Class<T> interfaceClass) {
        return newMBeanProxy(connection, objectName, interfaceClass, false);
    }
    public static <T> T newMBeanProxy(MBeanServerConnection connection,
                                      ObjectName objectName,
                                      Class<T> interfaceClass,
                                      boolean notificationEmitter) {
        return MBeanServerInvocationHandler.newProxyInstance(
                connection,
                objectName,
                interfaceClass,
                notificationEmitter);
    }
    public static <T> T newMXBeanProxy(MBeanServerConnection connection,
                                       ObjectName objectName,
                                       Class<T> interfaceClass) {
        return newMXBeanProxy(connection, objectName, interfaceClass, false);
    }
    public static <T> T newMXBeanProxy(MBeanServerConnection connection,
                                       ObjectName objectName,
                                       Class<T> interfaceClass,
                                       boolean notificationEmitter) {
        try {
            Introspector.testComplianceMXBeanInterface(interfaceClass);
        } catch (NotCompliantMBeanException e) {
            throw new IllegalArgumentException(e);
        }
        InvocationHandler handler = new MBeanServerInvocationHandler(
                connection, objectName, true);
        final Class<?>[] interfaces;
        if (notificationEmitter) {
            interfaces =
                new Class<?>[] {interfaceClass, NotificationEmitter.class};
        } else
            interfaces = new Class<?>[] {interfaceClass};
        Object proxy = Proxy.newProxyInstance(
                interfaceClass.getClassLoader(),
                interfaces,
                handler);
        return interfaceClass.cast(proxy);
    }
    public static boolean isMXBeanInterface(Class<?> interfaceClass) {
        if (!interfaceClass.isInterface())
            return false;
        MXBean a = interfaceClass.getAnnotation(MXBean.class);
        if (a != null)
            return a.value();
        return interfaceClass.getName().endsWith("MXBean");
    }
}
