public class InterfaceImplementor {
    private Invocable engine;
    public InterfaceImplementor(Invocable engine) {
        this.engine = engine;
    }
    private final class InterfaceImplementorInvocationHandler
                        implements InvocationHandler {
        private Object thiz;
        private AccessControlContext accCtxt;
        public InterfaceImplementorInvocationHandler(Object thiz,
            AccessControlContext accCtxt) {
            this.thiz = thiz;
            this.accCtxt = accCtxt;
        }
        public Object invoke(Object proxy , Method method, Object[] args)
        throws java.lang.Throwable {
            args = convertArguments(method, args);
            Object result;
            final Method m = method;
            final Object[] a = args;
            result = AccessController.doPrivileged(new PrivilegedExceptionAction<Object>() {
                public Object run() throws Exception {
                    if (thiz == null) {
                        return engine.invokeFunction(m.getName(), a);
                    } else {
                        return engine.invokeMethod(thiz, m.getName(), a);
                    }
                }
            }, accCtxt);
            return convertResult(method, result);
        }
    }
    public <T> T getInterface(Object thiz, Class<T> iface)
    throws ScriptException {
        if (iface == null || !iface.isInterface()) {
            throw new IllegalArgumentException("interface Class expected");
        }
        if (! isImplemented(thiz, iface)) {
            return null;
        }
        AccessControlContext accCtxt = AccessController.getContext();
        return iface.cast(Proxy.newProxyInstance(iface.getClassLoader(),
            new Class[]{iface},
            new InterfaceImplementorInvocationHandler(thiz, accCtxt)));
    }
    protected boolean isImplemented(Object thiz, Class<?> iface) {
        return true;
    }
    protected Object convertResult(Method method, Object res)
                                   throws ScriptException {
        return res;
    }
    protected Object[] convertArguments(Method method, Object[] args)
                                      throws ScriptException {
        return args;
    }
}
