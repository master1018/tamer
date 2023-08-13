public abstract class POAPolicyMediatorBase_R extends POAPolicyMediatorBase {
    protected ActiveObjectMap activeObjectMap ;
    POAPolicyMediatorBase_R( Policies policies, POAImpl poa )
    {
        super( policies, poa ) ;
        if (!policies.retainServants())
            throw poa.invocationWrapper().policyMediatorBadPolicyInFactory() ;
        activeObjectMap = ActiveObjectMap.create(poa, !isUnique);
    }
    public void returnServant()
    {
    }
    public void clearAOM()
    {
        activeObjectMap.clear() ;
        activeObjectMap = null ;
    }
    protected Servant internalKeyToServant( ActiveObjectMap.Key key )
    {
        AOMEntry entry = activeObjectMap.get(key);
        if (entry == null)
            return null ;
        return activeObjectMap.getServant( entry ) ;
    }
    protected Servant internalIdToServant( byte[] id )
    {
        ActiveObjectMap.Key key = new ActiveObjectMap.Key( id ) ;
        return internalKeyToServant( key ) ;
    }
    protected void activateServant( ActiveObjectMap.Key key, AOMEntry entry, Servant servant )
    {
        setDelegate(servant, key.id );
        if (orb.shutdownDebugFlag) {
            System.out.println("Activating object " + servant +
                " with POA " + poa);
        }
        activeObjectMap.putServant( servant, entry ) ;
        if (Util.instance != null) {
            POAManagerImpl pm = (POAManagerImpl)poa.the_POAManager() ;
            POAFactory factory = pm.getFactory() ;
            factory.registerPOAForServant(poa, servant);
        }
    }
    public final void activateObject(byte[] id, Servant servant)
        throws WrongPolicy, ServantAlreadyActive, ObjectAlreadyActive
    {
        if (isUnique && activeObjectMap.contains(servant))
            throw new ServantAlreadyActive();
        ActiveObjectMap.Key key = new ActiveObjectMap.Key( id ) ;
        AOMEntry entry = activeObjectMap.get( key ) ;
        entry.activateObject() ;
        activateServant( key, entry, servant ) ;
    }
    public Servant deactivateObject( byte[] id )
        throws ObjectNotActive, WrongPolicy
    {
        ActiveObjectMap.Key key = new ActiveObjectMap.Key( id ) ;
        return deactivateObject( key ) ;
    }
    protected void deactivateHelper( ActiveObjectMap.Key key, AOMEntry entry,
        Servant s ) throws ObjectNotActive, WrongPolicy
    {
        activeObjectMap.remove(key);
        if (Util.instance != null) {
            POAManagerImpl pm = (POAManagerImpl)poa.the_POAManager() ;
            POAFactory factory = pm.getFactory() ;
            factory.unregisterPOAForServant(poa, s);
        }
    }
    public Servant deactivateObject( ActiveObjectMap.Key key )
        throws ObjectNotActive, WrongPolicy
    {
        if (orb.poaDebugFlag) {
            ORBUtility.dprint( this,
                "Calling deactivateObject for key " + key ) ;
        }
        try {
            AOMEntry entry = activeObjectMap.get(key);
            if (entry == null)
                throw new ObjectNotActive();
            Servant s = activeObjectMap.getServant( entry ) ;
            if (s == null)
                throw new ObjectNotActive();
            if (orb.poaDebugFlag) {
                System.out.println("Deactivating object " + s + " with POA " + poa);
            }
            deactivateHelper( key, entry, s ) ;
            return s ;
        } finally {
            if (orb.poaDebugFlag) {
                ORBUtility.dprint( this,
                    "Exiting deactivateObject" ) ;
            }
        }
    }
    public byte[] servantToId( Servant servant ) throws ServantNotActive, WrongPolicy
    {
        if (!isUnique && !isImplicit)
            throw new WrongPolicy();
        if (isUnique) {
            ActiveObjectMap.Key key = activeObjectMap.getKey(servant);
            if (key != null)
                return key.id ;
        }
        if (isImplicit)
            try {
                byte[] id = newSystemId() ;
                activateObject( id, servant ) ;
                return id ;
            } catch (ObjectAlreadyActive oaa) {
                throw poa.invocationWrapper().servantToIdOaa( oaa ) ;
            } catch (ServantAlreadyActive s) {
                throw poa.invocationWrapper().servantToIdSaa( s ) ;
            } catch (WrongPolicy w) {
                throw poa.invocationWrapper().servantToIdWp( w ) ;
            }
        throw new ServantNotActive();
    }
}
