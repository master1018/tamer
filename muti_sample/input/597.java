public abstract class StubFactoryDynamicBase extends StubFactoryBase
{
    protected final ClassLoader loader ;
    public StubFactoryDynamicBase( PresentationManager.ClassData classData,
        ClassLoader loader )
    {
        super( classData ) ;
        if (loader == null) {
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            if (cl == null)
                cl = ClassLoader.getSystemClassLoader();
            this.loader = cl ;
        } else {
            this.loader = loader ;
        }
    }
    public abstract org.omg.CORBA.Object makeStub() ;
}
