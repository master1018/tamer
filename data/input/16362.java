abstract class MethodAccessorImpl extends MagicAccessorImpl
    implements MethodAccessor {
    public abstract Object invoke(Object obj, Object[] args)
        throws IllegalArgumentException, InvocationTargetException;
}
