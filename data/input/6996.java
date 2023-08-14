public class POAFactory implements ObjectAdapterFactory
{
    private Map exportedServantsToPOA = new WeakHashMap();
    private Set poaManagers ;
    private int poaManagerId ;
    private int poaId ;
    private POAImpl rootPOA ;
    private DelegateImpl delegateImpl;
    private ORB orb ;
    private POASystemException wrapper ;
    private OMGSystemException omgWrapper ;
    private boolean isShuttingDown = false;
    public POASystemException getWrapper()
    {
        return wrapper ;
    }
    public POAFactory()
    {
        poaManagers = Collections.synchronizedSet(new HashSet(4));
        poaManagerId = 0 ;
        poaId = 0 ;
        rootPOA = null ;
        delegateImpl = null ;
        orb = null ;
    }
    public synchronized POA lookupPOA (Servant servant)
    {
        return (POA)exportedServantsToPOA.get(servant);
    }
    public synchronized void registerPOAForServant(POA poa, Servant servant)
    {
        exportedServantsToPOA.put(servant, poa);
    }
    public synchronized void unregisterPOAForServant(POA poa, Servant servant)
    {
        exportedServantsToPOA.remove(servant);
    }
    public void init( ORB orb )
    {
        this.orb = orb ;
        wrapper = POASystemException.get( orb,
            CORBALogDomains.OA_LIFECYCLE ) ;
        omgWrapper = OMGSystemException.get( orb,
            CORBALogDomains.OA_LIFECYCLE ) ;
        delegateImpl = new DelegateImpl( orb, this ) ;
        registerRootPOA() ;
        POACurrent poaCurrent = new POACurrent(orb);
        orb.getLocalResolver().register( ORBConstants.POA_CURRENT_NAME,
            ClosureFactory.makeConstant( poaCurrent ) ) ;
    }
    public ObjectAdapter find( ObjectAdapterId oaid )
    {
        POA poa=null;
        try {
            boolean first = true ;
            Iterator iter = oaid.iterator() ;
            poa = getRootPOA();
            while (iter.hasNext()) {
                String name = (String)(iter.next()) ;
                if (first) {
                    if (!name.equals( ORBConstants.ROOT_POA_NAME ))
                        throw wrapper.makeFactoryNotPoa( name ) ;
                    first = false ;
                } else {
                    poa = poa.find_POA( name, true ) ;
                }
            }
        } catch ( org.omg.PortableServer.POAPackage.AdapterNonExistent ex ){
            throw omgWrapper.noObjectAdaptor( ex ) ;
        } catch ( OBJECT_NOT_EXIST ex ) {
            throw ex;
        } catch ( TRANSIENT ex ) {
            throw ex;
        } catch ( Exception ex ) {
            throw wrapper.poaLookupError( ex ) ;
        }
        if ( poa == null )
            throw wrapper.poaLookupError() ;
        return (ObjectAdapter)poa;
    }
    public void shutdown( boolean waitForCompletion )
    {
        Iterator managers = null ;
        synchronized (this) {
            isShuttingDown = true ;
            managers = (new HashSet(poaManagers)).iterator();
        }
        while ( managers.hasNext() ) {
            try {
                ((POAManager)managers.next()).deactivate(true, waitForCompletion);
            } catch ( org.omg.PortableServer.POAManagerPackage.AdapterInactive e ) {}
        }
    }
    public synchronized void removePoaManager( POAManager manager )
    {
        poaManagers.remove(manager);
    }
    public synchronized void addPoaManager( POAManager manager )
    {
        poaManagers.add(manager);
    }
    synchronized public int newPOAManagerId()
    {
        return poaManagerId++ ;
    }
    public void registerRootPOA()
    {
        Closure rpClosure = new Closure() {
            public Object evaluate() {
                return POAImpl.makeRootPOA( orb ) ;
            }
        } ;
        orb.getLocalResolver().register( ORBConstants.ROOT_POA_NAME,
            ClosureFactory.makeFuture( rpClosure ) ) ;
    }
    public synchronized POA getRootPOA()
    {
        if (rootPOA == null) {
            if (isShuttingDown) {
                throw omgWrapper.noObjectAdaptor( ) ;
            }
            try {
                Object obj = orb.resolve_initial_references(
                    ORBConstants.ROOT_POA_NAME ) ;
                rootPOA = (POAImpl)obj ;
            } catch (InvalidName inv) {
                throw wrapper.cantResolveRootPoa( inv ) ;
            }
        }
        return rootPOA;
    }
    public org.omg.PortableServer.portable.Delegate getDelegateImpl()
    {
        return delegateImpl ;
    }
    synchronized public int newPOAId()
    {
        return poaId++ ;
    }
    public ORB getORB()
    {
        return orb ;
    }
}
