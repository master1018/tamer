public class POAPolicyMediatorImpl_R_UDS extends POAPolicyMediatorBase_R {
    private Servant defaultServant ;
    POAPolicyMediatorImpl_R_UDS( Policies policies, POAImpl poa )
    {
        super( policies, poa ) ;
        defaultServant = null ;
        if (!policies.useDefaultServant())
            throw poa.invocationWrapper().policyMediatorBadPolicyInFactory() ;
    }
    protected java.lang.Object internalGetServant( byte[] id,
        String operation ) throws ForwardRequest
    {
        Servant servant = internalIdToServant( id ) ;
        if (servant == null)
            servant = defaultServant ;
        if (servant == null)
            throw poa.invocationWrapper().poaNoDefaultServant() ;
        return servant ;
    }
    public void etherealizeAll()
    {
    }
    public ServantManager getServantManager() throws WrongPolicy
    {
        throw new WrongPolicy();
    }
    public void setServantManager( ServantManager servantManager ) throws WrongPolicy
    {
        throw new WrongPolicy();
    }
    public Servant getDefaultServant() throws NoServant, WrongPolicy
    {
        if (defaultServant == null)
            throw new NoServant();
        else
            return defaultServant;
    }
    public void setDefaultServant( Servant servant ) throws WrongPolicy
    {
        defaultServant = servant;
        setDelegate(defaultServant, "DefaultServant".getBytes());
    }
    public Servant idToServant( byte[] id )
        throws WrongPolicy, ObjectNotActive
    {
        ActiveObjectMap.Key key = new ActiveObjectMap.Key( id ) ;
        Servant s = internalKeyToServant(key);
        if (s == null)
            if (defaultServant != null)
                s = defaultServant;
        if (s == null)
            throw new ObjectNotActive() ;
        return s;
    }
}
