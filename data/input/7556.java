public abstract class TaggedComponentBase extends IdentifiableBase
    implements TaggedComponent
{
    public org.omg.IOP.TaggedComponent getIOPComponent(
        org.omg.CORBA.ORB orb )
    {
        EncapsOutputStream os = new EncapsOutputStream( (ORB)orb ) ;
        write( os ) ;
        InputStream is = (InputStream)(os.create_input_stream() ) ;
        return org.omg.IOP.TaggedComponentHelper.read( is ) ;
    }
}
