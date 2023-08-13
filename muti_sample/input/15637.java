public class GenericTaggedComponent extends GenericIdentifiable
    implements TaggedComponent
{
    public GenericTaggedComponent( int id, InputStream is )
    {
        super( id, is ) ;
    }
    public GenericTaggedComponent( int id, byte[] data )
    {
        super( id, data ) ;
    }
    public org.omg.IOP.TaggedComponent getIOPComponent( ORB orb )
    {
        return new org.omg.IOP.TaggedComponent( getId(),
            getData() ) ;
    }
}
