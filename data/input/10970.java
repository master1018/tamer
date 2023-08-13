public class MBeanServerInvocationHandler implements InvocationHandler {
    public MBeanServerInvocationHandler(MBeanServerConnection connection,
                                        ObjectName objectName) {
        this(connection, objectName, false);
    }
    public MBeanServerInvocationHandler(MBeanServerConnection connection,
                                        ObjectName objectName,
                                        boolean isMXBean) {
        if (connection == null) {
            throw new IllegalArgumentException("Null connection");
        }
        if (objectName == null) {
            throw new IllegalArgumentException("Null object name");
        }
        this.connection = connection;
        this.objectName = objectName;
        this.isMXBean = isMXBean;
    }
    public MBeanServerConnection getMBeanServerConnection() {
        return connection;
    }
    public ObjectName getObjectName() {
        return objectName;
    }
    public boolean isMXBean() {
        return isMXBean;
    }
    public static <T> T newProxyInstance(MBeanServerConnection connection,
                                         ObjectName objectName,
                                         Class<T> interfaceClass,
                                         boolean notificationBroadcaster) {
        final InvocationHandler handler =
            new MBeanServerInvocationHandler(connection, objectName);
        final Class<?>[] interfaces;
        if (notificationBroadcaster) {
            interfaces =
                new Class<?>[] {interfaceClass, NotificationEmitter.class};
        } else
            interfaces = new Class<?>[] {interfaceClass};
        Object proxy =
            Proxy.newProxyInstance(interfaceClass.getClassLoader(),
                                   interfaces,
                                   handler);
        return interfaceClass.cast(proxy);
    }
    public Object invoke(Object proxy, Method method, Object[] args)
            throws Throwable {
        final Class<?> methodClass = method.getDeclaringClass();
        if (methodClass.equals(NotificationBroadcaster.class)
            || methodClass.equals(NotificationEmitter.class))
            return invokeBroadcasterMethod(proxy, method, args);
        if (shouldDoLocally(proxy, method))
            return doLocally(proxy, method, args);
        try {
            if (isMXBean()) {
                MXBeanProxy p = findMXBeanProxy(methodClass);
                return p.invoke(connection, objectName, method, args);
            } else {
                final String methodName = method.getName();
                final Class<?>[] paramTypes = method.getParameterTypes();
                final Class<?> returnType = method.getReturnType();
                final int nargs = (args == null) ? 0 : args.length;
                if (methodName.startsWith("get")
                    && methodName.length() > 3
                    && nargs == 0
                    && !returnType.equals(Void.TYPE)) {
                    return connection.getAttribute(objectName,
                        methodName.substring(3));
                }
                if (methodName.startsWith("is")
                    && methodName.length() > 2
                    && nargs == 0
                    && (returnType.equals(Boolean.TYPE)
                    || returnType.equals(Boolean.class))) {
                    return connection.getAttribute(objectName,
                        methodName.substring(2));
                }
                if (methodName.startsWith("set")
                    && methodName.length() > 3
                    && nargs == 1
                    && returnType.equals(Void.TYPE)) {
                    Attribute attr = new Attribute(methodName.substring(3), args[0]);
                    connection.setAttribute(objectName, attr);
                    return null;
                }
                final String[] signature = new String[paramTypes.length];
                for (int i = 0; i < paramTypes.length; i++)
                    signature[i] = paramTypes[i].getName();
                return connection.invoke(objectName, methodName,
                                         args, signature);
            }
        } catch (MBeanException e) {
            throw e.getTargetException();
        } catch (RuntimeMBeanException re) {
            throw re.getTargetException();
        } catch (RuntimeErrorException rre) {
            throw rre.getTargetError();
        }
    }
    private static MXBeanProxy findMXBeanProxy(Class<?> mxbeanInterface) {
        synchronized (mxbeanProxies) {
            WeakReference<MXBeanProxy> proxyRef =
                    mxbeanProxies.get(mxbeanInterface);
            MXBeanProxy p = (proxyRef == null) ? null : proxyRef.get();
            if (p == null) {
                try {
                    p = new MXBeanProxy(mxbeanInterface);
                } catch (IllegalArgumentException e) {
                    String msg = "Cannot make MXBean proxy for " +
                            mxbeanInterface.getName() + ": " + e.getMessage();
                    IllegalArgumentException iae =
                            new IllegalArgumentException(msg, e.getCause());
                    iae.setStackTrace(e.getStackTrace());
                    throw iae;
                }
                mxbeanProxies.put(mxbeanInterface,
                                  new WeakReference<MXBeanProxy>(p));
            }
            return p;
        }
    }
    private static final WeakHashMap<Class<?>, WeakReference<MXBeanProxy>>
            mxbeanProxies = new WeakHashMap<Class<?>, WeakReference<MXBeanProxy>>();
    private Object invokeBroadcasterMethod(Object proxy, Method method,
                                           Object[] args) throws Exception {
        final String methodName = method.getName();
        final int nargs = (args == null) ? 0 : args.length;
        if (methodName.equals("addNotificationListener")) {
            if (nargs != 3) {
                final String msg =
                    "Bad arg count to addNotificationListener: " + nargs;
                throw new IllegalArgumentException(msg);
            }
            NotificationListener listener = (NotificationListener) args[0];
            NotificationFilter filter = (NotificationFilter) args[1];
            Object handback = args[2];
            connection.addNotificationListener(objectName,
                                               listener,
                                               filter,
                                               handback);
            return null;
        } else if (methodName.equals("removeNotificationListener")) {
            NotificationListener listener = (NotificationListener) args[0];
            switch (nargs) {
            case 1:
                connection.removeNotificationListener(objectName, listener);
                return null;
            case 3:
                NotificationFilter filter = (NotificationFilter) args[1];
                Object handback = args[2];
                connection.removeNotificationListener(objectName,
                                                      listener,
                                                      filter,
                                                      handback);
                return null;
            default:
                final String msg =
                    "Bad arg count to removeNotificationListener: " + nargs;
                throw new IllegalArgumentException(msg);
            }
        } else if (methodName.equals("getNotificationInfo")) {
            if (args != null) {
                throw new IllegalArgumentException("getNotificationInfo has " +
                                                   "args");
            }
            MBeanInfo info = connection.getMBeanInfo(objectName);
            return info.getNotifications();
        } else {
            throw new IllegalArgumentException("Bad method name: " +
                                               methodName);
        }
    }
    private boolean shouldDoLocally(Object proxy, Method method) {
        final String methodName = method.getName();
        if ((methodName.equals("hashCode") || methodName.equals("toString"))
            && method.getParameterTypes().length == 0
            && isLocal(proxy, method))
            return true;
        if (methodName.equals("equals")
            && Arrays.equals(method.getParameterTypes(),
                             new Class<?>[] {Object.class})
            && isLocal(proxy, method))
            return true;
        return false;
    }
    private Object doLocally(Object proxy, Method method, Object[] args) {
        final String methodName = method.getName();
        if (methodName.equals("equals")) {
            if (this == args[0]) {
                return true;
            }
            if (!(args[0] instanceof Proxy)) {
                return false;
            }
            final InvocationHandler ihandler =
                Proxy.getInvocationHandler(args[0]);
            if (ihandler == null ||
                !(ihandler instanceof MBeanServerInvocationHandler)) {
                return false;
            }
            final MBeanServerInvocationHandler handler =
                (MBeanServerInvocationHandler)ihandler;
            return connection.equals(handler.connection) &&
                objectName.equals(handler.objectName) &&
                proxy.getClass().equals(args[0].getClass());
        } else if (methodName.equals("toString")) {
            return (isMXBean() ? "MX" : "M") + "BeanProxy(" +
                connection + "[" + objectName + "])";
        } else if (methodName.equals("hashCode")) {
            return objectName.hashCode()+connection.hashCode();
        }
        throw new RuntimeException("Unexpected method name: " + methodName);
    }
    private static boolean isLocal(Object proxy, Method method) {
        final Class<?>[] interfaces = proxy.getClass().getInterfaces();
        if(interfaces == null) {
            return true;
        }
        final String methodName = method.getName();
        final Class<?>[] params = method.getParameterTypes();
        for (Class<?> intf : interfaces) {
            try {
                intf.getMethod(methodName, params);
                return false; 
            } catch (NoSuchMethodException nsme) {
            }
        }
        return true;  
    }
    private final MBeanServerConnection connection;
    private final ObjectName objectName;
    private final boolean isMXBean;
}
