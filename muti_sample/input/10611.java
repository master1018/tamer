public abstract class DelegateInvocationHandlerImpl
{
    private DelegateInvocationHandlerImpl() {}
    public static InvocationHandler create( final Object delegate )
    {
        SecurityManager s = System.getSecurityManager();
        if (s != null) {
            s.checkPermission(new DynamicAccessPermission("access"));
        }
        return new InvocationHandler() {
            public Object invoke( Object proxy, Method method, Object[] args )
                throws Throwable
            {
                try {
                    return method.invoke( delegate, args ) ;
                } catch (InvocationTargetException ite) {
                    throw ite.getCause() ;
                }
            }
        } ;
    }
}
