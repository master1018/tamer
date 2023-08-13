public abstract class PresentationDefaults
{
    private static StubFactoryFactoryStaticImpl staticImpl = null ;
    private PresentationDefaults() {}
    public synchronized static PresentationManager.StubFactoryFactory
        getStaticStubFactoryFactory()
    {
        if (staticImpl == null)
            staticImpl = new StubFactoryFactoryStaticImpl( );
        return staticImpl ;
    }
    public static PresentationManager.StubFactoryFactory
        getProxyStubFactoryFactory()
    {
        return new StubFactoryFactoryProxyImpl();
    }
    public static PresentationManager.StubFactory makeStaticStubFactory(
        Class stubClass )
    {
        return new StubFactoryStaticImpl( stubClass ) ;
    }
}
