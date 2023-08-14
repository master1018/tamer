public class TaggedComponentFactoryFinderImpl extends
    IdentifiableFactoryFinderBase implements TaggedComponentFactoryFinder
{
    public TaggedComponentFactoryFinderImpl( ORB orb )
    {
        super( orb ) ;
    }
    public Identifiable handleMissingFactory( int id, InputStream is ) {
        return new GenericTaggedComponent( id, is ) ;
    }
    public TaggedComponent create( org.omg.CORBA.ORB orb,
        org.omg.IOP.TaggedComponent comp )
    {
        EncapsOutputStream os = new EncapsOutputStream( (ORB)orb ) ;
        org.omg.IOP.TaggedComponentHelper.write( os, comp ) ;
        InputStream is = (InputStream)(os.create_input_stream() ) ;
        is.read_ulong() ;
        return (TaggedComponent)create( comp.tag, is ) ;
    }
}
