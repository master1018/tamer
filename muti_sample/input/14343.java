public class SplitLocalResolverImpl implements LocalResolver
{
    private Resolver resolver ;
    private LocalResolver localResolver ;
    public SplitLocalResolverImpl( Resolver resolver,
        LocalResolver localResolver )
    {
        this.resolver = resolver ;
        this.localResolver = localResolver ;
    }
    public void register( String name, Closure closure )
    {
        localResolver.register( name, closure ) ;
    }
    public org.omg.CORBA.Object resolve( String name )
    {
        return resolver.resolve( name ) ;
    }
    public java.util.Set list()
    {
        return resolver.list() ;
    }
}
