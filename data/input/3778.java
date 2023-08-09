public abstract class ORB extends com.sun.corba.se.org.omg.CORBA.ORB
    implements Broker, TypeCodeFactory
{
    public static boolean ORBInitDebug = false;
    public boolean transportDebugFlag = false ;
    public boolean subcontractDebugFlag = false ;
    public boolean poaDebugFlag = false ;
    public boolean poaConcurrencyDebugFlag = false ;
    public boolean poaFSMDebugFlag = false ;
    public boolean orbdDebugFlag = false ;
    public boolean namingDebugFlag = false ;
    public boolean serviceContextDebugFlag = false ;
    public boolean transientObjectManagerDebugFlag = false ;
    public boolean giopVersionDebugFlag = false;
    public boolean shutdownDebugFlag = false;
    public boolean giopDebugFlag = false;
    public boolean invocationTimingDebugFlag = false ;
    protected static ORBUtilSystemException staticWrapper ;
    protected ORBUtilSystemException wrapper ;
    protected OMGSystemException omgWrapper ;
    private Map typeCodeMap ;
    private TypeCodeImpl[] primitiveTypeCodeConstants ;
    ByteBufferPool byteBufferPool;
    public abstract boolean isLocalHost( String hostName ) ;
    public abstract boolean isLocalServerId( int subcontractId, int serverId ) ;
    public abstract OAInvocationInfo peekInvocationInfo() ;
    public abstract void pushInvocationInfo( OAInvocationInfo info ) ;
    public abstract OAInvocationInfo popInvocationInfo() ;
    public abstract CorbaTransportManager getCorbaTransportManager();
    public abstract LegacyServerSocketManager getLegacyServerSocketManager();
    private Map wrapperMap ;
    private static Map staticWrapperMap = new ConcurrentHashMap();
    private MonitoringManager monitoringManager;
    protected static PresentationManager globalPM = null ;
    static {
        staticWrapper = ORBUtilSystemException.get(
            CORBALogDomains.RPC_PRESENTATION ) ;
        boolean useDynamicStub =
            ((Boolean)AccessController.doPrivileged(
                new PrivilegedAction() {
                    public java.lang.Object run() {
                        return Boolean.valueOf( Boolean.getBoolean (
                            ORBConstants.USE_DYNAMIC_STUB_PROPERTY ) ) ;
                    }
                }
            )).booleanValue() ;
        PresentationManager.StubFactoryFactory dynamicStubFactoryFactory =
            (PresentationManager.StubFactoryFactory)AccessController.doPrivileged(
                new PrivilegedAction() {
                    public java.lang.Object run() {
                        PresentationManager.StubFactoryFactory sff =
                            PresentationDefaults.getProxyStubFactoryFactory() ;
                        String className = System.getProperty(
                            ORBConstants.DYNAMIC_STUB_FACTORY_FACTORY_CLASS,
                            "com.sun.corba.se.impl.presentation.rmi.bcel.StubFactoryFactoryBCELImpl" ) ;
                        try {
                            Class cls = ORBClassLoader.loadClass( className ) ;
                            sff = (PresentationManager.StubFactoryFactory)cls.newInstance() ;
                        } catch (Exception exc) {
                            staticWrapper.errorInSettingDynamicStubFactoryFactory(
                                exc, className ) ;
                        }
                        return sff ;
                    }
                }
            ) ;
        globalPM = new PresentationManagerImpl( useDynamicStub ) ;
        globalPM.setStubFactoryFactory( false,
            PresentationDefaults.getStaticStubFactoryFactory() ) ;
        globalPM.setStubFactoryFactory( true, dynamicStubFactoryFactory ) ;
    }
    public static PresentationManager getPresentationManager()
    {
        return globalPM ;
    }
    public static PresentationManager.StubFactoryFactory
        getStubFactoryFactory()
    {
        boolean useDynamicStubs = globalPM.useDynamicStubs() ;
        return globalPM.getStubFactoryFactory( useDynamicStubs ) ;
    }
    protected ORB()
    {
        wrapperMap = new ConcurrentHashMap();
        wrapper = ORBUtilSystemException.get( this,
            CORBALogDomains.RPC_PRESENTATION ) ;
        omgWrapper = OMGSystemException.get( this,
            CORBALogDomains.RPC_PRESENTATION ) ;
        typeCodeMap = new HashMap();
        primitiveTypeCodeConstants = new TypeCodeImpl[] {
            new TypeCodeImpl(this, TCKind._tk_null),
            new TypeCodeImpl(this, TCKind._tk_void),
            new TypeCodeImpl(this, TCKind._tk_short),
            new TypeCodeImpl(this, TCKind._tk_long),
            new TypeCodeImpl(this, TCKind._tk_ushort),
            new TypeCodeImpl(this, TCKind._tk_ulong),
            new TypeCodeImpl(this, TCKind._tk_float),
            new TypeCodeImpl(this, TCKind._tk_double),
            new TypeCodeImpl(this, TCKind._tk_boolean),
            new TypeCodeImpl(this, TCKind._tk_char),
            new TypeCodeImpl(this, TCKind._tk_octet),
            new TypeCodeImpl(this, TCKind._tk_any),
            new TypeCodeImpl(this, TCKind._tk_TypeCode),
            new TypeCodeImpl(this, TCKind._tk_Principal),
            new TypeCodeImpl(this, TCKind._tk_objref),
            null,       
            null,       
            null,       
            new TypeCodeImpl(this, TCKind._tk_string),
            null,       
            null,       
            null,       
            null,       
            new TypeCodeImpl(this, TCKind._tk_longlong),
            new TypeCodeImpl(this, TCKind._tk_ulonglong),
            new TypeCodeImpl(this, TCKind._tk_longdouble),
            new TypeCodeImpl(this, TCKind._tk_wchar),
            new TypeCodeImpl(this, TCKind._tk_wstring),
            new TypeCodeImpl(this, TCKind._tk_fixed),
            new TypeCodeImpl(this, TCKind._tk_value),
            new TypeCodeImpl(this, TCKind._tk_value_box),
            new TypeCodeImpl(this, TCKind._tk_native),
            new TypeCodeImpl(this, TCKind._tk_abstract_interface)
        } ;
        monitoringManager =
            MonitoringFactories.getMonitoringManagerFactory( ).
                createMonitoringManager(
                MonitoringConstants.DEFAULT_MONITORING_ROOT,
                MonitoringConstants.DEFAULT_MONITORING_ROOT_DESCRIPTION);
    }
    public TypeCodeImpl get_primitive_tc(int kind)
    {
        try {
            return primitiveTypeCodeConstants[kind] ;
        } catch (Throwable t) {
            throw wrapper.invalidTypecodeKind( t, new Integer(kind) ) ;
        }
    }
    public synchronized void setTypeCode(String id, TypeCodeImpl code)
    {
        typeCodeMap.put(id, code);
    }
    public synchronized TypeCodeImpl getTypeCode(String id)
    {
        return (TypeCodeImpl)typeCodeMap.get(id);
    }
    public MonitoringManager getMonitoringManager( ) {
        return monitoringManager;
    }
    public abstract void set_parameters( Properties props ) ;
    public abstract ORBVersion getORBVersion() ;
    public abstract void setORBVersion( ORBVersion version ) ;
    public abstract IOR getFVDCodeBaseIOR() ;
    public abstract void handleBadServerId( ObjectKey okey ) ;
    public abstract void setBadServerIdHandler( BadServerIdHandler handler ) ;
    public abstract void initBadServerIdHandler() ;
    public abstract void notifyORB() ;
    public abstract PIHandler getPIHandler() ;
    public abstract void checkShutdownState();
    public abstract boolean isDuringDispatch() ;
    public abstract void startingDispatch();
    public abstract void finishedDispatch();
    public abstract int getTransientServerId();
    public abstract ServiceContextRegistry getServiceContextRegistry() ;
    public abstract RequestDispatcherRegistry getRequestDispatcherRegistry();
    public abstract ORBData getORBData() ;
    public abstract void setClientDelegateFactory( ClientDelegateFactory factory ) ;
    public abstract ClientDelegateFactory getClientDelegateFactory() ;
    public abstract void setCorbaContactInfoListFactory( CorbaContactInfoListFactory factory ) ;
    public abstract CorbaContactInfoListFactory getCorbaContactInfoListFactory() ;
    public abstract void setResolver( Resolver resolver ) ;
    public abstract Resolver getResolver() ;
    public abstract void setLocalResolver( LocalResolver resolver ) ;
    public abstract LocalResolver getLocalResolver() ;
    public abstract void setURLOperation( Operation stringToObject ) ;
    public abstract Operation getURLOperation() ;
    public abstract void setINSDelegate( CorbaServerRequestDispatcher insDelegate ) ;
    public abstract TaggedComponentFactoryFinder getTaggedComponentFactoryFinder() ;
    public abstract IdentifiableFactoryFinder getTaggedProfileFactoryFinder() ;
    public abstract IdentifiableFactoryFinder getTaggedProfileTemplateFactoryFinder() ;
    public abstract ObjectKeyFactory getObjectKeyFactory() ;
    public abstract void setObjectKeyFactory( ObjectKeyFactory factory ) ;
    public Logger getLogger( String domain )
    {
        ORBData odata = getORBData() ;
        String ORBId ;
        if (odata == null)
            ORBId = "_INITIALIZING_" ;
        else {
            ORBId = odata.getORBId() ;
            if (ORBId.equals(""))
                ORBId = "_DEFAULT_" ;
        }
        return getCORBALogger( ORBId, domain ) ;
    }
    public static Logger staticGetLogger( String domain )
    {
        return getCORBALogger( "_CORBA_", domain ) ;
    }
    private static Logger getCORBALogger( String ORBId, String domain )
    {
        String fqLogDomain = CORBALogDomains.TOP_LEVEL_DOMAIN + "." +
            ORBId + "." + domain;
        return Logger.getLogger( fqLogDomain, ORBConstants.LOG_RESOURCE_FILE );
    }
    public LogWrapperBase getLogWrapper( String logDomain,
        String exceptionGroup, LogWrapperFactory factory )
    {
        StringPair key = new StringPair( logDomain, exceptionGroup ) ;
        LogWrapperBase logWrapper = (LogWrapperBase)wrapperMap.get( key );
        if (logWrapper == null) {
            logWrapper = factory.create( getLogger( logDomain ) );
            wrapperMap.put( key, logWrapper );
        }
        return logWrapper;
    }
    public static LogWrapperBase staticGetLogWrapper( String logDomain,
        String exceptionGroup, LogWrapperFactory factory )
    {
        StringPair key = new StringPair( logDomain, exceptionGroup ) ;
        LogWrapperBase logWrapper = (LogWrapperBase)staticWrapperMap.get( key );
        if (logWrapper == null) {
            logWrapper = factory.create( staticGetLogger( logDomain ) );
            staticWrapperMap.put( key, logWrapper );
        }
        return logWrapper;
    }
    public ByteBufferPool getByteBufferPool()
    {
        if (byteBufferPool == null)
            byteBufferPool = new ByteBufferPoolImpl(this);
        return byteBufferPool;
    }
    public abstract void setThreadPoolManager(ThreadPoolManager mgr);
    public abstract ThreadPoolManager getThreadPoolManager();
    public abstract CopierManager getCopierManager() ;
}
