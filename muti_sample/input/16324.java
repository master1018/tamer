public final class OldPOAObjectKeyTemplate extends OldObjectKeyTemplateBase
{
    public OldPOAObjectKeyTemplate( ORB orb, int magic, int scid, InputStream is )
    {
        this( orb, magic, scid, is.read_long(), is.read_long(), is.read_long() ) ;
    }
    public OldPOAObjectKeyTemplate( ORB orb, int magic, int scid, InputStream is,
        OctetSeqHolder osh )
    {
        this( orb, magic, scid, is ) ;
        osh.value = readObjectKey( is ) ;
    }
    public OldPOAObjectKeyTemplate( ORB orb, int magic, int scid, int serverid,
        int orbid, int poaid)
    {
        super( orb, magic, scid, serverid,
            Integer.toString( orbid ),
            new ObjectAdapterIdNumber( poaid ) ) ;
    }
    public void writeTemplate(OutputStream os)
    {
        os.write_long( getMagic() ) ;
        os.write_long( getSubcontractId() ) ;
        os.write_long( getServerId() ) ;
        int orbid = Integer.parseInt( getORBId() ) ;
        os.write_long( orbid ) ;
        ObjectAdapterIdNumber oaid = (ObjectAdapterIdNumber)(getObjectAdapterId()) ;
        int poaid = oaid.getOldPOAId()  ;
        os.write_long( poaid ) ;
    }
    public ORBVersion getORBVersion()
    {
        if (getMagic() == ObjectKeyFactoryImpl.JAVAMAGIC_OLD)
            return ORBVersionFactory.getOLD() ;
        else if (getMagic() == ObjectKeyFactoryImpl.JAVAMAGIC_NEW)
            return ORBVersionFactory.getNEW() ;
        else
            throw new INTERNAL() ;
    }
}
