abstract public class ObjectAdapterBase extends org.omg.CORBA.LocalObject
    implements ObjectAdapter
{
    private ORB orb;
    private final POASystemException _iorWrapper ;
    private final POASystemException _invocationWrapper ;
    private final POASystemException _lifecycleWrapper ;
    private final OMGSystemException _omgInvocationWrapper ;
    private final OMGSystemException _omgLifecycleWrapper ;
    private IORTemplate iortemp;
    private byte[] adapterId ;
    private ObjectReferenceTemplate adapterTemplate ;
    private ObjectReferenceFactory currentFactory ;
    public ObjectAdapterBase( ORB orb )
    {
        this.orb = orb ;
        _iorWrapper = POASystemException.get( orb,
            CORBALogDomains.OA_IOR ) ;
        _lifecycleWrapper = POASystemException.get( orb,
            CORBALogDomains.OA_LIFECYCLE ) ;
        _omgLifecycleWrapper = OMGSystemException.get( orb,
            CORBALogDomains.OA_LIFECYCLE ) ;
        _invocationWrapper = POASystemException.get( orb,
            CORBALogDomains.OA_INVOCATION ) ;
        _omgInvocationWrapper = OMGSystemException.get( orb,
            CORBALogDomains.OA_INVOCATION ) ;
    }
    public final POASystemException iorWrapper()
    {
        return _iorWrapper ;
    }
    public final POASystemException lifecycleWrapper()
    {
        return _lifecycleWrapper ;
    }
    public final OMGSystemException omgLifecycleWrapper()
    {
        return _omgLifecycleWrapper ;
    }
    public final POASystemException invocationWrapper()
    {
        return _invocationWrapper ;
    }
    public final OMGSystemException omgInvocationWrapper()
    {
        return _omgInvocationWrapper ;
    }
    final public void initializeTemplate( ObjectKeyTemplate oktemp,
        boolean notifyORB, Policies policies, String codebase,
        String objectAdapterManagerId, ObjectAdapterId objectAdapterId)
    {
        adapterId = oktemp.getAdapterId() ;
        iortemp = IORFactories.makeIORTemplate(oktemp) ;
        orb.getCorbaTransportManager().addToIORTemplate(
            iortemp, policies,
            codebase, objectAdapterManagerId, objectAdapterId);
        adapterTemplate = IORFactories.makeObjectReferenceTemplate( orb,
            iortemp ) ;
        currentFactory = adapterTemplate ;
        if (notifyORB) {
            PIHandler pih = orb.getPIHandler() ;
            if (pih != null)
                pih.objectAdapterCreated( this ) ;
        }
        iortemp.makeImmutable() ;
    }
    final public org.omg.CORBA.Object makeObject( String repId, byte[] oid )
    {
        return currentFactory.make_object( repId, oid ) ;
    }
    final public byte[] getAdapterId()
    {
        return adapterId ;
    }
    final public ORB getORB()
    {
        return orb ;
    }
    abstract public Policy getEffectivePolicy( int type ) ;
    final public IORTemplate getIORTemplate()
    {
        return iortemp ;
    }
    abstract public int getManagerId() ;
    abstract public short getState() ;
    final public ObjectReferenceTemplate getAdapterTemplate()
    {
        return adapterTemplate ;
    }
    final public ObjectReferenceFactory getCurrentFactory()
    {
        return currentFactory ;
    }
    final public void setCurrentFactory( ObjectReferenceFactory factory )
    {
        currentFactory = factory ;
    }
    abstract public org.omg.CORBA.Object getLocalServant( byte[] objectId ) ;
    abstract public void getInvocationServant( OAInvocationInfo info ) ;
    abstract public void returnServant() ;
    abstract public void enter() throws OADestroyed ;
    abstract public void exit() ;
    abstract protected ObjectCopierFactory getObjectCopierFactory() ;
    public OAInvocationInfo makeInvocationInfo( byte[] objectId )
    {
        OAInvocationInfo info = new OAInvocationInfo( this, objectId ) ;
        info.setCopierFactory( getObjectCopierFactory() ) ;
        return info ;
    }
    abstract public String[] getInterfaces( Object servant, byte[] objectId ) ;
}
