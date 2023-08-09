public abstract class ServiceContext {
    protected ServiceContext() { }
    private void dprint( String msg )
    {
        ORBUtility.dprint( this, msg ) ;
    }
    protected ServiceContext(InputStream s, GIOPVersion gv) throws SystemException
    {
        in = s;
    }
    public abstract int getId() ;
    public void write(OutputStream s, GIOPVersion gv) throws SystemException
    {
        EncapsOutputStream os = new EncapsOutputStream( (ORB)(s.orb()), gv ) ;
        os.putEndian() ;
        writeData( os ) ;
        byte[] data = os.toByteArray() ;
        s.write_long(getId());
        s.write_long(data.length);
        s.write_octet_array(data, 0, data.length);
    }
    protected abstract void writeData( OutputStream os ) ;
    protected InputStream in = null ;
    public String toString()
    {
        return "ServiceContext[ id=" + getId() + " ]" ;
    }
}
