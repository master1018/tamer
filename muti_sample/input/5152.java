public abstract class IdentifiableFactoryFinderBase implements
    IdentifiableFactoryFinder
{
    private ORB orb ;
    private Map map ;
    protected IORSystemException wrapper ;
    protected IdentifiableFactoryFinderBase( ORB orb )
    {
        map = new HashMap() ;
        this.orb = orb ;
        wrapper = IORSystemException.get( orb,
            CORBALogDomains.OA_IOR ) ;
    }
    protected IdentifiableFactory getFactory(int id)
    {
        Integer ident = new Integer( id ) ;
        IdentifiableFactory factory = (IdentifiableFactory)(map.get(
            ident ) ) ;
        return factory ;
    }
    public abstract Identifiable handleMissingFactory( int id, InputStream is ) ;
    public Identifiable create(int id, InputStream is)
    {
        IdentifiableFactory factory = getFactory( id ) ;
        if (factory != null)
            return factory.create( is ) ;
        else
            return handleMissingFactory( id, is ) ;
    }
    public void registerFactory(IdentifiableFactory factory)
    {
        Integer ident = new Integer( factory.getId() ) ;
        map.put( ident, factory ) ;
    }
}
