public abstract class StubFactoryBase implements PresentationManager.StubFactory
{
    private String[] typeIds = null ;
    protected final PresentationManager.ClassData classData ;
    protected StubFactoryBase( PresentationManager.ClassData classData )
    {
        this.classData = classData ;
    }
    public synchronized String[] getTypeIds()
    {
        if (typeIds == null) {
            if (classData == null) {
                org.omg.CORBA.Object stub = makeStub() ;
                typeIds = StubAdapter.getTypeIds( stub ) ;
            } else {
                typeIds = classData.getTypeIds() ;
            }
        }
        return typeIds ;
    }
}
