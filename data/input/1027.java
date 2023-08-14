public class POAImpl extends ObjectAdapterBase implements POA
{
    private boolean debug ;
    private static final int STATE_START        = 0 ; 
    private static final int STATE_INIT         = 1 ; 
    private static final int STATE_INIT_DONE    = 2 ; 
    private static final int STATE_RUN          = 3 ; 
    private static final int STATE_DESTROYING   = 4 ; 
    private static final int STATE_DESTROYED    = 5 ; 
    private String stateToString()
    {
        switch (state) {
            case STATE_START :
                return "START" ;
            case STATE_INIT :
                return "INIT" ;
            case STATE_INIT_DONE :
                return "INIT_DONE" ;
            case STATE_RUN :
                return "RUN" ;
            case STATE_DESTROYING :
                return "DESTROYING" ;
            case STATE_DESTROYED :
                return "DESTROYED" ;
            default :
                return "UNKNOWN(" + state + ")" ;
        }
    }
    private int state ;
    private POAPolicyMediator mediator;
    private int numLevels;          
    private ObjectAdapterId poaId ; 
    private String name;            
    private POAManagerImpl manager; 
    private int uniquePOAId ;       
    private POAImpl parent;         
    private Map children;           
    private AdapterActivator activator;
    private int invocationCount ; 
    Sync poaMutex ;
    private CondVar adapterActivatorCV ;
    private CondVar invokeCV ;
    private CondVar beingDestroyedCV ;
    protected ThreadLocal isDestroying ;
    public String toString()
    {
        return "POA[" + poaId.toString() +
            ", uniquePOAId=" + uniquePOAId +
            ", state=" + stateToString() +
            ", invocationCount=" + invocationCount + "]" ;
    }
    boolean getDebug()
    {
        return debug ;
    }
    static POAFactory getPOAFactory( ORB orb )
    {
        return (POAFactory)orb.getRequestDispatcherRegistry().
            getObjectAdapterFactory( ORBConstants.TRANSIENT_SCID ) ;
    }
    static POAImpl makeRootPOA( ORB orb )
    {
        POAManagerImpl poaManager = new POAManagerImpl( getPOAFactory( orb ),
            orb.getPIHandler() ) ;
        POAImpl result = new POAImpl( ORBConstants.ROOT_POA_NAME,
            null, orb, STATE_START ) ;
        result.initialize( poaManager, Policies.rootPOAPolicies ) ;
        return result ;
    }
    int getPOAId()
    {
        return uniquePOAId ;
    }
    void lock()
    {
        SyncUtil.acquire( poaMutex ) ;
        if (debug) {
            ORBUtility.dprint( this, "LOCKED poa " + this ) ;
        }
    }
    void unlock()
    {
        if (debug) {
            ORBUtility.dprint( this, "UNLOCKED poa " + this ) ;
        }
        poaMutex.release() ;
    }
    Policies getPolicies()
    {
        return mediator.getPolicies() ;
    }
    private POAImpl( String name, POAImpl parent, ORB orb, int initialState )
    {
        super( orb ) ;
        debug = orb.poaDebugFlag ;
        if (debug) {
            ORBUtility.dprint( this, "Creating POA with name=" + name +
                " parent=" + parent ) ;
        }
        this.state     = initialState ;
        this.name      = name ;
        this.parent    = parent;
        children = new HashMap();
        activator = null ;
        uniquePOAId = getPOAFactory( orb ).newPOAId() ;
        if (parent == null) {
            numLevels = 1 ;
        } else {
            numLevels = parent.numLevels + 1 ;
            parent.children.put(name, this);
        }
        String[] names = new String[ numLevels ] ;
        POAImpl poaImpl = this ;
        int ctr = numLevels - 1 ;
        while (poaImpl != null) {
            names[ctr--] = poaImpl.name ;
            poaImpl = poaImpl.parent ;
        }
        poaId = new ObjectAdapterIdArray( names ) ;
        invocationCount = 0;
        poaMutex = new ReentrantMutex( orb.poaConcurrencyDebugFlag ) ;
        adapterActivatorCV = new CondVar( poaMutex,
            orb.poaConcurrencyDebugFlag ) ;
        invokeCV           = new CondVar( poaMutex,
            orb.poaConcurrencyDebugFlag ) ;
        beingDestroyedCV   = new CondVar( poaMutex,
            orb.poaConcurrencyDebugFlag ) ;
        isDestroying = new ThreadLocal () {
            protected java.lang.Object initialValue() {
                return Boolean.FALSE;
            }
        };
    }
    private void initialize( POAManagerImpl manager, Policies policies )
    {
        if (debug) {
            ORBUtility.dprint( this, "Initializing poa " + this +
                " with POAManager=" + manager + " policies=" + policies ) ;
        }
        this.manager = manager;
        manager.addPOA(this);
        mediator = POAPolicyMediatorFactory.create( policies, this ) ;
        int serverid = mediator.getServerId() ;
        int scid = mediator.getScid() ;
        String orbId = getORB().getORBData().getORBId();
        ObjectKeyTemplate oktemp = new POAObjectKeyTemplate( getORB(),
            scid, serverid, orbId, poaId ) ;
        if (debug) {
            ORBUtility.dprint( this, "Initializing poa: oktemp=" + oktemp ) ;
        }
        boolean objectAdapterCreated = true; 
        initializeTemplate( oktemp, objectAdapterCreated,
                            policies,
                            null, 
                            null, 
                            oktemp.getObjectAdapterId()
                            ) ;
        if (state == STATE_START)
            state = STATE_RUN ;
        else if (state == STATE_INIT)
            state = STATE_INIT_DONE ;
        else
            throw lifecycleWrapper().illegalPoaStateTrans() ;
    }
    private boolean waitUntilRunning()
    {
        if (debug) {
            ORBUtility.dprint( this,
                "Calling waitUntilRunning on poa " + this ) ;
        }
        while (state < STATE_RUN) {
            try {
                adapterActivatorCV.await() ;
            } catch (InterruptedException exc) {
            }
        }
        if (debug) {
            ORBUtility.dprint( this,
                "Exiting waitUntilRunning on poa " + this ) ;
        }
        return (state == STATE_RUN) ;
    }
    private boolean destroyIfNotInitDone()
    {
        try {
            lock() ;
            if (debug) {
                ORBUtility.dprint( this,
                    "Calling destroyIfNotInitDone on poa " + this ) ;
            }
            boolean success = (state == STATE_INIT_DONE) ;
            if (success)
                state = STATE_RUN ;
            else {
                DestroyThread destroyer = new DestroyThread( false, debug );
                destroyer.doIt( this, true ) ;
            }
            return success ;
        } finally {
            adapterActivatorCV.broadcast() ;
            if (debug) {
                ORBUtility.dprint( this,
                    "Exiting destroyIfNotInitDone on poa " + this ) ;
            }
            unlock() ;
        }
    }
    private byte[] internalReferenceToId(
        org.omg.CORBA.Object reference ) throws WrongAdapter
    {
        IOR ior = ORBUtility.getIOR( reference ) ;
        IORTemplateList thisTemplate = ior.getIORTemplates() ;
        ObjectReferenceFactory orf = getCurrentFactory() ;
        IORTemplateList poaTemplate =
            IORFactories.getIORTemplateList( orf ) ;
        if (!poaTemplate.isEquivalent( thisTemplate ))
            throw new WrongAdapter();
        Iterator iter = ior.iterator() ;
        if (!iter.hasNext())
            throw iorWrapper().noProfilesInIor() ;
        TaggedProfile prof = (TaggedProfile)(iter.next()) ;
        ObjectId oid = prof.getObjectId() ;
        return oid.getId();
    }
    static class DestroyThread extends Thread {
        private boolean wait ;
        private boolean etherealize ;
        private boolean debug ;
        private POAImpl thePoa ;
        public DestroyThread( boolean etherealize, boolean debug )
        {
            this.etherealize = etherealize ;
            this.debug = debug ;
        }
        public void doIt( POAImpl thePoa, boolean wait )
        {
            if (debug) {
                ORBUtility.dprint( this,
                    "Calling DestroyThread.doIt(thePOA=" + thePoa +
                    " wait=" + wait + " etherealize=" + etherealize ) ;
            }
            this.thePoa = thePoa ;
            this.wait = wait ;
            if (wait) {
                run() ;
            } else {
                try { setDaemon(true); } catch (Exception e) {}
                start() ;
            }
        }
        public void run()
        {
            Set destroyedPOATemplates = new HashSet() ;
            performDestroy( thePoa, destroyedPOATemplates );
            Iterator iter = destroyedPOATemplates.iterator() ;
            ObjectReferenceTemplate[] orts = new ObjectReferenceTemplate[
                destroyedPOATemplates.size() ] ;
            int index = 0 ;
            while (iter.hasNext())
                orts[ index++ ] = (ObjectReferenceTemplate)iter.next();
            thePoa.getORB().getPIHandler().adapterStateChanged( orts,
                NON_EXISTENT.value ) ;
        }
        private boolean prepareForDestruction( POAImpl poa,
            Set destroyedPOATemplates )
        {
            POAImpl[] childPoas = null ;
            try {
                poa.lock() ;
                if (debug) {
                    ORBUtility.dprint( this,
                        "Calling performDestroy on poa " + poa ) ;
                }
                if (poa.state <= STATE_RUN) {
                    poa.state = STATE_DESTROYING ;
                } else {
                    if (wait)
                        while (poa.state != STATE_DESTROYED) {
                            try {
                                poa.beingDestroyedCV.await() ;
                            } catch (InterruptedException exc) {
                            }
                        }
                    return false ;
                }
                poa.isDestroying.set(Boolean.TRUE);
                childPoas = (POAImpl[])poa.children.values().toArray(
                    new POAImpl[0] );
            } finally {
                poa.unlock() ;
            }
            for (int ctr=0; ctr<childPoas.length; ctr++ ) {
                performDestroy( childPoas[ctr], destroyedPOATemplates ) ;
            }
            return true ;
        }
        public void performDestroy( POAImpl poa, Set destroyedPOATemplates )
        {
            if (!prepareForDestruction( poa, destroyedPOATemplates ))
                return ;
            POAImpl parent = poa.parent ;
            boolean isRoot = parent == null ;
            try {
                if (!isRoot)
                    parent.lock() ;
                try {
                    poa.lock() ;
                    completeDestruction( poa, parent,
                        destroyedPOATemplates ) ;
                } finally {
                    poa.unlock() ;
                    if (isRoot)
                        poa.manager.getFactory().registerRootPOA() ;
                }
            } finally {
                if (!isRoot) {
                    parent.unlock() ;
                    poa.parent = null ;
                }
            }
        }
        private void completeDestruction( POAImpl poa, POAImpl parent,
            Set destroyedPOATemplates )
        {
            if (debug) {
                ORBUtility.dprint( this,
                    "Calling completeDestruction on poa " + poa ) ;
            }
            try {
                while (poa.invocationCount != 0) {
                    try {
                        poa.invokeCV.await() ;
                    } catch (InterruptedException ex) {
                    }
                }
                if (poa.mediator != null) {
                    if (etherealize)
                        poa.mediator.etherealizeAll();
                    poa.mediator.clearAOM() ;
                }
                if (poa.manager != null)
                    poa.manager.removePOA(poa);
                if (parent != null)
                    parent.children.remove( poa.name ) ;
                destroyedPOATemplates.add( poa.getAdapterTemplate() ) ;
            } catch (Throwable thr) {
                if (thr instanceof ThreadDeath)
                    throw (ThreadDeath)thr ;
                poa.lifecycleWrapper().unexpectedException( thr, poa.toString() ) ;
            } finally {
                poa.state = STATE_DESTROYED ;
                poa.beingDestroyedCV.broadcast();
                poa.isDestroying.set(Boolean.FALSE);
                if (debug) {
                    ORBUtility.dprint( this,
                        "Exiting completeDestruction on poa " + poa ) ;
                }
            }
        }
    }
    void etherealizeAll()
    {
        try {
            lock() ;
            if (debug) {
                ORBUtility.dprint( this,
                    "Calling etheralizeAll on poa " + this ) ;
            }
            mediator.etherealizeAll() ;
        } finally {
            if (debug) {
                ORBUtility.dprint( this,
                    "Exiting etheralizeAll on poa " + this ) ;
            }
            unlock() ;
        }
    }
    public POA create_POA(String name, POAManager
        theManager, Policy[] policies) throws AdapterAlreadyExists,
        InvalidPolicy
    {
        try {
            lock() ;
            if (debug) {
                ORBUtility.dprint( this, "Calling create_POA(name=" + name +
                    " theManager=" + theManager + " policies=" + policies +
                    ") on poa " + this ) ;
            }
            if (state > STATE_RUN)
                throw omgLifecycleWrapper().createPoaDestroy() ;
            POAImpl poa = (POAImpl)(children.get(name)) ;
            if (poa == null) {
                poa = new POAImpl( name, this, getORB(), STATE_START ) ;
            }
            try {
                poa.lock() ;
                if (debug) {
                    ORBUtility.dprint( this,
                        "Calling create_POA: new poa is " + poa ) ;
                }
                if ((poa.state != STATE_START) && (poa.state != STATE_INIT))
                    throw new AdapterAlreadyExists();
                POAManagerImpl newManager = (POAManagerImpl)theManager ;
                if (newManager == null)
                    newManager = new POAManagerImpl( manager.getFactory(),
                        manager.getPIHandler() );
                int defaultCopierId =
                    getORB().getCopierManager().getDefaultId() ;
                Policies POAPolicies =
                    new Policies( policies, defaultCopierId ) ;
                poa.initialize( newManager, POAPolicies ) ;
                return poa;
            } finally {
                poa.unlock() ;
            }
        } finally {
            unlock() ;
        }
    }
    public POA find_POA(String name, boolean activate)
        throws AdapterNonExistent
    {
        POAImpl found = null ;
        AdapterActivator act = null ;
        lock() ;
        if (debug) {
            ORBUtility.dprint( this, "Calling find_POA(name=" + name +
                " activate=" + activate + ") on poa " + this ) ;
        }
        found = (POAImpl) children.get(name);
        if (found != null) {
            if (debug) {
                ORBUtility.dprint( this,
                    "Calling find_POA: found poa " + found ) ;
            }
            try {
                found.lock() ;
                unlock() ;
                if (!found.waitUntilRunning())
                    throw omgLifecycleWrapper().poaDestroyed() ;
            } finally {
                found.unlock() ;
            }
        } else {
            try {
                if (debug) {
                    ORBUtility.dprint( this,
                        "Calling find_POA: no poa found" ) ;
                }
                if (activate && (activator != null)) {
                    found = new POAImpl( name, this, getORB(), STATE_INIT ) ;
                    if (debug) {
                        ORBUtility.dprint( this,
                            "Calling find_POA: created poa " + found ) ;
                    }
                    act = activator ;
                } else {
                    throw new AdapterNonExistent();
                }
            } finally {
                unlock() ;
            }
        }
        if (act != null) {
            boolean status = false ;
            boolean adapterResult = false ;
            if (debug) {
                ORBUtility.dprint( this,
                    "Calling find_POA: calling AdapterActivator"  ) ;
            }
            try {
                synchronized (act) {
                    status = act.unknown_adapter(this, name);
                }
            } catch (SystemException exc) {
                throw omgLifecycleWrapper().adapterActivatorException( exc,
                    name, poaId.toString() ) ;
            } catch (Throwable thr) {
                lifecycleWrapper().unexpectedException( thr, this.toString() ) ;
                if (thr instanceof ThreadDeath)
                    throw (ThreadDeath)thr ;
            } finally {
                adapterResult = found.destroyIfNotInitDone() ;
            }
            if (status) {
                if (!adapterResult)
                    throw omgLifecycleWrapper().adapterActivatorException( name,
                        poaId.toString() ) ;
            } else {
                if (debug) {
                    ORBUtility.dprint( this,
                        "Calling find_POA: AdapterActivator returned false"  ) ;
                }
                throw new AdapterNonExistent();
            }
        }
        return found;
    }
    public void destroy(boolean etherealize, boolean wait_for_completion)
    {
        if (wait_for_completion && getORB().isDuringDispatch()) {
            throw lifecycleWrapper().destroyDeadlock() ;
        }
        DestroyThread destroyer = new DestroyThread( etherealize, debug );
        destroyer.doIt( this, wait_for_completion ) ;
    }
    public ThreadPolicy create_thread_policy(
        ThreadPolicyValue value)
    {
        return new ThreadPolicyImpl(value);
    }
    public LifespanPolicy create_lifespan_policy(
        LifespanPolicyValue value)
    {
        return new LifespanPolicyImpl(value);
    }
    public IdUniquenessPolicy create_id_uniqueness_policy(
        IdUniquenessPolicyValue value)
    {
        return new IdUniquenessPolicyImpl(value);
    }
    public IdAssignmentPolicy create_id_assignment_policy(
        IdAssignmentPolicyValue value)
    {
        return new IdAssignmentPolicyImpl(value);
    }
    public ImplicitActivationPolicy create_implicit_activation_policy(
        ImplicitActivationPolicyValue value)
    {
        return new ImplicitActivationPolicyImpl(value);
    }
    public ServantRetentionPolicy create_servant_retention_policy(
        ServantRetentionPolicyValue value)
    {
        return new ServantRetentionPolicyImpl(value);
    }
    public RequestProcessingPolicy create_request_processing_policy(
        RequestProcessingPolicyValue value)
    {
        return new RequestProcessingPolicyImpl(value);
    }
    public String the_name()
    {
        try {
            lock() ;
            return name;
        } finally {
            unlock() ;
        }
    }
    public POA the_parent()
    {
        try {
            lock() ;
            return parent;
        } finally {
            unlock() ;
        }
    }
    public org.omg.PortableServer.POA[] the_children()
    {
        try {
            lock() ;
            Collection coll = children.values() ;
            int size = coll.size() ;
            POA[] result = new POA[ size ] ;
            int index = 0 ;
            Iterator iter = coll.iterator() ;
            while (iter.hasNext()) {
                POA poa = (POA)(iter.next()) ;
                result[ index++ ] = poa ;
            }
            return result ;
        } finally {
            unlock() ;
        }
    }
    public POAManager the_POAManager()
    {
        try {
            lock() ;
            return manager;
        } finally {
            unlock() ;
        }
    }
    public AdapterActivator the_activator()
    {
        try {
            lock() ;
            return activator;
        } finally {
            unlock() ;
        }
    }
    public void the_activator(AdapterActivator activator)
    {
        try {
            lock() ;
            if (debug) {
                ORBUtility.dprint( this, "Calling the_activator on poa " +
                    this + " activator=" + activator ) ;
            }
            this.activator = activator;
        } finally {
            unlock() ;
        }
    }
    public ServantManager get_servant_manager() throws WrongPolicy
    {
        try {
            lock() ;
            return mediator.getServantManager() ;
        } finally {
            unlock() ;
        }
    }
    public void set_servant_manager(ServantManager servantManager)
        throws WrongPolicy
    {
        try {
            lock() ;
            if (debug) {
                ORBUtility.dprint( this, "Calling set_servant_manager on poa " +
                    this + " servantManager=" + servantManager ) ;
            }
            mediator.setServantManager( servantManager ) ;
        } finally {
            unlock() ;
        }
    }
    public Servant get_servant() throws NoServant, WrongPolicy
    {
        try {
            lock() ;
            return mediator.getDefaultServant() ;
        } finally {
            unlock() ;
        }
    }
    public void set_servant(Servant defaultServant)
        throws WrongPolicy
    {
        try {
            lock() ;
            if (debug) {
                ORBUtility.dprint( this, "Calling set_servant on poa " +
                    this + " defaultServant=" + defaultServant ) ;
            }
            mediator.setDefaultServant( defaultServant ) ;
        } finally {
            unlock() ;
        }
    }
    public byte[] activate_object(Servant servant)
        throws ServantAlreadyActive, WrongPolicy
    {
        try {
            lock() ;
            if (debug) {
                ORBUtility.dprint( this,
                    "Calling activate_object on poa " + this +
                    " (servant=" + servant + ")" ) ;
            }
            byte[] id = mediator.newSystemId();
            try {
                mediator.activateObject( id, servant ) ;
            } catch (ObjectAlreadyActive oaa) {
            }
            return id ;
        } finally {
            if (debug) {
                ORBUtility.dprint( this,
                    "Exiting activate_object on poa " + this ) ;
            }
            unlock() ;
        }
    }
    public void activate_object_with_id(byte[] id,
                                                     Servant servant)
        throws ObjectAlreadyActive, ServantAlreadyActive, WrongPolicy
    {
        try {
            lock() ;
            if (debug) {
                ORBUtility.dprint( this,
                    "Calling activate_object_with_id on poa " + this +
                    " (servant=" + servant + " id=" + id + ")" ) ;
            }
            byte[] idClone = (byte[])(id.clone()) ;
            mediator.activateObject( idClone, servant ) ;
        } finally {
            if (debug) {
                ORBUtility.dprint( this,
                    "Exiting activate_object_with_id on poa " + this ) ;
            }
            unlock() ;
        }
    }
    public void deactivate_object(byte[] id)
        throws ObjectNotActive, WrongPolicy
    {
        try {
            lock() ;
            if (debug) {
                ORBUtility.dprint( this,
                    "Calling deactivate_object on poa " + this +
                    " (id=" + id + ")" ) ;
            }
            mediator.deactivateObject( id ) ;
        } finally {
            if (debug) {
                ORBUtility.dprint( this,
                    "Exiting deactivate_object on poa " + this ) ;
            }
            unlock() ;
        }
    }
    public org.omg.CORBA.Object create_reference(String repId)
        throws WrongPolicy
    {
        try {
            lock() ;
            if (debug) {
                ORBUtility.dprint( this, "Calling create_reference(repId=" +
                    repId + ") on poa " + this ) ;
            }
            return makeObject( repId, mediator.newSystemId()) ;
        } finally {
            unlock() ;
        }
    }
    public org.omg.CORBA.Object
        create_reference_with_id(byte[] oid, String repId)
    {
        try {
            lock() ;
            if (debug) {
                ORBUtility.dprint( this,
                    "Calling create_reference_with_id(oid=" +
                    oid + " repId=" + repId + ") on poa " + this ) ;
            }
            byte[] idClone = (byte[])(oid.clone()) ;
            return makeObject( repId, idClone ) ;
        } finally {
            unlock() ;
        }
    }
    public byte[] servant_to_id(Servant servant)
        throws ServantNotActive, WrongPolicy
    {
        try {
            lock() ;
            if (debug) {
                ORBUtility.dprint( this, "Calling servant_to_id(servant=" +
                    servant + ") on poa " + this ) ;
            }
            return mediator.servantToId( servant ) ;
        } finally {
            unlock() ;
        }
    }
    public org.omg.CORBA.Object servant_to_reference(Servant servant)
        throws ServantNotActive, WrongPolicy
    {
        try {
            lock() ;
            if (debug) {
                ORBUtility.dprint( this,
                    "Calling servant_to_reference(servant=" +
                    servant + ") on poa " + this ) ;
            }
            byte[] oid = mediator.servantToId(servant);
            String repId = servant._all_interfaces( this, oid )[0] ;
            return create_reference_with_id(oid, repId);
        } finally {
            unlock() ;
        }
    }
    public Servant reference_to_servant(org.omg.CORBA.Object reference)
        throws ObjectNotActive, WrongPolicy, WrongAdapter
    {
        try {
            lock() ;
            if (debug) {
                ORBUtility.dprint( this,
                    "Calling reference_to_servant(reference=" +
                    reference + ") on poa " + this ) ;
            }
            if ( state >= STATE_DESTROYING ) {
                throw lifecycleWrapper().adapterDestroyed() ;
            }
            byte [] id = internalReferenceToId(reference);
            return mediator.idToServant( id ) ;
        } finally {
            unlock() ;
        }
    }
    public byte[] reference_to_id(org.omg.CORBA.Object reference)
        throws WrongAdapter, WrongPolicy
    {
        try {
            lock() ;
            if (debug) {
                ORBUtility.dprint( this, "Calling reference_to_id(reference=" +
                    reference + ") on poa " + this ) ;
            }
            if( state >= STATE_DESTROYING ) {
                throw lifecycleWrapper().adapterDestroyed() ;
            }
            return internalReferenceToId( reference ) ;
        } finally {
            unlock() ;
        }
    }
    public Servant id_to_servant(byte[] id)
        throws ObjectNotActive, WrongPolicy
    {
        try {
            lock() ;
            if (debug) {
                ORBUtility.dprint( this, "Calling id_to_servant(id=" +
                    id + ") on poa " + this ) ;
            }
            if( state >= STATE_DESTROYING ) {
                throw lifecycleWrapper().adapterDestroyed() ;
            }
            return mediator.idToServant( id ) ;
        } finally {
            unlock() ;
        }
    }
    public org.omg.CORBA.Object id_to_reference(byte[] id)
        throws ObjectNotActive, WrongPolicy
    {
        try {
            lock() ;
            if (debug) {
                ORBUtility.dprint( this, "Calling id_to_reference(id=" +
                    id + ") on poa " + this ) ;
            }
            if( state >= STATE_DESTROYING ) {
                throw lifecycleWrapper().adapterDestroyed() ;
            }
            Servant s = mediator.idToServant( id ) ;
            String repId = s._all_interfaces( this, id )[0] ;
            return makeObject(repId, id );
        } finally {
            unlock() ;
        }
    }
    public byte[] id()
    {
        try {
            lock() ;
            return getAdapterId() ;
        } finally {
            unlock() ;
        }
    }
    public Policy getEffectivePolicy( int type )
    {
        return mediator.getPolicies().get_effective_policy( type ) ;
    }
    public int getManagerId()
    {
        return manager.getManagerId() ;
    }
    public short getState()
    {
        return manager.getORTState() ;
    }
    public String[] getInterfaces( java.lang.Object servant, byte[] objectId )
    {
        Servant serv = (Servant)servant ;
        return serv._all_interfaces( this, objectId ) ;
    }
    protected ObjectCopierFactory getObjectCopierFactory()
    {
        int copierId = mediator.getPolicies().getCopierId() ;
        CopierManager cm = getORB().getCopierManager() ;
        return cm.getObjectCopierFactory( copierId ) ;
    }
    public void enter() throws OADestroyed
    {
        try {
            lock() ;
            if (debug) {
                ORBUtility.dprint( this, "Calling enter on poa " + this ) ;
            }
            while ((state == STATE_DESTROYING) &&
                (isDestroying.get() == Boolean.FALSE)) {
                try {
                    beingDestroyedCV.await();
                } catch (InterruptedException ex) {
                }
            }
            if (!waitUntilRunning())
                throw new OADestroyed() ;
            invocationCount++;
        } finally {
            if (debug) {
                ORBUtility.dprint( this, "Exiting enter on poa " + this ) ;
            }
            unlock() ;
        }
        manager.enter();
    }
    public void exit()
    {
        try {
            lock() ;
            if (debug) {
                ORBUtility.dprint( this, "Calling exit on poa " + this ) ;
            }
            invocationCount--;
            if ((invocationCount == 0) && (state == STATE_DESTROYING)) {
                invokeCV.broadcast();
            }
        } finally {
            if (debug) {
                ORBUtility.dprint( this, "Exiting exit on poa " + this ) ;
            }
            unlock() ;
        }
        manager.exit();
    }
    public void getInvocationServant( OAInvocationInfo info )
    {
        try {
            lock() ;
            if (debug) {
                ORBUtility.dprint( this,
                    "Calling getInvocationServant on poa " + this ) ;
            }
            java.lang.Object servant = null ;
            try {
                servant = mediator.getInvocationServant( info.id(),
                    info.getOperation() );
            } catch (ForwardRequest freq) {
                throw new ForwardException( getORB(), freq.forward_reference ) ;
            }
            info.setServant( servant ) ;
        } finally {
            if (debug) {
                ORBUtility.dprint( this,
                    "Exiting getInvocationServant on poa " + this ) ;
            }
            unlock() ;
        }
    }
    public org.omg.CORBA.Object getLocalServant( byte[] objectId )
    {
        return null ;
    }
    public void returnServant()
    {
        try {
            lock() ;
            if (debug) {
                ORBUtility.dprint( this,
                    "Calling returnServant on poa " + this  ) ;
            }
            mediator.returnServant();
        } catch (Throwable thr) {
            if (debug) {
                ORBUtility.dprint( this,
                    "Exception " + thr + " in returnServant on poa " + this  ) ;
            }
            if (thr instanceof Error)
                throw (Error)thr ;
            else if (thr instanceof RuntimeException)
                throw (RuntimeException)thr ;
        } finally {
            if (debug) {
                ORBUtility.dprint( this,
                    "Exiting returnServant on poa " + this  ) ;
            }
            unlock() ;
        }
    }
}
