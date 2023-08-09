public class POAPolicyMediatorImpl_R_USM extends POAPolicyMediatorBase_R {
    protected ServantActivator activator ;
    POAPolicyMediatorImpl_R_USM( Policies policies, POAImpl poa )
    {
        super( policies, poa ) ;
        activator = null ;
        if (!policies.useServantManager())
            throw poa.invocationWrapper().policyMediatorBadPolicyInFactory() ;
    }
    private AOMEntry enterEntry( ActiveObjectMap.Key key )
    {
        AOMEntry result = null ;
        boolean failed ;
        do {
            failed = false ;
            result = activeObjectMap.get(key) ;
            try {
                result.enter() ;
            } catch (Exception exc) {
                failed = true ;
            }
        } while (failed) ;
        return result ;
    }
    protected java.lang.Object internalGetServant( byte[] id,
        String operation ) throws ForwardRequest
    {
        if (poa.getDebug()) {
            ORBUtility.dprint( this,
                "Calling POAPolicyMediatorImpl_R_USM.internalGetServant " +
                "for poa " + poa + " operation=" + operation ) ;
        }
        try {
            ActiveObjectMap.Key key = new ActiveObjectMap.Key( id ) ;
            AOMEntry entry = enterEntry(key) ;
            java.lang.Object servant = activeObjectMap.getServant( entry ) ;
            if (servant != null) {
                if (poa.getDebug()) {
                    ORBUtility.dprint( this,
                        "internalGetServant: servant already activated" ) ;
                }
                return servant ;
            }
            if (activator == null) {
                if (poa.getDebug()) {
                    ORBUtility.dprint( this,
                        "internalGetServant: no servant activator in POA" ) ;
                }
                entry.incarnateFailure() ;
                throw poa.invocationWrapper().poaNoServantManager() ;
            }
            try {
                if (poa.getDebug()) {
                    ORBUtility.dprint( this,
                        "internalGetServant: upcall to incarnate" ) ;
                }
                poa.unlock() ;
                servant = activator.incarnate(id, poa);
                if (servant == null)
                    servant = new NullServantImpl(
                        poa.omgInvocationWrapper().nullServantReturned() ) ;
            } catch (ForwardRequest freq) {
                if (poa.getDebug()) {
                    ORBUtility.dprint( this,
                        "internalGetServant: incarnate threw ForwardRequest" ) ;
                }
                throw freq ;
            } catch (SystemException exc) {
                if (poa.getDebug()) {
                    ORBUtility.dprint( this,
                        "internalGetServant: incarnate threw SystemException " + exc ) ;
                }
                throw exc ;
            } catch (Throwable exc) {
                if (poa.getDebug()) {
                    ORBUtility.dprint( this,
                        "internalGetServant: incarnate threw Throwable " + exc ) ;
                }
                throw poa.invocationWrapper().poaServantActivatorLookupFailed(
                    exc ) ;
            } finally {
                poa.lock() ;
                if ((servant == null) || (servant instanceof NullServant)) {
                    if (poa.getDebug()) {
                        ORBUtility.dprint( this,
                            "internalGetServant: incarnate failed" ) ;
                    }
                    entry.incarnateFailure() ;
                } else {
                    if (isUnique) {
                        if (activeObjectMap.contains((Servant)servant)) {
                            if (poa.getDebug()) {
                                ORBUtility.dprint( this,
                                    "internalGetServant: servant already assigned to ID" ) ;
                            }
                            entry.incarnateFailure() ;
                            throw poa.invocationWrapper().poaServantNotUnique() ;
                        }
                    }
                    if (poa.getDebug()) {
                        ORBUtility.dprint( this,
                            "internalGetServant: incarnate complete" ) ;
                    }
                    entry.incarnateComplete() ;
                    activateServant(key, entry, (Servant)servant);
                }
            }
            return servant ;
        } finally {
            if (poa.getDebug()) {
                ORBUtility.dprint( this,
                    "Exiting POAPolicyMediatorImpl_R_USM.internalGetServant " +
                    "for poa " + poa ) ;
            }
        }
    }
    public void returnServant()
    {
        OAInvocationInfo info = orb.peekInvocationInfo();
        byte[] id = info.id() ;
        ActiveObjectMap.Key key = new ActiveObjectMap.Key( id ) ;
        AOMEntry entry = activeObjectMap.get( key ) ;
        entry.exit() ;
    }
    public void etherealizeAll()
    {
        if (activator != null)  {
            Set keySet = activeObjectMap.keySet() ;
            ActiveObjectMap.Key[] keys =
                (ActiveObjectMap.Key[])keySet.toArray(
                    new ActiveObjectMap.Key[ keySet.size() ] ) ;
            for (int ctr=0; ctr<keySet.size(); ctr++) {
                ActiveObjectMap.Key key = keys[ctr] ;
                AOMEntry entry = activeObjectMap.get( key ) ;
                Servant servant = activeObjectMap.getServant( entry ) ;
                if (servant != null) {
                    boolean remainingActivations =
                        activeObjectMap.hasMultipleIDs(entry) ;
                    entry.startEtherealize( null ) ;
                    try {
                        poa.unlock() ;
                        try {
                            activator.etherealize(key.id, poa, servant, true,
                                remainingActivations);
                        } catch (Exception exc) {
                        }
                    } finally {
                        poa.lock() ;
                        entry.etherealizeComplete() ;
                    }
                }
            }
        }
    }
    public ServantManager getServantManager() throws WrongPolicy
    {
        return activator;
    }
    public void setServantManager(
        ServantManager servantManager ) throws WrongPolicy
    {
        if (activator != null)
            throw poa.invocationWrapper().servantManagerAlreadySet() ;
        if (servantManager instanceof ServantActivator)
            activator = (ServantActivator)servantManager;
        else
            throw poa.invocationWrapper().servantManagerBadType() ;
    }
    public Servant getDefaultServant() throws NoServant, WrongPolicy
    {
        throw new WrongPolicy();
    }
    public void setDefaultServant( Servant servant ) throws WrongPolicy
    {
        throw new WrongPolicy();
    }
    class Etherealizer extends Thread {
        private POAPolicyMediatorImpl_R_USM mediator ;
        private ActiveObjectMap.Key key ;
        private AOMEntry entry ;
        private Servant servant ;
        private boolean debug ;
        public Etherealizer( POAPolicyMediatorImpl_R_USM mediator,
            ActiveObjectMap.Key key, AOMEntry entry, Servant servant,
            boolean debug )
        {
            this.mediator = mediator ;
            this.key = key ;
            this.entry = entry;
            this.servant = servant;
            this.debug = debug ;
        }
        public void run() {
            if (debug) {
                ORBUtility.dprint( this, "Calling Etherealizer.run on key " +
                    key ) ;
            }
            try {
                try {
                    mediator.activator.etherealize( key.id, mediator.poa, servant,
                        false, mediator.activeObjectMap.hasMultipleIDs( entry ) );
                } catch (Exception exc) {
                }
                try {
                    mediator.poa.lock() ;
                    entry.etherealizeComplete() ;
                    mediator.activeObjectMap.remove( key ) ;
                    POAManagerImpl pm = (POAManagerImpl)mediator.poa.the_POAManager() ;
                    POAFactory factory = pm.getFactory() ;
                    factory.unregisterPOAForServant( mediator.poa, servant);
                } finally {
                    mediator.poa.unlock() ;
                }
            } finally {
                if (debug) {
                    ORBUtility.dprint( this, "Exiting Etherealizer.run" ) ;
                }
            }
        }
    }
    public void deactivateHelper( ActiveObjectMap.Key key, AOMEntry entry,
        Servant servant ) throws ObjectNotActive, WrongPolicy
    {
        if (activator == null)
            throw poa.invocationWrapper().poaNoServantManager() ;
        Etherealizer eth = new Etherealizer( this, key, entry, servant, poa.getDebug() ) ;
        entry.startEtherealize( eth ) ;
    }
    public Servant idToServant( byte[] id )
        throws WrongPolicy, ObjectNotActive
    {
        ActiveObjectMap.Key key = new ActiveObjectMap.Key( id ) ;
        AOMEntry entry = activeObjectMap.get(key);
        Servant servant = activeObjectMap.getServant( entry ) ;
        if (servant != null)
            return servant ;
        else
            throw new ObjectNotActive() ;
    }
}
