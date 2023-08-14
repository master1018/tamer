class BootstrapConstructorAccessorImpl extends ConstructorAccessorImpl {
    private Constructor constructor;
    BootstrapConstructorAccessorImpl(Constructor c) {
        this.constructor = c;
    }
    public Object newInstance(Object[] args)
        throws IllegalArgumentException, InvocationTargetException
    {
        try {
            return UnsafeFieldAccessorImpl.unsafe.
                allocateInstance(constructor.getDeclaringClass());
        } catch (InstantiationException e) {
            throw new InvocationTargetException(e);
        }
    }
}
