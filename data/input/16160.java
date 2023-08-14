public class ORBImpl extends com.sun.corba.se.spi.orb.ORB
{
    protected TransportManager transportManager;
    protected LegacyServerSocketManager legacyServerSocketManager;
    private ThreadLocal OAInvocationInfoStack ;
    private ThreadLocal clientInvocationInfoStack ;
    private static IOR codeBaseIOR ;
    private Vector            dynamicRequests ;
    private SynchVariable     svResponseReceived ;
    private java.lang.Object runObj = new java.lang.Object();
    private java.lang.Object shutdownObj = new java.lang.Object();
    private static final byte STATUS_OPERATING = 1;
    private static final byte STATUS_SHUTTING_DOWN = 2;
    private static final byte STATUS_SHUTDOWN = 3;
    private static final byte STATUS_DESTROYED = 4;
    private byte status = STATUS_OPERATING;
    private java.lang.Object invocationObj = new java.lang.Object();
    private ThreadLocal isProcessingInvocation = new ThreadLocal () {
        protected java.lang.Object initialValue() {
            return Boolean.FALSE;
        }
    };
    private Map typeCodeForClassMap ;
    private Hashtable valueFactoryCache = new Hashtable();
    private ThreadLocal orbVersionThreadLocal ;
    private RequestDispatcherRegistry requestDispatcherRegistry ;
    private CopierManager copierManager ;
    private int transientServerId ;
    private ThreadGroup threadGroup ;
    private ServiceContextRegistry serviceContextRegistry ;
    private TOAFactory toaFactory ;
    private POAFactory poaFactory ;
    private PIHandler pihandler ;
    private ORBData configData ;
    private BadServerIdHandler badServerIdHandler ;
    private ClientDelegateFactory clientDelegateFactory ;
    private CorbaContactInfoListFactory corbaContactInfoListFactory ;
    private Resolver resolver ;
    private LocalResolver localResolver ;
    private Operation urlOperation ;
    private final Object urlOperationLock = new java.lang.Object() ;
    private CorbaServerRequestDispatcher insNamingDelegate ;
    private final Object resolverLock = new Object() ;
    private TaggedComponentFactoryFinder taggedComponentFactoryFinder ;
    private IdentifiableFactoryFinder taggedProfileFactoryFinder ;
    private IdentifiableFactoryFinder taggedProfileTemplateFactoryFinder ;
    private ObjectKeyFactory objectKeyFactory ;
    private ThreadPoolManager threadpoolMgr;
    private void dprint( String msg )
    {
        ORBUtility.dprint( this, msg ) ;
    }
    public ORBData getORBData()
    {
        return configData ;
    }
    public PIHandler getPIHandler()
    {
        return pihandler ;
    }
    public ORBImpl()
    {
    }
    public ORBVersion getORBVersion()
    {
        return (ORBVersion)(orbVersionThreadLocal.get()) ;
    }
    public void setORBVersion(ORBVersion verObj)
    {
        orbVersionThreadLocal.set(verObj);
    }
    private void preInit( String[] params, Properties props )
    {
        pihandler = new PINoOpHandlerImpl( );
        try {
            threadGroup = (ThreadGroup) AccessController.doPrivileged(
                new PrivilegedAction() {
                    public Object run() {
                        ThreadGroup tg = Thread.currentThread().getThreadGroup() ;
                        ThreadGroup ptg = tg ;
                        try {
                            while (ptg != null) {
                                tg = ptg;
                                ptg = tg.getParent();
                            }
                        } catch (SecurityException se) {
                        }
                        return new ThreadGroup(tg, "ORB ThreadGroup");
                    }
                }
            );
        } catch (SecurityException e) {
            threadGroup = Thread.currentThread().getThreadGroup();
        }
        transientServerId = (int)System.currentTimeMillis();
        orbVersionThreadLocal  = new ThreadLocal () {
            protected java.lang.Object initialValue() {
                return ORBVersionFactory.getORBVersion() ;
            }
        };
        requestDispatcherRegistry = new RequestDispatcherRegistryImpl(
            this, ORBConstants.DEFAULT_SCID);
        copierManager = new CopierManagerImpl( this ) ;
        taggedComponentFactoryFinder =
            new TaggedComponentFactoryFinderImpl(this) ;
        taggedProfileFactoryFinder =
            new TaggedProfileFactoryFinderImpl(this) ;
        taggedProfileTemplateFactoryFinder =
            new TaggedProfileTemplateFactoryFinderImpl(this) ;
        dynamicRequests = new Vector();
        svResponseReceived = new SynchVariable();
        OAInvocationInfoStack =
            new ThreadLocal () {
                protected java.lang.Object initialValue()
                {
                    return new StackImpl();
                }
            };
        clientInvocationInfoStack =
            new ThreadLocal() {
                protected java.lang.Object initialValue() {
                    return new StackImpl();
                }
            };
        serviceContextRegistry = new ServiceContextRegistry( this ) ;
    }
    protected void setDebugFlags( String[] args )
    {
        for (int ctr=0; ctr<args.length; ctr++ ) {
            String token = args[ctr] ;
            try {
                Field fld = this.getClass().getField( token + "DebugFlag" ) ;
                int mod = fld.getModifiers() ;
                if (Modifier.isPublic( mod ) && !Modifier.isStatic( mod ))
                    if (fld.getType() == boolean.class)
                        fld.setBoolean( this, true ) ;
            } catch (Exception exc) {
            }
        }
    }
    private static class ConfigParser extends ParserImplBase {
        public Class configurator = ORBConfiguratorImpl.class ;
        public PropertyParser makeParser()
        {
            PropertyParser parser = new PropertyParser() ;
            parser.add( ORBConstants.SUN_PREFIX + "ORBConfigurator",
                OperationFactory.classAction(), "configurator" ) ;
            return parser ;
        }
    }
    private void postInit( String[] params, DataCollector dataCollector )
    {
        configData = new ORBDataParserImpl( this, dataCollector) ;
        setDebugFlags( configData.getORBDebugFlags() ) ;
        getTransportManager();
        getLegacyServerSocketManager();
        ConfigParser parser = new ConfigParser() ;
        parser.init( dataCollector ) ;
        ORBConfigurator configurator =  null ;
        try {
            configurator =
                (ORBConfigurator)(parser.configurator.newInstance()) ;
        } catch (Exception iexc) {
            throw wrapper.badOrbConfigurator( iexc, parser.configurator.getName() ) ;
        }
        try {
            configurator.configure( dataCollector, this ) ;
        } catch (Exception exc) {
            throw wrapper.orbConfiguratorError( exc ) ;
        }
        pihandler = new PIHandlerImpl( this, params) ;
        pihandler.initialize() ;
        getThreadPoolManager();
        super.getByteBufferPool();
    }
    private synchronized POAFactory getPOAFactory()
    {
        if (poaFactory == null) {
            poaFactory = (POAFactory)requestDispatcherRegistry.getObjectAdapterFactory(
                ORBConstants.TRANSIENT_SCID ) ;
        }
        return poaFactory ;
    }
    private synchronized TOAFactory getTOAFactory()
    {
        if (toaFactory == null) {
            toaFactory = (TOAFactory)requestDispatcherRegistry.getObjectAdapterFactory(
                ORBConstants.TOA_SCID ) ;
        }
        return toaFactory ;
    }
    public void set_parameters( Properties props )
    {
        preInit( null, props ) ;
        DataCollector dataCollector =
            DataCollectorFactory.create( props, getLocalHostName() ) ;
        postInit( null, dataCollector ) ;
    }
    protected void set_parameters(Applet app, Properties props)
    {
        preInit( null, props ) ;
        DataCollector dataCollector =
            DataCollectorFactory.create( app, props, getLocalHostName() ) ;
        postInit( null, dataCollector ) ;
    }
    protected void set_parameters (String[] params, Properties props)
    {
        preInit( params, props ) ;
        DataCollector dataCollector =
            DataCollectorFactory.create( params, props, getLocalHostName() ) ;
        postInit( params, dataCollector ) ;
    }
    public synchronized org.omg.CORBA.portable.OutputStream create_output_stream()
    {
        checkShutdownState();
        return new EncapsOutputStream(this);
    }
    public synchronized org.omg.CORBA.Current get_current()
    {
        checkShutdownState();
        throw wrapper.genericNoImpl() ;
    }
    public synchronized NVList create_list(int count)
    {
        checkShutdownState();
        return new NVListImpl(this, count);
    }
    public synchronized NVList create_operation_list(org.omg.CORBA.Object oper)
    {
        checkShutdownState();
        throw wrapper.genericNoImpl() ;
    }
    public synchronized NamedValue create_named_value(String s, Any any, int flags)
    {
        checkShutdownState();
        return new NamedValueImpl(this, s, any, flags);
    }
    public synchronized org.omg.CORBA.ExceptionList create_exception_list()
    {
        checkShutdownState();
        return new ExceptionListImpl();
    }
    public synchronized org.omg.CORBA.ContextList create_context_list()
    {
        checkShutdownState();
        return new ContextListImpl(this);
    }
    public synchronized org.omg.CORBA.Context get_default_context()
    {
        checkShutdownState();
        throw wrapper.genericNoImpl() ;
    }
    public synchronized org.omg.CORBA.Environment create_environment()
    {
        checkShutdownState();
        return new EnvironmentImpl();
    }
    public synchronized void send_multiple_requests_oneway(Request[] req)
    {
        checkShutdownState();
        for (int i = 0; i < req.length; i++) {
            req[i].send_oneway();
        }
    }
    public synchronized void send_multiple_requests_deferred(Request[] req)
    {
        checkShutdownState();
        for (int i = 0; i < req.length; i++) {
            dynamicRequests.addElement(req[i]);
        }
        for (int i = 0; i < req.length; i++) {
            AsynchInvoke invokeObject = new AsynchInvoke( this,
                (com.sun.corba.se.impl.corba.RequestImpl)req[i], true);
            new Thread(invokeObject).start();
        }
    }
    public synchronized boolean poll_next_response()
    {
        checkShutdownState();
        Request currRequest;
        Enumeration ve = dynamicRequests.elements();
        while (ve.hasMoreElements() == true) {
            currRequest = (Request)ve.nextElement();
            if (currRequest.poll_response() == true) {
                return true;
            }
        }
        return false;
    }
    public org.omg.CORBA.Request get_next_response()
        throws org.omg.CORBA.WrongTransaction
    {
        synchronized( this ) {
            checkShutdownState();
        }
        while (true) {
            synchronized ( dynamicRequests ) {
                Enumeration elems = dynamicRequests.elements();
                while ( elems.hasMoreElements() ) {
                    Request currRequest = (Request)elems.nextElement();
                    if ( currRequest.poll_response() ) {
                        currRequest.get_response();
                        dynamicRequests.removeElement(currRequest);
                        return currRequest;
                    }
                }
            }
            synchronized(this.svResponseReceived) {
                while (!this.svResponseReceived.value()) {
                    try {
                        this.svResponseReceived.wait();
                    } catch(java.lang.InterruptedException ex) {
                    }
                }
                this.svResponseReceived.reset();
            }
        }
    }
    public void notifyORB()
    {
        synchronized (this.svResponseReceived) {
            this.svResponseReceived.set();
            this.svResponseReceived.notify();
        }
    }
    public synchronized String object_to_string(org.omg.CORBA.Object obj)
    {
        checkShutdownState();
        if (obj == null) {
            IOR nullIOR = IORFactories.makeIOR( this ) ;
            return nullIOR.stringify();
        }
        IOR ior = null ;
        try {
            ior = ORBUtility.connectAndGetIOR( this, obj ) ;
        } catch (BAD_PARAM bp) {
            if (bp.minor == ORBUtilSystemException.LOCAL_OBJECT_NOT_ALLOWED) {
                throw omgWrapper.notAnObjectImpl( bp ) ;
            } else
                throw bp ;
        }
        return ior.stringify() ;
    }
    public org.omg.CORBA.Object string_to_object(String str)
    {
        Operation op ;
        synchronized (this) {
            checkShutdownState();
            op = urlOperation ;
        }
        if (str == null)
            throw wrapper.nullParam() ;
        synchronized (urlOperationLock) {
            org.omg.CORBA.Object obj = (org.omg.CORBA.Object)op.operate( str ) ;
            return obj ;
        }
    }
    public synchronized IOR getFVDCodeBaseIOR()
    {
        if (codeBaseIOR != null) 
            return codeBaseIOR;
        CodeBase cb;
        ValueHandler vh = ORBUtility.createValueHandler(this);
        cb = (CodeBase)vh.getRunTimeCodeBase();
        return ORBUtility.connectAndGetIOR( this, cb ) ;
    }
    public synchronized TypeCode get_primitive_tc(TCKind tcKind)
    {
        checkShutdownState();
        return get_primitive_tc( tcKind.value() ) ;
    }
    public synchronized TypeCode create_struct_tc(String id,
                                     String name,
                                     StructMember[] members)
    {
        checkShutdownState();
        return new TypeCodeImpl(this, TCKind._tk_struct, id, name, members);
    }
    public synchronized TypeCode create_union_tc(String id,
                                    String name,
                                    TypeCode discriminator_type,
                                    UnionMember[] members)
    {
        checkShutdownState();
        return new TypeCodeImpl(this,
                                TCKind._tk_union,
                                id,
                                name,
                                discriminator_type,
                                members);
    }
    public synchronized TypeCode create_enum_tc(String id,
                                   String name,
                                   String[] members)
    {
        checkShutdownState();
        return new TypeCodeImpl(this, TCKind._tk_enum, id, name, members);
    }
    public synchronized TypeCode create_alias_tc(String id,
                                    String name,
                                    TypeCode original_type)
    {
        checkShutdownState();
        return new TypeCodeImpl(this, TCKind._tk_alias, id, name, original_type);
    }
    public synchronized TypeCode create_exception_tc(String id,
                                        String name,
                                        StructMember[] members)
    {
        checkShutdownState();
        return new TypeCodeImpl(this, TCKind._tk_except, id, name, members);
    }
    public synchronized TypeCode create_interface_tc(String id,
                                        String name)
    {
        checkShutdownState();
        return new TypeCodeImpl(this, TCKind._tk_objref, id, name);
    }
    public synchronized TypeCode create_string_tc(int bound)
    {
        checkShutdownState();
        return new TypeCodeImpl(this, TCKind._tk_string, bound);
    }
    public synchronized TypeCode create_wstring_tc(int bound)
    {
        checkShutdownState();
        return new TypeCodeImpl(this, TCKind._tk_wstring, bound);
    }
    public synchronized TypeCode create_sequence_tc(int bound,
                                       TypeCode element_type)
    {
        checkShutdownState();
        return new TypeCodeImpl(this, TCKind._tk_sequence, bound, element_type);
    }
    public synchronized TypeCode create_recursive_sequence_tc(int bound,
                                                 int offset)
    {
        checkShutdownState();
        return new TypeCodeImpl(this, TCKind._tk_sequence, bound, offset);
    }
    public synchronized TypeCode create_array_tc(int length,
                                    TypeCode element_type)
    {
        checkShutdownState();
        return new TypeCodeImpl(this, TCKind._tk_array, length, element_type);
    }
    public synchronized org.omg.CORBA.TypeCode create_native_tc(String id,
                                                   String name)
    {
        checkShutdownState();
        return new TypeCodeImpl(this, TCKind._tk_native, id, name);
    }
    public synchronized org.omg.CORBA.TypeCode create_abstract_interface_tc(
                                                               String id,
                                                               String name)
    {
        checkShutdownState();
        return new TypeCodeImpl(this, TCKind._tk_abstract_interface, id, name);
    }
    public synchronized org.omg.CORBA.TypeCode create_fixed_tc(short digits, short scale)
    {
        checkShutdownState();
        return new TypeCodeImpl(this, TCKind._tk_fixed, digits, scale);
    }
    public synchronized org.omg.CORBA.TypeCode create_value_tc(String id,
                                                  String name,
                                                  short type_modifier,
                                                  TypeCode concrete_base,
                                                  ValueMember[] members)
    {
        checkShutdownState();
        return new TypeCodeImpl(this, TCKind._tk_value, id, name,
                                type_modifier, concrete_base, members);
    }
    public synchronized org.omg.CORBA.TypeCode create_recursive_tc(String id) {
        checkShutdownState();
        return new TypeCodeImpl(this, id);
    }
    public synchronized org.omg.CORBA.TypeCode create_value_box_tc(String id,
                                                      String name,
                                                      TypeCode boxed_type)
    {
        checkShutdownState();
        return new TypeCodeImpl(this, TCKind._tk_value_box, id, name,
            boxed_type);
    }
    public synchronized Any create_any()
    {
        checkShutdownState();
        return new AnyImpl(this);
    }
    public synchronized void setTypeCodeForClass(Class c, TypeCodeImpl tci)
    {
        if (typeCodeForClassMap == null)
            typeCodeForClassMap = Collections.synchronizedMap(
                new WeakHashMap(64));
        if ( ! typeCodeForClassMap.containsKey(c))
            typeCodeForClassMap.put(c, tci);
    }
    public synchronized TypeCodeImpl getTypeCodeForClass(Class c)
    {
        if (typeCodeForClassMap == null)
            return null;
        return (TypeCodeImpl)typeCodeForClassMap.get(c);
    }
    public String[] list_initial_services()
    {
        Resolver res ;
        synchronized( this ) {
            checkShutdownState();
            res = resolver ;
        }
        synchronized (resolverLock) {
            java.util.Set keys = res.list() ;
            return (String[])keys.toArray( new String[keys.size()] ) ;
        }
    }
    public org.omg.CORBA.Object resolve_initial_references(
        String identifier) throws InvalidName
    {
        Resolver res ;
        synchronized( this ) {
            checkShutdownState();
            res = resolver ;
        }
        synchronized (resolverLock) {
            org.omg.CORBA.Object result = res.resolve( identifier ) ;
            if (result == null)
                throw new InvalidName() ;
            else
                return result ;
        }
    }
    public void register_initial_reference(
        String id, org.omg.CORBA.Object obj ) throws InvalidName
    {
        CorbaServerRequestDispatcher insnd ;
        if ((id == null) || (id.length() == 0))
            throw new InvalidName() ;
        synchronized (this) {
            checkShutdownState();
        }
        synchronized (resolverLock) {
            insnd = insNamingDelegate ;
            java.lang.Object obj2 = localResolver.resolve( id ) ;
            if (obj2 != null)
                throw new InvalidName(id + " already registered") ;
            localResolver.register( id, ClosureFactory.makeConstant( obj )) ;
        }
        synchronized (this) {
            if (StubAdapter.isStub(obj))
                requestDispatcherRegistry.registerServerRequestDispatcher(
                    insnd, id ) ;
        }
    }
    public void run()
    {
        synchronized (this) {
            checkShutdownState();
        }
        synchronized (runObj) {
            try {
                runObj.wait();
            } catch ( InterruptedException ex ) {}
        }
    }
    public void shutdown(boolean wait_for_completion)
    {
        if (wait_for_completion && ((Boolean)isProcessingInvocation.get()).booleanValue()) {
            throw omgWrapper.shutdownWaitForCompletionDeadlock() ;
        }
        boolean doShutdown = false ;
        synchronized (this) {
            checkShutdownState() ;
            if (status == STATUS_SHUTTING_DOWN) {
                if (!wait_for_completion)
                return ;
            } else {
                status = STATUS_SHUTTING_DOWN ;
                doShutdown = true ;
            }
        }
        synchronized (shutdownObj) {
            if (doShutdown) {
                shutdownServants(wait_for_completion);
                synchronized (runObj) {
                    runObj.notifyAll();
                }
                synchronized (this) {
                    status = STATUS_SHUTDOWN;
                }
            }
        }
    }
    protected void shutdownServants(boolean wait_for_completion) {
        Iterator iter = requestDispatcherRegistry.getObjectAdapterFactories().iterator() ;
        while (iter.hasNext()) {
            ObjectAdapterFactory oaf = (ObjectAdapterFactory)iter.next() ;
            oaf.shutdown( wait_for_completion ) ;
        }
    }
    public void checkShutdownState()
    {
        if (status == STATUS_DESTROYED) {
            throw wrapper.orbDestroyed() ;
        }
        if (status == STATUS_SHUTDOWN) {
            throw omgWrapper.badOperationAfterShutdown() ;
        }
    }
    public boolean isDuringDispatch()
    {
        Boolean value = (Boolean)(isProcessingInvocation.get()) ;
        return value.booleanValue() ;
    }
    public void startingDispatch()
    {
        synchronized (invocationObj) {
            isProcessingInvocation.set(Boolean.TRUE);
        }
    }
    public void finishedDispatch()
    {
        synchronized (invocationObj) {
            isProcessingInvocation.set(Boolean.FALSE);
        }
    }
    public synchronized void destroy()
    {
        boolean shutdownFirst = false ;
        synchronized (this) {
            shutdownFirst = (status == STATUS_OPERATING) ;
        }
        if (shutdownFirst) {
            shutdown(true);
        }
        synchronized (this) {
            if (status < STATUS_DESTROYED) {
                getCorbaTransportManager().close();
                getPIHandler().destroyInterceptors() ;
                status = STATUS_DESTROYED;
            }
        }
    }
    public synchronized ValueFactory register_value_factory(String repositoryID,
        ValueFactory factory)
    {
        checkShutdownState();
        if ((repositoryID == null) || (factory == null))
            throw omgWrapper.unableRegisterValueFactory() ;
        return (ValueFactory)valueFactoryCache.put(repositoryID, factory);
    }
    public synchronized void unregister_value_factory(String repositoryID)
    {
        checkShutdownState();
        if (valueFactoryCache.remove(repositoryID) == null)
            throw wrapper.nullParam() ;
    }
    public synchronized ValueFactory lookup_value_factory(String repositoryID)
    {
        checkShutdownState();
        ValueFactory factory =
            (ValueFactory)valueFactoryCache.get(repositoryID);
        if (factory == null) {
            try {
                factory = Utility.getFactory(null, null, null, repositoryID);
            } catch(org.omg.CORBA.MARSHAL ex) {
                throw wrapper.unableFindValueFactory( ex ) ;
            }
        }
        return factory ;
    }
    public OAInvocationInfo peekInvocationInfo()
    {
        StackImpl stack = (StackImpl)(OAInvocationInfoStack.get()) ;
        return (OAInvocationInfo)(stack.peek()) ;
    }
    public void pushInvocationInfo( OAInvocationInfo info )
    {
        StackImpl stack = (StackImpl)(OAInvocationInfoStack.get()) ;
        stack.push( info ) ;
    }
    public OAInvocationInfo popInvocationInfo()
    {
        StackImpl stack = (StackImpl)(OAInvocationInfoStack.get()) ;
        return (OAInvocationInfo)(stack.pop()) ;
    }
    private Object badServerIdHandlerAccessLock = new Object();
    public void initBadServerIdHandler()
    {
        synchronized (badServerIdHandlerAccessLock) {
            Class cls = configData.getBadServerIdHandler() ;
            if (cls != null) {
                try {
                    Class[] params = new Class[] { org.omg.CORBA.ORB.class };
                    java.lang.Object[] args = new java.lang.Object[]{this};
                    Constructor cons = cls.getConstructor(params);
                    badServerIdHandler =
                        (BadServerIdHandler) cons.newInstance(args);
                } catch (Exception e) {
                    throw wrapper.errorInitBadserveridhandler( e ) ;
                }
            }
        }
    }
    public void setBadServerIdHandler( BadServerIdHandler handler )
    {
        synchronized (badServerIdHandlerAccessLock) {
            badServerIdHandler = handler;
        }
    }
    public void handleBadServerId( ObjectKey okey )
    {
        synchronized (badServerIdHandlerAccessLock) {
            if (badServerIdHandler == null)
                throw wrapper.badServerId() ;
            else
                badServerIdHandler.handle( okey ) ;
        }
    }
    public synchronized org.omg.CORBA.Policy create_policy( int type,
        org.omg.CORBA.Any val ) throws org.omg.CORBA.PolicyError
    {
        checkShutdownState() ;
        return pihandler.create_policy( type, val ) ;
    }
    public synchronized void connect(org.omg.CORBA.Object servant)
    {
        checkShutdownState();
        if (getTOAFactory() == null)
            throw wrapper.noToa() ;
        try {
            String codebase = javax.rmi.CORBA.Util.getCodebase( servant.getClass() ) ;
            getTOAFactory().getTOA( codebase ).connect( servant ) ;
        } catch ( Exception ex ) {
            throw wrapper.orbConnectError( ex ) ;
        }
    }
    public synchronized void disconnect(org.omg.CORBA.Object obj)
    {
        checkShutdownState();
        if (getTOAFactory() == null)
            throw wrapper.noToa() ;
        try {
            getTOAFactory().getTOA().disconnect( obj ) ;
        } catch ( Exception ex ) {
            throw wrapper.orbConnectError( ex ) ;
        }
    }
    public int getTransientServerId()
    {
        if( configData.getORBServerIdPropertySpecified( ) ) {
            return configData.getPersistentServerId( );
        }
        return transientServerId;
    }
    public RequestDispatcherRegistry getRequestDispatcherRegistry()
    {
        return requestDispatcherRegistry;
    }
    public ServiceContextRegistry getServiceContextRegistry()
    {
        return serviceContextRegistry ;
    }
    public boolean isLocalHost( String hostName )
    {
        return hostName.equals( configData.getORBServerHost() ) ||
            hostName.equals( getLocalHostName() ) ;
    }
    public boolean isLocalServerId( int subcontractId, int serverId )
    {
        if ((subcontractId < ORBConstants.FIRST_POA_SCID) ||
            (subcontractId > ORBConstants.MAX_POA_SCID))
            return serverId == getTransientServerId( ) ;
        if (ORBConstants.isTransient( subcontractId ))
            return (serverId == getTransientServerId()) ;
        else if (configData.getPersistentServerIdInitialized())
            return (serverId == configData.getPersistentServerId()) ;
        else
            return false ;
    }
    private String getHostName(String host)
        throws java.net.UnknownHostException
    {
        return InetAddress.getByName( host ).getHostAddress();
    }
    private static String localHostString = null;
    private synchronized String getLocalHostName()
    {
        if (localHostString == null) {
            try {
                localHostString = InetAddress.getLocalHost().getHostAddress();
            } catch (Exception ex) {
                throw wrapper.getLocalHostFailed( ex ) ;
            }
        }
        return localHostString ;
    }
    public synchronized boolean work_pending()
    {
        checkShutdownState();
        throw wrapper.genericNoImpl() ;
    }
    public synchronized void perform_work()
    {
        checkShutdownState();
        throw wrapper.genericNoImpl() ;
    }
    public synchronized void set_delegate(java.lang.Object servant){
        checkShutdownState();
        POAFactory poaFactory = getPOAFactory() ;
        if (poaFactory != null)
            ((org.omg.PortableServer.Servant)servant)
                ._set_delegate( poaFactory.getDelegateImpl() ) ;
        else
            throw wrapper.noPoa() ;
    }
    public ClientInvocationInfo createOrIncrementInvocationInfo()
    {
        StackImpl invocationInfoStack =
            (StackImpl) clientInvocationInfoStack.get();
        ClientInvocationInfo clientInvocationInfo = null;
        if (!invocationInfoStack.empty()) {
            clientInvocationInfo =
                (ClientInvocationInfo) invocationInfoStack.peek();
        }
        if ((clientInvocationInfo == null) ||
            (!clientInvocationInfo.isRetryInvocation()))
        {
            clientInvocationInfo = new CorbaInvocationInfo(this);
            startingDispatch();
            invocationInfoStack.push(clientInvocationInfo);
        }
        clientInvocationInfo.setIsRetryInvocation(false);
        clientInvocationInfo.incrementEntryCount();
        return clientInvocationInfo;
    }
    public void releaseOrDecrementInvocationInfo()
    {
        StackImpl invocationInfoStack =
            (StackImpl)clientInvocationInfoStack.get();
        int entryCount = -1;
        ClientInvocationInfo clientInvocationInfo = null;
        if (!invocationInfoStack.empty()) {
            clientInvocationInfo =
                (ClientInvocationInfo)invocationInfoStack.peek();
        } else {
            throw wrapper.invocationInfoStackEmpty() ;
        }
        clientInvocationInfo.decrementEntryCount();
        entryCount = clientInvocationInfo.getEntryCount();
        if (clientInvocationInfo.getEntryCount() == 0) {
            if (!clientInvocationInfo.isRetryInvocation()) {
                invocationInfoStack.pop();
            }
            finishedDispatch();
        }
    }
    public ClientInvocationInfo getInvocationInfo()
    {
        StackImpl invocationInfoStack =
            (StackImpl) clientInvocationInfoStack.get();
        return (ClientInvocationInfo) invocationInfoStack.peek();
    }
    private Object clientDelegateFactoryAccessorLock = new Object();
    public void setClientDelegateFactory( ClientDelegateFactory factory )
    {
        synchronized (clientDelegateFactoryAccessorLock) {
            clientDelegateFactory = factory ;
        }
    }
    public ClientDelegateFactory getClientDelegateFactory()
    {
        synchronized (clientDelegateFactoryAccessorLock) {
            return clientDelegateFactory ;
        }
    }
    private Object corbaContactInfoListFactoryAccessLock = new Object();
    public void setCorbaContactInfoListFactory( CorbaContactInfoListFactory factory )
    {
        synchronized (corbaContactInfoListFactoryAccessLock) {
            corbaContactInfoListFactory = factory ;
        }
    }
    public synchronized CorbaContactInfoListFactory getCorbaContactInfoListFactory()
    {
        return corbaContactInfoListFactory ;
    }
    public void setResolver( Resolver resolver )
    {
        synchronized (resolverLock) {
            this.resolver = resolver ;
        }
    }
    public Resolver getResolver()
    {
        synchronized (resolverLock) {
            return resolver ;
        }
    }
    public void setLocalResolver( LocalResolver resolver )
    {
        synchronized (resolverLock) {
            this.localResolver = resolver ;
        }
    }
    public LocalResolver getLocalResolver()
    {
        synchronized (resolverLock) {
            return localResolver ;
        }
    }
    public void setURLOperation( Operation stringToObject )
    {
        synchronized (urlOperationLock) {
            urlOperation = stringToObject ;
        }
    }
    public Operation getURLOperation()
    {
        synchronized (urlOperationLock) {
            return urlOperation ;
        }
    }
    public void setINSDelegate( CorbaServerRequestDispatcher sdel )
    {
        synchronized (resolverLock) {
            insNamingDelegate = sdel ;
        }
    }
    public TaggedComponentFactoryFinder getTaggedComponentFactoryFinder()
    {
        return taggedComponentFactoryFinder ;
    }
    public IdentifiableFactoryFinder getTaggedProfileFactoryFinder()
    {
        return taggedProfileFactoryFinder ;
    }
    public IdentifiableFactoryFinder getTaggedProfileTemplateFactoryFinder()
    {
        return taggedProfileTemplateFactoryFinder ;
    }
    private Object objectKeyFactoryAccessLock = new Object();
    public ObjectKeyFactory getObjectKeyFactory()
    {
        synchronized (objectKeyFactoryAccessLock) {
            return objectKeyFactory ;
        }
    }
    public void setObjectKeyFactory( ObjectKeyFactory factory )
    {
        synchronized (objectKeyFactoryAccessLock) {
            objectKeyFactory = factory ;
        }
    }
    private Object transportManagerAccessorLock = new Object();
    public TransportManager getTransportManager()
    {
        synchronized (transportManagerAccessorLock) {
            if (transportManager == null) {
                transportManager = new CorbaTransportManagerImpl(this);
            }
            return transportManager;
        }
    }
    public CorbaTransportManager getCorbaTransportManager()
    {
        return (CorbaTransportManager) getTransportManager();
    }
    private Object legacyServerSocketManagerAccessLock = new Object();
    public LegacyServerSocketManager getLegacyServerSocketManager()
    {
        synchronized (legacyServerSocketManagerAccessLock) {
            if (legacyServerSocketManager == null) {
                legacyServerSocketManager = new LegacyServerSocketManagerImpl(this);
            }
            return legacyServerSocketManager;
        }
    }
    private Object threadPoolManagerAccessLock = new Object();
    public void setThreadPoolManager(ThreadPoolManager mgr)
    {
        synchronized (threadPoolManagerAccessLock) {
            threadpoolMgr = mgr;
        }
    }
    public ThreadPoolManager getThreadPoolManager()
    {
        synchronized (threadPoolManagerAccessLock) {
            if (threadpoolMgr == null) {
                threadpoolMgr = new ThreadPoolManagerImpl( threadGroup );
            }
            return threadpoolMgr;
        }
    }
    public CopierManager getCopierManager()
    {
        return copierManager ;
    }
} 
class SynchVariable
{
    public boolean _flag;
    SynchVariable()
    {
        _flag = false;
    }
    public void set()
    {
        _flag = true;
    }
    public boolean value()
    {
        return _flag;
    }
    public void reset()
    {
        _flag = false;
    }
}
