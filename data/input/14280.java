public final class Policies {
    private static final int MIN_POA_POLICY_ID = THREAD_POLICY_ID.value ;
    private static final int MAX_POA_POLICY_ID = REQUEST_PROCESSING_POLICY_ID.value ;
    private static final int POLICY_TABLE_SIZE = MAX_POA_POLICY_ID -
        MIN_POA_POLICY_ID + 1 ;
    int defaultObjectCopierFactoryId ;
    private HashMap policyMap = new HashMap() ; 
    public static final Policies defaultPolicies
        = new Policies() ;
    public static final Policies rootPOAPolicies
        = new Policies(
            ThreadPolicyValue._ORB_CTRL_MODEL,
            LifespanPolicyValue._TRANSIENT,
            IdUniquenessPolicyValue._UNIQUE_ID,
            IdAssignmentPolicyValue._SYSTEM_ID,
            ImplicitActivationPolicyValue._IMPLICIT_ACTIVATION,
            ServantRetentionPolicyValue._RETAIN,
            RequestProcessingPolicyValue._USE_ACTIVE_OBJECT_MAP_ONLY ) ;
    private int[] poaPolicyValues ;
    private int getPolicyValue( int id )
    {
        return poaPolicyValues[ id - MIN_POA_POLICY_ID ] ;
    }
    private void setPolicyValue( int id, int value )
    {
        poaPolicyValues[ id - MIN_POA_POLICY_ID ] = value ;
    }
    private Policies(
        int threadModel,
        int lifespan,
        int idUniqueness,
        int idAssignment,
        int implicitActivation,
        int retention,
        int requestProcessing )
    {
        poaPolicyValues = new int[] {
            threadModel,
            lifespan,
            idUniqueness,
            idAssignment,
            implicitActivation,
            retention,
            requestProcessing };
    }
    private Policies() {
        this( ThreadPolicyValue._ORB_CTRL_MODEL,
            LifespanPolicyValue._TRANSIENT,
            IdUniquenessPolicyValue._UNIQUE_ID,
            IdAssignmentPolicyValue._SYSTEM_ID,
            ImplicitActivationPolicyValue._NO_IMPLICIT_ACTIVATION,
            ServantRetentionPolicyValue._RETAIN,
            RequestProcessingPolicyValue._USE_ACTIVE_OBJECT_MAP_ONLY ) ;
    }
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append( "Policies[" ) ;
        boolean first = true ;
        Iterator iter = policyMap.values().iterator() ;
        while (iter.hasNext()) {
            if (first)
                first = false ;
            else
                buffer.append( "," ) ;
            buffer.append( iter.next().toString() ) ;
        }
        buffer.append( "]" ) ;
        return buffer.toString() ;
    }
    private int getPOAPolicyValue( Policy policy)
    {
        if (policy instanceof ThreadPolicy) {
            return ((ThreadPolicy) policy).value().value();
        } else if (policy instanceof LifespanPolicy) {
            return ((LifespanPolicy) policy).value().value();
        } else if (policy instanceof IdUniquenessPolicy) {
            return ((IdUniquenessPolicy) policy).value().value();
        } else if (policy instanceof IdAssignmentPolicy) {
            return ((IdAssignmentPolicy) policy).value().value();
        } else if (policy instanceof ServantRetentionPolicy) {
            return ((ServantRetentionPolicy) policy).value().value();
        } else if (policy instanceof RequestProcessingPolicy) {
            return  ((RequestProcessingPolicy) policy).value().value();
        } else if (policy instanceof ImplicitActivationPolicy) {
            return ((ImplicitActivationPolicy) policy).value().value();
        }  else
            return -1 ;
    }
    private void checkForPolicyError( BitSet errorSet ) throws InvalidPolicy
    {
        for (short ctr=0; ctr<errorSet.length(); ctr++ )
            if (errorSet.get(ctr))
                throw new InvalidPolicy(ctr);
    }
    private void addToErrorSet( Policy[] policies, int policyId,
        BitSet errorSet )
    {
        for (int ctr=0; ctr<policies.length; ctr++ )
            if (policies[ctr].policy_type() == policyId) {
                errorSet.set( ctr ) ;
                return ;
            }
    }
    Policies(Policy[] policies, int id ) throws InvalidPolicy
    {
        this();
        defaultObjectCopierFactoryId = id ;
        if ( policies == null )
            return;
        BitSet errorSet = new BitSet( policies.length ) ;
        for(short i = 0; i < policies.length; i++) {
            Policy policy = policies[i];
            int POAPolicyValue = getPOAPolicyValue( policy ) ;
            Integer key = new Integer( policy.policy_type() ) ;
            Policy prev = (Policy)(policyMap.get( key )) ;
            if (prev == null)
                policyMap.put( key, policy ) ;
            if (POAPolicyValue >= 0) {
                setPolicyValue( key.intValue(), POAPolicyValue  ) ;
                if ((prev != null) &&
                    (getPOAPolicyValue( prev ) != POAPolicyValue))
                    errorSet.set( i ) ;
            }
        }
        if (!retainServants() && useActiveMapOnly() ) {
            addToErrorSet( policies, SERVANT_RETENTION_POLICY_ID.value,
                errorSet ) ;
            addToErrorSet( policies, REQUEST_PROCESSING_POLICY_ID.value,
                errorSet ) ;
        }
        if (isImplicitlyActivated()) {
            if (!retainServants()) {
                addToErrorSet( policies, IMPLICIT_ACTIVATION_POLICY_ID.value,
                    errorSet ) ;
                addToErrorSet( policies, SERVANT_RETENTION_POLICY_ID.value,
                    errorSet ) ;
            }
            if (!isSystemAssignedIds()) {
                addToErrorSet( policies, IMPLICIT_ACTIVATION_POLICY_ID.value,
                    errorSet ) ;
                addToErrorSet( policies, ID_ASSIGNMENT_POLICY_ID.value,
                    errorSet ) ;
            }
        }
        checkForPolicyError( errorSet ) ;
    }
    public Policy get_effective_policy( int type )
    {
        Integer key = new Integer( type ) ;
        Policy result = (Policy)(policyMap.get(key)) ;
        return result ;
    }
    public final boolean isOrbControlledThreads() {
        return getPolicyValue( THREAD_POLICY_ID.value ) ==
            ThreadPolicyValue._ORB_CTRL_MODEL;
    }
    public final boolean isSingleThreaded() {
        return getPolicyValue( THREAD_POLICY_ID.value ) ==
            ThreadPolicyValue._SINGLE_THREAD_MODEL;
    }
    public final boolean isTransient() {
        return getPolicyValue( LIFESPAN_POLICY_ID.value ) ==
            LifespanPolicyValue._TRANSIENT;
    }
    public final boolean isPersistent() {
        return getPolicyValue( LIFESPAN_POLICY_ID.value ) ==
            LifespanPolicyValue._PERSISTENT;
    }
    public final boolean isUniqueIds() {
        return getPolicyValue( ID_UNIQUENESS_POLICY_ID.value ) ==
            IdUniquenessPolicyValue._UNIQUE_ID;
    }
    public final boolean isMultipleIds() {
        return getPolicyValue( ID_UNIQUENESS_POLICY_ID.value ) ==
            IdUniquenessPolicyValue._MULTIPLE_ID;
    }
    public final boolean isUserAssignedIds() {
        return getPolicyValue( ID_ASSIGNMENT_POLICY_ID.value ) ==
            IdAssignmentPolicyValue._USER_ID;
    }
    public final boolean isSystemAssignedIds() {
        return getPolicyValue( ID_ASSIGNMENT_POLICY_ID.value ) ==
            IdAssignmentPolicyValue._SYSTEM_ID;
    }
    public final boolean retainServants() {
        return getPolicyValue( SERVANT_RETENTION_POLICY_ID.value ) ==
            ServantRetentionPolicyValue._RETAIN;
    }
    public final boolean useActiveMapOnly() {
        return getPolicyValue( REQUEST_PROCESSING_POLICY_ID.value ) ==
            RequestProcessingPolicyValue._USE_ACTIVE_OBJECT_MAP_ONLY;
    }
    public final boolean useDefaultServant() {
        return getPolicyValue( REQUEST_PROCESSING_POLICY_ID.value ) ==
            RequestProcessingPolicyValue._USE_DEFAULT_SERVANT;
    }
    public final boolean useServantManager() {
        return getPolicyValue( REQUEST_PROCESSING_POLICY_ID.value ) ==
            RequestProcessingPolicyValue._USE_SERVANT_MANAGER;
    }
    public final boolean isImplicitlyActivated() {
        return getPolicyValue( IMPLICIT_ACTIVATION_POLICY_ID.value ) ==
        ImplicitActivationPolicyValue._IMPLICIT_ACTIVATION;
    }
    public final int servantCachingLevel()
    {
        Integer key = new Integer( ORBConstants.SERVANT_CACHING_POLICY ) ;
        ServantCachingPolicy policy = (ServantCachingPolicy)policyMap.get( key ) ;
        if (policy == null)
            return ServantCachingPolicy.NO_SERVANT_CACHING ;
        else
            return policy.getType() ;
    }
    public final boolean forceZeroPort()
    {
        Integer key = new Integer( ORBConstants.ZERO_PORT_POLICY ) ;
        ZeroPortPolicy policy = (ZeroPortPolicy)policyMap.get( key ) ;
        if (policy == null)
            return false ;
        else
            return policy.forceZeroPort() ;
    }
    public final int getCopierId()
    {
        Integer key = new Integer( ORBConstants.COPY_OBJECT_POLICY ) ;
        CopyObjectPolicy policy = (CopyObjectPolicy)policyMap.get( key ) ;
        if (policy != null)
            return policy.getValue() ;
        else
            return defaultObjectCopierFactoryId ;
    }
}
