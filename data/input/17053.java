public class ServantCachingPolicy extends LocalObject implements Policy
{
    public static final int NO_SERVANT_CACHING = 0 ;
    public static final int FULL_SEMANTICS = 1 ;
    public static final int INFO_ONLY_SEMANTICS =  2 ;
    public static final int MINIMAL_SEMANTICS = 3 ;
    private static ServantCachingPolicy policy = null ;
    private static ServantCachingPolicy infoOnlyPolicy = null ;
    private static ServantCachingPolicy minimalPolicy = null ;
    private int type ;
    public String typeToName()
    {
        switch (type) {
            case FULL_SEMANTICS:
                return "FULL" ;
            case INFO_ONLY_SEMANTICS:
                return "INFO_ONLY" ;
            case MINIMAL_SEMANTICS:
                return "MINIMAL" ;
            default:
                return "UNKNOWN(" + type + ")" ;
        }
    }
    public String toString()
    {
        return "ServantCachingPolicy[" + typeToName() + "]" ;
    }
    private ServantCachingPolicy( int type )
    {
        this.type = type ;
    }
    public int getType()
    {
        return type ;
    }
    public synchronized static ServantCachingPolicy getPolicy()
    {
        return getFullPolicy() ;
    }
    public synchronized static ServantCachingPolicy getFullPolicy()
    {
        if (policy == null)
            policy = new ServantCachingPolicy( FULL_SEMANTICS ) ;
        return policy ;
    }
    public synchronized static ServantCachingPolicy getInfoOnlyPolicy()
    {
        if (infoOnlyPolicy == null)
            infoOnlyPolicy = new ServantCachingPolicy( INFO_ONLY_SEMANTICS ) ;
        return infoOnlyPolicy ;
    }
    public synchronized static ServantCachingPolicy getMinimalPolicy()
    {
        if (minimalPolicy == null)
            minimalPolicy = new ServantCachingPolicy( MINIMAL_SEMANTICS ) ;
        return minimalPolicy ;
    }
    public int policy_type ()
    {
        return ORBConstants.SERVANT_CACHING_POLICY ;
    }
    public org.omg.CORBA.Policy copy ()
    {
        return this ;
    }
    public void destroy ()
    {
    }
}
