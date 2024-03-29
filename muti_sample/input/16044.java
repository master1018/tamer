class NativeConstructorAccessorImpl extends ConstructorAccessorImpl {
    private Constructor c;
    private DelegatingConstructorAccessorImpl parent;
    private int numInvocations;
    NativeConstructorAccessorImpl(Constructor c) {
        this.c = c;
    }
    public Object newInstance(Object[] args)
        throws InstantiationException,
               IllegalArgumentException,
               InvocationTargetException
    {
        if (++numInvocations > ReflectionFactory.inflationThreshold()) {
            ConstructorAccessorImpl acc = (ConstructorAccessorImpl)
                new MethodAccessorGenerator().
                    generateConstructor(c.getDeclaringClass(),
                                        c.getParameterTypes(),
                                        c.getExceptionTypes(),
                                        c.getModifiers());
            parent.setDelegate(acc);
        }
        return newInstance0(c, args);
    }
    void setParent(DelegatingConstructorAccessorImpl parent) {
        this.parent = parent;
    }
    private static native Object newInstance0(Constructor c, Object[] args)
        throws InstantiationException,
               IllegalArgumentException,
               InvocationTargetException;
}
