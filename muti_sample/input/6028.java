public class ObjectReferenceFactoryImpl extends ObjectReferenceProducerBase
    implements ObjectReferenceFactory, StreamableValue
{
    transient private IORTemplateList iorTemplates ;
    public ObjectReferenceFactoryImpl( InputStream is )
    {
        super( (ORB)(is.orb()) ) ;
        _read( is ) ;
    }
    public ObjectReferenceFactoryImpl( ORB orb, IORTemplateList iortemps )
    {
        super( orb ) ;
        iorTemplates = iortemps ;
    }
    public boolean equals( Object obj )
    {
        if (!(obj instanceof ObjectReferenceFactoryImpl))
            return false ;
        ObjectReferenceFactoryImpl other = (ObjectReferenceFactoryImpl)obj ;
        return (iorTemplates != null) &&
            iorTemplates.equals( other.iorTemplates ) ;
    }
    public int hashCode()
    {
        return iorTemplates.hashCode() ;
    }
    public static final String repositoryId =
        "IDL:com/sun/corba/se/impl/ior/ObjectReferenceFactoryImpl:1.0" ;
    public String[] _truncatable_ids()
    {
        return new String[] { repositoryId } ;
    }
    public TypeCode _type()
    {
        return ObjectReferenceFactoryHelper.type() ;
    }
    public void _read( InputStream is )
    {
        org.omg.CORBA_2_3.portable.InputStream istr =
            (org.omg.CORBA_2_3.portable.InputStream)is ;
        iorTemplates = IORFactories.makeIORTemplateList( istr ) ;
    }
    public void _write( OutputStream os )
    {
        org.omg.CORBA_2_3.portable.OutputStream ostr =
            (org.omg.CORBA_2_3.portable.OutputStream)os ;
        iorTemplates.write( ostr ) ;
    }
    public IORFactory getIORFactory()
    {
        return iorTemplates ;
    }
    public IORTemplateList getIORTemplateList()
    {
        return iorTemplates ;
    }
}
