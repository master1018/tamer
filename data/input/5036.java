public abstract class ProviderSkeleton implements InvocationHandler, Provider {
    protected boolean active; 
    protected Class<? extends Provider> providerType; 
    protected HashMap<Method, ProbeSkeleton> probes; 
    protected abstract ProbeSkeleton createProbe(Method method);
    protected ProviderSkeleton(Class<? extends Provider> type) {
        this.active = false; 
        this.providerType = type;
        this.probes = new HashMap<Method,ProbeSkeleton>();
    }
    public void init() {
        Method[] methods = AccessController.doPrivileged(new PrivilegedAction<Method[]>() {
            public Method[] run() {
                return providerType.getDeclaredMethods();
            }
        });
        for (Method m : methods) {
            if ( m.getReturnType() != Void.TYPE ) {
                throw new IllegalArgumentException(
                   "Return value of method is not void");
            } else {
                probes.put(m, createProbe(m));
            }
        }
        this.active = true;
    }
    @SuppressWarnings("unchecked")
    public <T extends Provider> T newProxyInstance() {
        return (T)Proxy.newProxyInstance(providerType.getClassLoader(),
               new Class<?>[] { providerType }, this);
    }
    public Object invoke(Object proxy, Method method, Object[] args) {
        if (method.getDeclaringClass() != providerType) {
            try {
                return method.invoke(this, args);
            } catch (IllegalAccessException e) {
                assert false;
            } catch (InvocationTargetException e) {
                assert false;
            }
        } else if (active) {
            ProbeSkeleton p = probes.get(method);
            if (p != null) {
                p.uncheckedTrigger(args);
            }
        }
        return null;
    }
    public Probe getProbe(Method m) {
        return active ? probes.get(m) : null;
    }
    public void dispose() {
        active = false;
        probes.clear();
    }
    protected String getProviderName() {
        return getAnnotationString(
                providerType, ProviderName.class, providerType.getSimpleName());
    }
    protected static String getAnnotationString(
            AnnotatedElement element, Class<? extends Annotation> annotation,
            String defaultValue) {
        String ret = (String)getAnnotationValue(
                element, annotation, "value", defaultValue);
        return ret.isEmpty() ? defaultValue : ret;
    }
    protected static Object getAnnotationValue(
            AnnotatedElement element, Class<? extends Annotation> annotation,
            String methodName, Object defaultValue) {
        Object ret = defaultValue;
        try {
            Method m = annotation.getMethod(methodName);
            Annotation a = element.getAnnotation(annotation);
            ret = m.invoke(a);
        } catch (NoSuchMethodException e) {
            assert false;
        } catch (IllegalAccessException e) {
            assert false;
        } catch (InvocationTargetException e) {
            assert false;
        } catch (NullPointerException e) {
            assert false;
        }
        return ret;
    }
}
