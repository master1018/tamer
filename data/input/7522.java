public class TOAFactory implements ObjectAdapterFactory
{
    private ORB orb ;
    private ORBUtilSystemException wrapper ;
    private TOAImpl toa ;
    private Map codebaseToTOA ;
    private TransientObjectManager tom ;
    public ObjectAdapter find ( ObjectAdapterId oaid )
    {
        if (oaid.equals( ObjectKeyTemplateBase.JIDL_OAID )  )
            return getTOA() ;
        else
            throw wrapper.badToaOaid() ;
    }
    public void init( ORB orb )
    {
        this.orb = orb ;
        wrapper = ORBUtilSystemException.get( orb,
            CORBALogDomains.OA_LIFECYCLE ) ;
        tom = new TransientObjectManager( orb ) ;
        codebaseToTOA = new HashMap() ;
    }
    public void shutdown( boolean waitForCompletion )
    {
        if (Util.instance != null) {
            Util.instance.unregisterTargetsForORB(orb);
        }
    }
    public synchronized TOA getTOA( String codebase )
    {
        TOA toa = (TOA)(codebaseToTOA.get( codebase )) ;
        if (toa == null) {
            toa = new TOAImpl( orb, tom, codebase ) ;
            codebaseToTOA.put( codebase, toa ) ;
        }
        return toa ;
    }
    public synchronized TOA getTOA()
    {
        if (toa == null)
            toa = new TOAImpl( orb, tom, null ) ;
        return toa ;
    }
    public ORB getORB()
    {
        return orb ;
    }
} ;
