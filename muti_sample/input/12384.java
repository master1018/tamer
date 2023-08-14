public class StubFactoryProxyImpl extends StubFactoryDynamicBase
{
    public StubFactoryProxyImpl( PresentationManager.ClassData classData,
        ClassLoader loader )
    {
        super( classData, loader ) ;
    }
    public org.omg.CORBA.Object makeStub()
    {
        InvocationHandlerFactory factory = classData.getInvocationHandlerFactory() ;
        LinkedInvocationHandler handler =
            (LinkedInvocationHandler)factory.getInvocationHandler() ;
        Class[] interfaces = factory.getProxyInterfaces() ;
        DynamicStub stub = (DynamicStub)Proxy.newProxyInstance( loader, interfaces,
            handler ) ;
        handler.setProxy( (Proxy)stub ) ;
        return stub ;
    }
}
