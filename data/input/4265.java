public final class OldJIDLObjectKeyTemplate extends OldObjectKeyTemplateBase
{
    public static final byte NULL_PATCH_VERSION = 0;
    byte patchVersion = OldJIDLObjectKeyTemplate.NULL_PATCH_VERSION;
    public OldJIDLObjectKeyTemplate( ORB orb, int magic, int scid,
        InputStream is, OctetSeqHolder osh )
    {
        this( orb, magic, scid, is );
        osh.value = readObjectKey( is ) ;
        if (magic == ObjectKeyFactoryImpl.JAVAMAGIC_NEW &&
            osh.value.length > ((CDRInputStream)is).getPosition()) {
            patchVersion = is.read_octet();
            if (patchVersion == ObjectKeyFactoryImpl.JDK1_3_1_01_PATCH_LEVEL)
                setORBVersion(ORBVersionFactory.getJDK1_3_1_01());
            else if (patchVersion > ObjectKeyFactoryImpl.JDK1_3_1_01_PATCH_LEVEL)
                setORBVersion(ORBVersionFactory.getORBVersion());
            else
                throw wrapper.invalidJdk131PatchLevel( new Integer( patchVersion ) ) ;
        }
    }
    public OldJIDLObjectKeyTemplate( ORB orb, int magic, int scid, int serverid)
    {
        super( orb, magic, scid, serverid, JIDL_ORB_ID, JIDL_OAID ) ;
    }
    public OldJIDLObjectKeyTemplate(ORB orb, int magic, int scid, InputStream is)
    {
        this( orb, magic, scid, is.read_long() ) ;
    }
    protected void writeTemplate( OutputStream os )
    {
        os.write_long( getMagic() ) ;
        os.write_long( getSubcontractId() ) ;
        os.write_long( getServerId() ) ;
    }
    public void write(ObjectId objectId, OutputStream os)
    {
        super.write(objectId, os);
        if (patchVersion != OldJIDLObjectKeyTemplate.NULL_PATCH_VERSION)
           os.write_octet( patchVersion ) ;
    }
}
