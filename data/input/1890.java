public class CodeSetServiceContext extends ServiceContext {
    public CodeSetServiceContext( CodeSetComponentInfo.CodeSetContext csc )
    {
        this.csc = csc ;
    }
    public CodeSetServiceContext(InputStream is, GIOPVersion gv)
    {
        super(is, gv) ;
        csc = new CodeSetComponentInfo.CodeSetContext() ;
        csc.read( (MarshalInputStream)in ) ;
    }
    public static final int SERVICE_CONTEXT_ID = 1 ;
    public int getId() { return SERVICE_CONTEXT_ID ; }
    public void writeData( OutputStream os ) throws SystemException
    {
        csc.write( (MarshalOutputStream)os ) ;
    }
    public CodeSetComponentInfo.CodeSetContext getCodeSetContext()
    {
        return csc ;
    }
    private CodeSetComponentInfo.CodeSetContext csc ;
    public String toString()
    {
        return "CodeSetServiceContext[ csc=" + csc + " ]" ;
    }
}
