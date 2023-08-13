public class InvocationHandlerFactoryImpl implements InvocationHandlerFactory
{
    private final PresentationManager.ClassData classData ;
    private final PresentationManager pm ;
    private Class[] proxyInterfaces ;
    public InvocationHandlerFactoryImpl( PresentationManager pm,
        PresentationManager.ClassData classData )
    {
        this.classData = classData ;
        this.pm = pm ;
        Class[] remoteInterfaces =
            classData.getIDLNameTranslator().getInterfaces() ;
        proxyInterfaces = new Class[ remoteInterfaces.length + 1 ] ;
        for (int ctr=0; ctr<remoteInterfaces.length; ctr++)
            proxyInterfaces[ctr] = remoteInterfaces[ctr] ;
        proxyInterfaces[remoteInterfaces.length] = DynamicStub.class ;
    }
    private class CustomCompositeInvocationHandlerImpl extends
        CompositeInvocationHandlerImpl implements LinkedInvocationHandler,
        Serializable
    {
        private transient DynamicStub stub ;
        public void setProxy( Proxy proxy )
        {
            ((DynamicStubImpl)stub).setSelf( (DynamicStub)proxy ) ;
        }
        public Proxy getProxy()
        {
            return (Proxy)((DynamicStubImpl)stub).getSelf() ;
        }
        public CustomCompositeInvocationHandlerImpl( DynamicStub stub )
        {
            this.stub = stub ;
        }
        public Object writeReplace() throws ObjectStreamException
        {
            return stub ;
        }
    }
    public InvocationHandler getInvocationHandler()
    {
        final DynamicStub stub = new DynamicStubImpl(
            classData.getTypeIds() ) ;
        return getInvocationHandler( stub ) ;
    }
    InvocationHandler getInvocationHandler( DynamicStub stub )
    {
        InvocationHandler dynamicStubHandler =
            DelegateInvocationHandlerImpl.create( stub ) ;
        InvocationHandler stubMethodHandler = new StubInvocationHandlerImpl(
            pm, classData, stub ) ;
        final CompositeInvocationHandler handler =
            new CustomCompositeInvocationHandlerImpl( stub ) ;
        handler.addInvocationHandler( DynamicStub.class,
            dynamicStubHandler ) ;
        handler.addInvocationHandler( org.omg.CORBA.Object.class,
            dynamicStubHandler ) ;
        handler.addInvocationHandler( Object.class,
            dynamicStubHandler ) ;
        handler.setDefaultHandler( stubMethodHandler ) ;
        return handler ;
    }
    public Class[] getProxyInterfaces()
    {
        return proxyInterfaces ;
    }
}
