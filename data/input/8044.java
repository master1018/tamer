public class TaggedProfileFactoryFinderImpl extends
    IdentifiableFactoryFinderBase
{
    public TaggedProfileFactoryFinderImpl( ORB orb )
    {
        super( orb ) ;
    }
    public Identifiable handleMissingFactory( int id, InputStream is)
    {
        return new GenericTaggedProfile( id, is ) ;
    }
}
