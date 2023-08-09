public class StubFactoryFactoryProxyImpl extends StubFactoryFactoryDynamicBase
{
    public PresentationManager.StubFactory makeDynamicStubFactory(
        PresentationManager pm, PresentationManager.ClassData classData,
        ClassLoader classLoader )
    {
        return new StubFactoryProxyImpl( classData, classLoader ) ;
    }
}
