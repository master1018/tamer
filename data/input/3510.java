public class GenericTaggedProfile extends GenericIdentifiable implements TaggedProfile
{
    private ORB orb ;
    public GenericTaggedProfile( int id, InputStream is )
    {
        super( id, is ) ;
        this.orb = (ORB)(is.orb()) ;
    }
    public GenericTaggedProfile( ORB orb, int id, byte[] data )
    {
        super( id, data ) ;
        this.orb = orb ;
    }
    public TaggedProfileTemplate getTaggedProfileTemplate()
    {
        return null ;
    }
    public ObjectId getObjectId()
    {
        return null ;
    }
    public ObjectKeyTemplate getObjectKeyTemplate()
    {
        return null ;
    }
    public ObjectKey getObjectKey()
    {
        return null ;
    }
    public boolean isEquivalent( TaggedProfile prof )
    {
        return equals( prof ) ;
    }
    public void makeImmutable()
    {
    }
    public boolean isLocal()
    {
        return false ;
    }
    public org.omg.IOP.TaggedProfile getIOPProfile()
    {
        EncapsOutputStream os = new EncapsOutputStream( orb ) ;
        write( os ) ;
        InputStream is = (InputStream)(os.create_input_stream()) ;
        return org.omg.IOP.TaggedProfileHelper.read( is ) ;
    }
}
