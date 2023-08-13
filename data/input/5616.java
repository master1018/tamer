public final class POAObjectKeyTemplate extends NewObjectKeyTemplateBase
{
    public POAObjectKeyTemplate( ORB orb, int magic, int scid, InputStream is )
    {
        super( orb, magic, scid, is.read_long(), is.read_string(),
            new ObjectAdapterIdArray( POANameHelper.read( is ) ) ) ;
        setORBVersion( is ) ;
    }
    public POAObjectKeyTemplate( ORB orb, int magic, int scid, InputStream is,
        OctetSeqHolder osh )
    {
        super( orb, magic, scid, is.read_long(), is.read_string(),
            new ObjectAdapterIdArray( POANameHelper.read( is ) ) ) ;
        osh.value = readObjectKey( is ) ;
        setORBVersion( is ) ;
    }
    public POAObjectKeyTemplate( ORB orb, int scid, int serverid, String orbid,
        ObjectAdapterId objectAdapterId)
    {
        super( orb, ObjectKeyFactoryImpl.JAVAMAGIC_NEWER, scid, serverid, orbid,
            objectAdapterId ) ;
        setORBVersion( ORBVersionFactory.getORBVersion() ) ;
    }
    public void writeTemplate(OutputStream os)
    {
        os.write_long( getMagic() ) ;
        os.write_long( getSubcontractId() ) ;
        os.write_long( getServerId() ) ;
        os.write_string( getORBId() ) ;
        getObjectAdapterId().write( os ) ;
    }
}
