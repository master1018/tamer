public class MethodHandleProxies {
    private MethodHandleProxies() { }  
    public static
    <T> T asInterfaceInstance(final Class<T> intfc, final MethodHandle target) {
        final Method sm = getSingleMethod(intfc);
        if (sm == null)
            throw new IllegalArgumentException("not a single-method interface: "+intfc.getName());
        MethodType smMT = MethodType.methodType(sm.getReturnType(), sm.getParameterTypes());
        MethodHandle checkTarget = target.asType(smMT);  
        checkTarget = checkTarget.asType(checkTarget.type().changeReturnType(Object.class));
        final MethodHandle vaTarget = checkTarget.asSpreader(Object[].class, smMT.parameterCount());
        return intfc.cast(Proxy.newProxyInstance(
                intfc.getClassLoader(),
                new Class[]{ intfc, WrapperInstance.class },
                new InvocationHandler() {
                    private Object getArg(String name) {
                        if ((Object)name == "getWrapperInstanceTarget")  return target;
                        if ((Object)name == "getWrapperInstanceType")    return intfc;
                        throw new AssertionError();
                    }
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        if (method.getDeclaringClass() == WrapperInstance.class)
                            return getArg(method.getName());
                        if (method.equals(sm))
                            return vaTarget.invokeExact(args);
                        if (isObjectMethod(method))
                            return callObjectMethod(this, method, args);
                        throw new InternalError();
                    }
                }));
    }
    public static
    boolean isWrapperInstance(Object x) {
        return x instanceof WrapperInstance;
    }
    private static WrapperInstance asWrapperInstance(Object x) {
        try {
            if (x != null)
                return (WrapperInstance) x;
        } catch (ClassCastException ex) {
        }
        throw new IllegalArgumentException("not a wrapper instance");
    }
    public static
    MethodHandle wrapperInstanceTarget(Object x) {
        return asWrapperInstance(x).getWrapperInstanceTarget();
    }
    public static
    Class<?> wrapperInstanceType(Object x) {
        return asWrapperInstance(x).getWrapperInstanceType();
    }
    private static
    boolean isObjectMethod(Method m) {
        switch (m.getName()) {
        case "toString":
            return (m.getReturnType() == String.class
                    && m.getParameterTypes().length == 0);
        case "hashCode":
            return (m.getReturnType() == int.class
                    && m.getParameterTypes().length == 0);
        case "equals":
            return (m.getReturnType() == boolean.class
                    && m.getParameterTypes().length == 1
                    && m.getParameterTypes()[0] == Object.class);
        }
        return false;
    }
    private static
    Object callObjectMethod(Object self, Method m, Object[] args) {
        assert(isObjectMethod(m)) : m;
        switch (m.getName()) {
        case "toString":
            return self.getClass().getName() + "@" + Integer.toHexString(self.hashCode());
        case "hashCode":
            return System.identityHashCode(self);
        case "equals":
            return (self == args[0]);
        }
        return null;
    }
    private static
    Method getSingleMethod(Class<?> intfc) {
        if (!intfc.isInterface())  return null;
        Method sm = null;
        for (Method m : intfc.getMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isAbstract(mod)) {
                if (sm != null && !isObjectMethod(sm))
                    return null;  
                sm = m;
            }
        }
        return sm;
    }
}
