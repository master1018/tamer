public class TaggedProfileTemplateFactoryFinderImpl extends
    IdentifiableFactoryFinderBase
{
    public TaggedProfileTemplateFactoryFinderImpl( ORB orb )
    {
        super( orb ) ;
    }
    public Identifiable handleMissingFactory( int id, InputStream is)
    {
        throw wrapper.taggedProfileTemplateFactoryNotFound( new Integer(id) ) ;
    }
}
