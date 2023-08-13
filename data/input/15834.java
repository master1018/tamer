public abstract class NewObjectKeyTemplateBase extends ObjectKeyTemplateBase
{
    public NewObjectKeyTemplateBase( ORB orb, int magic, int scid, int serverid,
        String orbid, ObjectAdapterId oaid )
    {
        super( orb, magic, scid, serverid, orbid, oaid ) ;
        if (magic != ObjectKeyFactoryImpl.JAVAMAGIC_NEWER)
            throw wrapper.badMagic( new Integer( magic ) ) ;
    }
    public void write(ObjectId objectId, OutputStream os)
    {
        super.write( objectId, os ) ;
        getORBVersion().write( os ) ;
    }
    public void write(OutputStream os)
    {
        super.write( os ) ;
        getORBVersion().write( os ) ;
    }
    protected void setORBVersion( InputStream is )
    {
        ORBVersion version = ORBVersionFactory.create( is ) ;
        setORBVersion( version ) ;
    }
}
