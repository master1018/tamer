public abstract class StubFactoryFactoryBase implements
    PresentationManager.StubFactoryFactory
{
    public String getStubName(String fullName)
    {
        return Utility.stubName( fullName ) ;
    }
}
