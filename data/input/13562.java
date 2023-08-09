public class WireObjectKeyTemplate implements ObjectKeyTemplate
{
    private ORB orb ;
    private IORSystemException wrapper ;
    public boolean equals( Object obj )
    {
        if (obj == null)
            return false ;
        return obj instanceof WireObjectKeyTemplate ;
    }
    public int hashCode()
    {
        return 53 ; 
    }
    private byte[] getId( InputStream is )
    {
        CDRInputStream cis = (CDRInputStream)is ;
        int len = cis.getBufferLength() ;
        byte[] result = new byte[ len ] ;
        cis.read_octet_array( result, 0, len ) ;
        return result ;
    }
    public WireObjectKeyTemplate( ORB orb )
    {
        initORB( orb ) ;
    }
    public WireObjectKeyTemplate( InputStream is, OctetSeqHolder osh )
    {
        osh.value = getId( is ) ;
        initORB( (ORB)(is.orb())) ;
    }
    private void initORB( ORB orb )
    {
        this.orb = orb ;
        wrapper = IORSystemException.get( orb,
            CORBALogDomains.OA_IOR ) ;
    }
    public void write( ObjectId id, OutputStream os )
    {
        byte[] key = id.getId() ;
        os.write_octet_array( key, 0, key.length ) ;
    }
    public void write( OutputStream os )
    {
    }
    public int getSubcontractId()
    {
        return ORBConstants.DEFAULT_SCID ;
    }
    public int getServerId()
    {
        return -1 ;
    }
    public String getORBId()
    {
        throw wrapper.orbIdNotAvailable() ;
    }
    public ObjectAdapterId getObjectAdapterId()
    {
        throw wrapper.objectAdapterIdNotAvailable() ;
    }
    public byte[] getAdapterId()
    {
        throw wrapper.adapterIdNotAvailable() ;
    }
    public ORBVersion getORBVersion()
    {
        return ORBVersionFactory.getFOREIGN() ;
    }
    public CorbaServerRequestDispatcher getServerRequestDispatcher( ORB orb, ObjectId id )
    {
        byte[] bid = id.getId() ;
        String str = new String( bid ) ;
        return orb.getRequestDispatcherRegistry().getServerRequestDispatcher( str ) ;
    }
}
