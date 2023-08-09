public class OAInvocationInfo extends ServantObject {
    private java.lang.Object    servantContainer ;
    private ObjectAdapter       oa;
    private byte[]              oid;
    private CookieHolder        cookieHolder;
    private String              operation;
    private ObjectCopierFactory factory ;
    public OAInvocationInfo(ObjectAdapter oa, byte[] id )
    {
        this.oa = oa;
        this.oid  = id;
    }
    public OAInvocationInfo( OAInvocationInfo info, String operation )
    {
        this.servant            = info.servant ;
        this.servantContainer   = info.servantContainer ;
        this.cookieHolder       = info.cookieHolder ;
        this.oa                 = info.oa;
        this.oid                = info.oid;
        this.factory            = info.factory ;
        this.operation          = operation;
    }
    public ObjectAdapter    oa()                    { return oa ; }
    public byte[]           id()                    { return oid ; }
    public Object           getServantContainer()   { return servantContainer ; }
    public CookieHolder     getCookieHolder()
    {
        if (cookieHolder == null)
            cookieHolder = new CookieHolder() ;
        return cookieHolder;
    }
    public String           getOperation()      { return operation; }
    public ObjectCopierFactory  getCopierFactory()      { return factory; }
    public void setOperation( String operation )    { this.operation = operation ; }
    public void setCopierFactory( ObjectCopierFactory factory )    { this.factory = factory ; }
    public void setServant(Object servant)
    {
        servantContainer = servant ;
        if (servant instanceof Tie)
            this.servant = ((Tie)servant).getTarget() ;
        else
            this.servant = servant;
    }
}
