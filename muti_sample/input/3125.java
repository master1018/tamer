public abstract class POAPolicyMediatorBase implements POAPolicyMediator {
    protected POAImpl poa ;
    protected ORB orb ;
    private int sysIdCounter ;
    private Policies policies ;
    private DelegateImpl delegateImpl ;
    private int serverid ;
    private int scid ;
    protected boolean isImplicit ;
    protected boolean isUnique ;
    protected boolean isSystemId ;
    public final Policies getPolicies()
    {
        return policies ;
    }
    public final int getScid()
    {
        return scid ;
    }
    public final int getServerId()
    {
        return serverid ;
    }
    POAPolicyMediatorBase( Policies policies, POAImpl poa )
    {
        if (policies.isSingleThreaded())
            throw poa.invocationWrapper().singleThreadNotSupported() ;
        POAManagerImpl poam = (POAManagerImpl)(poa.the_POAManager()) ;
        POAFactory poaf = poam.getFactory() ;
        delegateImpl = (DelegateImpl)(poaf.getDelegateImpl()) ;
        this.policies = policies ;
        this.poa = poa ;
        orb = (ORB)poa.getORB() ;
        switch (policies.servantCachingLevel()) {
            case ServantCachingPolicy.NO_SERVANT_CACHING :
                scid = ORBConstants.TRANSIENT_SCID ;
                break ;
            case ServantCachingPolicy.FULL_SEMANTICS :
                scid = ORBConstants.SC_TRANSIENT_SCID ;
                break ;
            case ServantCachingPolicy.INFO_ONLY_SEMANTICS :
                scid = ORBConstants.IISC_TRANSIENT_SCID ;
                break ;
            case ServantCachingPolicy.MINIMAL_SEMANTICS :
                scid = ORBConstants.MINSC_TRANSIENT_SCID ;
                break ;
        }
        if ( policies.isTransient() ) {
            serverid = orb.getTransientServerId();
        } else {
            serverid = orb.getORBData().getPersistentServerId();
            scid = ORBConstants.makePersistent( scid ) ;
        }
        isImplicit = policies.isImplicitlyActivated() ;
        isUnique = policies.isUniqueIds() ;
        isSystemId = policies.isSystemAssignedIds() ;
        sysIdCounter = 0 ;
    }
    public final java.lang.Object getInvocationServant( byte[] id,
        String operation ) throws ForwardRequest
    {
        java.lang.Object result = internalGetServant( id, operation ) ;
        return result ;
    }
    protected final void setDelegate(Servant servant, byte[] id)
    {
        servant._set_delegate(delegateImpl);
    }
    public synchronized byte[] newSystemId() throws WrongPolicy
    {
        if (!isSystemId)
            throw new WrongPolicy() ;
        byte[] array = new byte[8];
        ORBUtility.intToBytes(++sysIdCounter, array, 0);
        ORBUtility.intToBytes( poa.getPOAId(), array, 4);
        return array;
    }
    protected abstract  java.lang.Object internalGetServant( byte[] id,
        String operation ) throws ForwardRequest ;
}
