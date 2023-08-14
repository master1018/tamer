public class SendingContextServiceContext extends ServiceContext {
    public SendingContextServiceContext( IOR ior )
    {
        this.ior = ior ;
    }
    public SendingContextServiceContext(InputStream is, GIOPVersion gv)
    {
        super(is, gv) ;
        ior = new IORImpl( in ) ;
    }
    public static final int SERVICE_CONTEXT_ID = 6 ;
    public int getId() { return SERVICE_CONTEXT_ID ; }
    public void writeData( OutputStream os ) throws SystemException
    {
        ior.write( os ) ;
    }
    public IOR getIOR()
    {
        return ior ;
    }
    private IOR ior = null ;
    public String toString()
    {
        return "SendingContexServiceContext[ ior=" + ior + " ]" ;
    }
}
