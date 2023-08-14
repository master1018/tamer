public class UEInfoServiceContext extends ServiceContext {
    public UEInfoServiceContext( Throwable ex )
    {
        unknown = ex ;
    }
    public UEInfoServiceContext(InputStream is, GIOPVersion gv)
    {
        super(is, gv) ;
        try {
            unknown = (Throwable) in.read_value() ;
        } catch (ThreadDeath d) {
            throw d ;
        } catch (Throwable e) {
            unknown = new UNKNOWN( 0, CompletionStatus.COMPLETED_MAYBE ) ;
        }
    }
    public static final int SERVICE_CONTEXT_ID = 9 ;
    public int getId() { return SERVICE_CONTEXT_ID ; }
    public void writeData( OutputStream os ) throws SystemException
    {
        os.write_value( (Serializable)unknown ) ;
    }
    public Throwable getUE() { return unknown ; }
    private Throwable unknown = null ;
    public String toString()
    {
        return "UEInfoServiceContext[ unknown=" + unknown.toString() + " ]" ;
    }
}
