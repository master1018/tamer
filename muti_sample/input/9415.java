public class DynamicStubImpl extends ObjectImpl
    implements DynamicStub, Serializable
{
    private static final long serialVersionUID = 4852612040012087675L;
    private String[] typeIds ;
    private StubIORImpl ior ;
    private DynamicStub self = null ;  
    public void setSelf( DynamicStub self )
    {
        this.self = self ;
    }
    public DynamicStub getSelf()
    {
        return self ;
    }
    public DynamicStubImpl( String[] typeIds )
    {
        this.typeIds = typeIds ;
        ior = null ;
    }
    public void setDelegate( Delegate delegate )
    {
        _set_delegate( delegate ) ;
    }
    public Delegate getDelegate()
    {
        return _get_delegate() ;
    }
    public ORB getORB()
    {
        return (ORB)_orb() ;
    }
    public String[] _ids()
    {
        return typeIds ;
    }
    public String[] getTypeIds()
    {
        return _ids() ;
    }
    public void connect( ORB orb ) throws RemoteException
    {
        ior = StubConnectImpl.connect( ior, self, this, orb ) ;
    }
    public boolean isLocal()
    {
        return _is_local() ;
    }
    public OutputStream request( String operation,
        boolean responseExpected )
    {
        return _request( operation, responseExpected ) ;
    }
    private void readObject( ObjectInputStream stream ) throws
        IOException, ClassNotFoundException
    {
        ior = new StubIORImpl() ;
        ior.doRead( stream ) ;
    }
    private void writeObject( ObjectOutputStream stream ) throws
        IOException
    {
        if (ior == null)
            ior = new StubIORImpl( this ) ;
        ior.doWrite( stream ) ;
    }
    public Object readResolve()
    {
        String repositoryId = ior.getRepositoryId() ;
        String cname = RepositoryId.cache.getId( repositoryId ).getClassName() ;
        Class cls = null ;
        try {
            cls = JDKBridge.loadClass( cname, null, null ) ;
        } catch (ClassNotFoundException exc) {
        }
        PresentationManager pm =
            com.sun.corba.se.spi.orb.ORB.getPresentationManager() ;
        PresentationManager.ClassData classData = pm.getClassData( cls ) ;
        InvocationHandlerFactoryImpl ihfactory =
            (InvocationHandlerFactoryImpl)classData.getInvocationHandlerFactory() ;
        return ihfactory.getInvocationHandler( this ) ;
    }
}
