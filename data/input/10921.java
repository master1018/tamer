abstract class ConstructorAccessorImpl extends MagicAccessorImpl
    implements ConstructorAccessor {
    public abstract Object newInstance(Object[] args)
        throws InstantiationException,
               IllegalArgumentException,
               InvocationTargetException;
}
