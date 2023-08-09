public class RemoteObjectInvocationHandler
    extends RemoteObject
    implements InvocationHandler
{
    private static final long serialVersionUID = 2L;
    private static final MethodToHash_Maps methodToHash_Maps =
        new MethodToHash_Maps();
    public RemoteObjectInvocationHandler(RemoteRef ref) {
        super(ref);
        if (ref == null) {
            throw new NullPointerException();
        }
    }
    public Object invoke(Object proxy, Method method, Object[] args)
        throws Throwable
    {
        if (method.getDeclaringClass() == Object.class) {
            return invokeObjectMethod(proxy, method, args);
        } else {
            return invokeRemoteMethod(proxy, method, args);
        }
    }
    private Object invokeObjectMethod(Object proxy,
                                      Method method,
                                      Object[] args)
    {
        String name = method.getName();
        if (name.equals("hashCode")) {
            return hashCode();
        } else if (name.equals("equals")) {
            Object obj = args[0];
            return
                proxy == obj ||
                (obj != null &&
                 Proxy.isProxyClass(obj.getClass()) &&
                 equals(Proxy.getInvocationHandler(obj)));
        } else if (name.equals("toString")) {
            return proxyToString(proxy);
        } else {
            throw new IllegalArgumentException(
                "unexpected Object method: " + method);
        }
    }
    private Object invokeRemoteMethod(Object proxy,
                                      Method method,
                                      Object[] args)
        throws Exception
    {
        try {
            if (!(proxy instanceof Remote)) {
                throw new IllegalArgumentException(
                    "proxy not Remote instance");
            }
            return ref.invoke((Remote) proxy, method, args,
                              getMethodHash(method));
        } catch (Exception e) {
            if (!(e instanceof RuntimeException)) {
                Class<?> cl = proxy.getClass();
                try {
                    method = cl.getMethod(method.getName(),
                                          method.getParameterTypes());
                } catch (NoSuchMethodException nsme) {
                    throw (IllegalArgumentException)
                        new IllegalArgumentException().initCause(nsme);
                }
                Class<?> thrownType = e.getClass();
                for (Class<?> declaredType : method.getExceptionTypes()) {
                    if (declaredType.isAssignableFrom(thrownType)) {
                        throw e;
                    }
                }
                e = new UnexpectedException("unexpected exception", e);
            }
            throw e;
        }
    }
    private String proxyToString(Object proxy) {
        Class<?>[] interfaces = proxy.getClass().getInterfaces();
        if (interfaces.length == 0) {
            return "Proxy[" + this + "]";
        }
        String iface = interfaces[0].getName();
        if (iface.equals("java.rmi.Remote") && interfaces.length > 1) {
            iface = interfaces[1].getName();
        }
        int dot = iface.lastIndexOf('.');
        if (dot >= 0) {
            iface = iface.substring(dot + 1);
        }
        return "Proxy[" + iface + "," + this + "]";
    }
    private void readObjectNoData() throws InvalidObjectException {
        throw new InvalidObjectException("no data in stream; class: " +
                                         this.getClass().getName());
    }
    private static long getMethodHash(Method method) {
        return methodToHash_Maps.get(method.getDeclaringClass()).get(method);
    }
    private static class MethodToHash_Maps
        extends WeakClassHashMap<Map<Method,Long>>
    {
        MethodToHash_Maps() {}
        protected Map<Method,Long> computeValue(Class<?> remoteClass) {
            return new WeakHashMap<Method,Long>() {
                public synchronized Long get(Object key) {
                    Long hash = super.get(key);
                    if (hash == null) {
                        Method method = (Method) key;
                        hash = Util.computeMethodHash(method);
                        put(method, hash);
                    }
                    return hash;
                }
            };
        }
    }
}
