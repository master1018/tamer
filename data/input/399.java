public class CopierManagerImpl implements CopierManager
{
    private int defaultId ;
    private DenseIntMapImpl map ;
    private ORB orb ;
    public CopierManagerImpl( ORB orb )
    {
        defaultId = 0 ;
        map = new DenseIntMapImpl() ;
        this.orb = orb ;
    }
    public void setDefaultId( int id )
    {
        defaultId = id ;
    }
    public int getDefaultId()
    {
        return defaultId ;
    }
    public ObjectCopierFactory getObjectCopierFactory( int id )
    {
        return (ObjectCopierFactory)(map.get( id )) ;
    }
    public ObjectCopierFactory getDefaultObjectCopierFactory()
    {
        return (ObjectCopierFactory)(map.get( defaultId )) ;
    }
    public void registerObjectCopierFactory( ObjectCopierFactory factory, int id )
    {
        map.set( id, factory ) ;
    }
}
