public class CompositeInvocationHandlerImpl implements
    CompositeInvocationHandler
{
    private Map classToInvocationHandler = new LinkedHashMap() ;
    private InvocationHandler defaultHandler = null ;
    public void addInvocationHandler( Class interf,
        InvocationHandler handler )
    {
        classToInvocationHandler.put( interf, handler ) ;
    }
    public void setDefaultHandler( InvocationHandler handler )
    {
        defaultHandler = handler ;
    }
    public Object invoke( Object proxy, Method method, Object[] args )
        throws Throwable
    {
        Class cls = method.getDeclaringClass() ;
        InvocationHandler handler =
            (InvocationHandler)classToInvocationHandler.get( cls ) ;
        if (handler == null) {
            if (defaultHandler != null)
                handler = defaultHandler ;
            else {
                ORBUtilSystemException wrapper = ORBUtilSystemException.get(
                    CORBALogDomains.UTIL ) ;
                throw wrapper.noInvocationHandler( "\"" + method.toString() +
                    "\"" ) ;
            }
        }
        return handler.invoke( proxy, method, args ) ;
    }
}
