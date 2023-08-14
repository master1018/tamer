class InstantiationExceptionConstructorAccessorImpl
    extends ConstructorAccessorImpl {
    private String message;
    InstantiationExceptionConstructorAccessorImpl(String message) {
        this.message = message;
    }
    public Object newInstance(Object[] args)
        throws InstantiationException,
               IllegalArgumentException,
               InvocationTargetException
    {
        if (message == null) {
            throw new InstantiationException();
        }
        throw new InstantiationException(message);
    }
}
