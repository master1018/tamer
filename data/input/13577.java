public class ServerManagerImpl extends _ServerManagerImplBase
    implements BadServerIdHandler
{
    HashMap serverTable;
    Repository repository;
    CorbaTransportManager transportManager;
    int initialPort;
    ORB orb;
    ActivationSystemException wrapper;
    String dbDirName;
    boolean debug = false ;
    private int serverStartupDelay;
    ServerManagerImpl(ORB orb, CorbaTransportManager transportManager,
                      Repository repository, String dbDirName, boolean debug)
    {
        this.orb = orb;
        wrapper = ActivationSystemException.get( orb, CORBALogDomains.ORBD_ACTIVATOR ) ;
        this.transportManager = transportManager; 
        this.repository = repository;
        this.dbDirName = dbDirName;
        this.debug = debug ;
        LegacyServerSocketEndPointInfo endpoint =
            orb.getLegacyServerSocketManager()
                .legacyGetEndpoint(LegacyServerSocketEndPointInfo.BOOT_NAMING);
        initialPort = ((SocketOrChannelAcceptor)endpoint)
            .getServerSocket().getLocalPort();
        serverTable = new HashMap(256);
        serverStartupDelay = ORBConstants.DEFAULT_SERVER_STARTUP_DELAY;
        String  delay = System.getProperty( ORBConstants.SERVER_STARTUP_DELAY);
        if( delay != null ) {
            try {
                serverStartupDelay = Integer.parseInt( delay );
            } catch ( Exception e ) {
            }
        }
        Class cls = orb.getORBData( ).getBadServerIdHandler();
        if( cls == null ) {
            orb.setBadServerIdHandler( this );
        } else {
            orb.initBadServerIdHandler() ;
        }
        orb.connect(this);
        ProcessMonitorThread.start( serverTable );
    }
    public void activate(int serverId)
        throws ServerAlreadyActive, ServerNotRegistered, ServerHeldDown
    {
        ServerLocation   location;
        ServerTableEntry entry;
        Integer key = new Integer(serverId);
        synchronized(serverTable) {
            entry = (ServerTableEntry) serverTable.get(key);
        }
        if (entry != null && entry.isActive()) {
            if (debug)
                System.out.println( "ServerManagerImpl: activate for server Id " +
                                    serverId + " failed because server is already active. " +
                                    "entry = " + entry ) ;
            throw new ServerAlreadyActive( serverId );
        }
        try {
            entry = getEntry(serverId);
            if (debug)
                System.out.println( "ServerManagerImpl: locateServer called with " +
                                " serverId=" + serverId + " endpointType="
                                + IIOP_CLEAR_TEXT.value + " block=false" ) ;
            location = locateServer(entry, IIOP_CLEAR_TEXT.value, false);
            if (debug)
                System.out.println( "ServerManagerImpl: activate for server Id " +
                                    serverId + " found location " +
                                    location.hostname + " and activated it" ) ;
        } catch (NoSuchEndPoint ex) {
            if (debug)
                System.out.println( "ServerManagerImpl: activate for server Id " +
                                    " threw NoSuchEndpoint exception, which was ignored" );
        }
    }
    public void active(int serverId, Server server) throws ServerNotRegistered
    {
        ServerTableEntry entry;
        Integer key = new Integer(serverId);
        synchronized (serverTable) {
            entry = (ServerTableEntry) serverTable.get(key);
            if (entry == null) {
                if (debug)
                    System.out.println( "ServerManagerImpl: active for server Id " +
                                        serverId + " called, but no such server is registered." ) ;
                throw wrapper.serverNotExpectedToRegister() ;
            } else {
                if (debug)
                    System.out.println( "ServerManagerImpl: active for server Id " +
                                        serverId + " called.  This server is now active." ) ;
                entry.register(server);
            }
        }
    }
    public void registerEndpoints( int serverId, String orbId,
        EndPointInfo [] endpointList ) throws NoSuchEndPoint, ServerNotRegistered,
        ORBAlreadyRegistered
    {
        ServerTableEntry entry;
        Integer key = new Integer(serverId);
        synchronized (serverTable) {
            entry = (ServerTableEntry) serverTable.get(key);
            if (entry == null) {
                if (debug)
                    System.out.println(
                        "ServerManagerImpl: registerEndpoint for server Id " +
                        serverId + " called, but no such server is registered." ) ;
                throw wrapper.serverNotExpectedToRegister() ;
            } else {
                if (debug)
                    System.out.println(
                        "ServerManagerImpl: registerEndpoints for server Id " +
                        serverId + " called.  This server is now active." ) ;
                entry.registerPorts( orbId, endpointList );
            }
        }
    }
    public int[] getActiveServers()
    {
        ServerTableEntry entry;
        int[] list = null;
        synchronized (serverTable) {
            ArrayList servers = new ArrayList(0);
            Iterator serverList = serverTable.keySet().iterator();
            try {
                while (serverList.hasNext()) {
                    Integer key = (Integer) serverList.next();
                    entry = (ServerTableEntry) serverTable.get(key);
                    if (entry.isValid() && entry.isActive()) {
                        servers.add(entry);
                    }
                }
            } catch (NoSuchElementException e) {
            }
            list = new int[servers.size()];
            for (int i = 0; i < servers.size(); i++) {
                entry = (ServerTableEntry) servers.get(i);
                list[i] = entry.getServerId();
            }
        }
        if (debug) {
            StringBuffer sb = new StringBuffer() ;
            for (int ctr=0; ctr<list.length; ctr++) {
                sb.append( ' ' ) ;
                sb.append( list[ctr] ) ;
            }
            System.out.println( "ServerManagerImpl: getActiveServers returns" +
                                sb.toString() ) ;
        }
        return list;
    }
    public void shutdown(int serverId) throws ServerNotActive
    {
        ServerTableEntry entry;
        Integer key = new Integer(serverId);
        synchronized(serverTable) {
            entry = (ServerTableEntry) serverTable.remove(key);
            if (entry == null) {
                if (debug)
                    System.out.println( "ServerManagerImpl: shutdown for server Id " +
                                    serverId + " throws ServerNotActive." ) ;
                throw new ServerNotActive( serverId );
            }
            try {
                entry.destroy();
                if (debug)
                    System.out.println( "ServerManagerImpl: shutdown for server Id " +
                                    serverId + " completed." ) ;
            } catch (Exception e) {
                if (debug)
                    System.out.println( "ServerManagerImpl: shutdown for server Id " +
                                    serverId + " threw exception " + e ) ;
            }
        }
    }
    private ServerTableEntry getEntry( int serverId )
        throws ServerNotRegistered
    {
        Integer key = new Integer(serverId);
        ServerTableEntry entry = null ;
        synchronized (serverTable) {
            entry = (ServerTableEntry) serverTable.get(key);
            if (debug)
                if (entry == null) {
                    System.out.println( "ServerManagerImpl: getEntry: " +
                                        "no active server found." ) ;
                } else {
                    System.out.println( "ServerManagerImpl: getEntry: " +
                                        " active server found " + entry + "." ) ;
                }
            if ((entry != null) && (!entry.isValid())) {
                serverTable.remove(key);
                entry = null;
            }
            if (entry == null) {
                ServerDef serverDef = repository.getServer(serverId);
                entry = new ServerTableEntry( wrapper,
                    serverId, serverDef, initialPort, dbDirName, false, debug);
                serverTable.put(key, entry);
                entry.activate() ;
            }
        }
        return entry ;
    }
    private ServerLocation locateServer (ServerTableEntry entry, String endpointType,
                                        boolean block)
        throws NoSuchEndPoint, ServerNotRegistered, ServerHeldDown
    {
        ServerLocation location = new ServerLocation() ;
        ORBPortInfo [] serverORBAndPortList;
        if (block) {
            try {
                    serverORBAndPortList = entry.lookup(endpointType);
            } catch (Exception ex) {
                if (debug)
                    System.out.println( "ServerManagerImpl: locateServer: " +
                                        "server held down" ) ;
                throw new ServerHeldDown( entry.getServerId() );
            }
            String host =
                orb.getLegacyServerSocketManager()
                    .legacyGetEndpoint(LegacyServerSocketEndPointInfo.DEFAULT_ENDPOINT).getHostName();
            location.hostname = host ;
            int listLength;
            if (serverORBAndPortList != null) {
                listLength = serverORBAndPortList.length;
            } else {
                listLength = 0;
            }
            location.ports = new ORBPortInfo[listLength];
            for (int i = 0; i < listLength; i++) {
                location.ports[i] = new ORBPortInfo(serverORBAndPortList[i].orbId,
                        serverORBAndPortList[i].port) ;
                if (debug)
                    System.out.println( "ServerManagerImpl: locateServer: " +
                                    "server located at location " +
                                    location.hostname + " ORBid  " +
                                    serverORBAndPortList[i].orbId +
                                    " Port " + serverORBAndPortList[i].port) ;
            }
        }
        return location;
    }
    private ServerLocationPerORB locateServerForORB (ServerTableEntry entry, String orbId,
                                        boolean block)
        throws InvalidORBid, ServerNotRegistered, ServerHeldDown
    {
        ServerLocationPerORB location = new ServerLocationPerORB() ;
        EndPointInfo [] endpointInfoList;
        if (block) {
            try {
                endpointInfoList = entry.lookupForORB(orbId);
            } catch (InvalidORBid ex) {
                throw ex;
            } catch (Exception ex) {
                if (debug)
                    System.out.println( "ServerManagerImpl: locateServerForORB: " +
                                        "server held down" ) ;
                throw new ServerHeldDown( entry.getServerId() );
            }
            String host =
                orb.getLegacyServerSocketManager()
                    .legacyGetEndpoint(LegacyServerSocketEndPointInfo.DEFAULT_ENDPOINT).getHostName();
            location.hostname = host ;
            int listLength;
            if (endpointInfoList != null) {
                listLength = endpointInfoList.length;
            } else {
                listLength = 0;
            }
            location.ports = new EndPointInfo[listLength];
            for (int i = 0; i < listLength; i++) {
                location.ports[i] = new EndPointInfo(endpointInfoList[i].endpointType,
                        endpointInfoList[i].port) ;
                if (debug)
                    System.out.println( "ServerManagerImpl: locateServer: " +
                                    "server located at location " +
                                    location.hostname + " endpointType  " +
                                    endpointInfoList[i].endpointType +
                                    " Port " + endpointInfoList[i].port) ;
            }
        }
        return location;
    }
    public String[] getORBNames(int serverId)
        throws ServerNotRegistered
    {
        try {
            ServerTableEntry entry = getEntry( serverId ) ;
            return (entry.getORBList());
        } catch (Exception ex) {
            throw new ServerNotRegistered(serverId);
        }
    }
    private ServerTableEntry getRunningEntry( int serverId )
        throws ServerNotRegistered
    {
        ServerTableEntry entry = getEntry( serverId ) ;
        try {
            ORBPortInfo [] serverORBAndPortList = entry.lookup(IIOP_CLEAR_TEXT.value) ;
        } catch (Exception exc) {
            return null ;
        }
        return entry;
    }
    public void install( int serverId )
        throws ServerNotRegistered, ServerHeldDown, ServerAlreadyInstalled
    {
        ServerTableEntry entry = getRunningEntry( serverId ) ;
        if (entry != null) {
            repository.install( serverId ) ;
            entry.install() ;
        }
    }
    public void uninstall( int serverId )
        throws ServerNotRegistered, ServerHeldDown, ServerAlreadyUninstalled
    {
        ServerTableEntry entry =
            (ServerTableEntry) serverTable.get( new Integer(serverId) );
        if (entry != null) {
            entry =
                (ServerTableEntry) serverTable.remove(new Integer(serverId));
            if (entry == null) {
                if (debug)
                    System.out.println( "ServerManagerImpl: shutdown for server Id " +
                                    serverId + " throws ServerNotActive." ) ;
                throw new ServerHeldDown( serverId );
            }
            entry.uninstall();
        }
    }
    public ServerLocation locateServer (int serverId, String endpointType)
        throws NoSuchEndPoint, ServerNotRegistered, ServerHeldDown
    {
        ServerTableEntry entry = getEntry( serverId ) ;
        if (debug)
            System.out.println( "ServerManagerImpl: locateServer called with " +
                                " serverId=" + serverId + " endpointType=" +
                                endpointType + " block=true" ) ;
        return locateServer(entry, endpointType, true);
    }
    public ServerLocationPerORB locateServerForORB (int serverId, String orbId)
        throws InvalidORBid, ServerNotRegistered, ServerHeldDown
    {
        ServerTableEntry entry = getEntry( serverId ) ;
        if (debug)
            System.out.println( "ServerManagerImpl: locateServerForORB called with " +
                                " serverId=" + serverId + " orbId=" + orbId +
                                " block=true" ) ;
        return locateServerForORB(entry, orbId, true);
    }
    public void handle(ObjectKey okey)
    {
        IOR newIOR = null;
        ServerLocationPerORB location;
        ObjectKeyTemplate oktemp = okey.getTemplate();
        int serverId = oktemp.getServerId() ;
        String orbId = oktemp.getORBId() ;
        try {
            ServerTableEntry entry = getEntry( serverId ) ;
            location = locateServerForORB(entry, orbId, true);
            if (debug)
                System.out.println( "ServerManagerImpl: handle called for server id" +
                        serverId + "  orbid  " + orbId) ;
            int clearPort = 0;
            EndPointInfo[] listenerPorts = location.ports;
            for (int i = 0; i < listenerPorts.length; i++) {
                if ((listenerPorts[i].endpointType).equals(IIOP_CLEAR_TEXT.value)) {
                    clearPort = listenerPorts[i].port;
                    break;
                }
            }
            IIOPAddress addr = IIOPFactories.makeIIOPAddress( orb,
                location.hostname, clearPort ) ;
            IIOPProfileTemplate iptemp =
                IIOPFactories.makeIIOPProfileTemplate(
                    orb, GIOPVersion.V1_2, addr ) ;
            if (GIOPVersion.V1_2.supportsIORIIOPProfileComponents()) {
                iptemp.add(IIOPFactories.makeCodeSetsComponent(orb));
                iptemp.add(IIOPFactories.makeMaxStreamFormatVersionComponent());
            }
            IORTemplate iortemp = IORFactories.makeIORTemplate(oktemp) ;
            iortemp.add( iptemp ) ;
            newIOR = iortemp.makeIOR(orb, "IDL:org/omg/CORBA/Object:1.0",
                okey.getId() );
        } catch (Exception e) {
            throw wrapper.errorInBadServerIdHandler( e ) ;
        }
        if (debug)
            System.out.println( "ServerManagerImpl: handle " +
                                "throws ForwardException" ) ;
        try {
            Thread.sleep( serverStartupDelay );
        } catch ( Exception e ) {
            System.out.println( "Exception = " + e );
            e.printStackTrace();
        }
        throw new ForwardException(orb, newIOR);
    }
    public int getEndpoint(String endpointType) throws NoSuchEndPoint
    {
        return orb.getLegacyServerSocketManager()
            .legacyGetTransientServerPort(endpointType);
    }
    public int getServerPortForType(ServerLocationPerORB location,
                                    String endPointType)
        throws NoSuchEndPoint
    {
        EndPointInfo[] listenerPorts = location.ports;
        for (int i = 0; i < listenerPorts.length; i++) {
            if ((listenerPorts[i].endpointType).equals(endPointType)) {
                return listenerPorts[i].port;
            }
        }
        throw new NoSuchEndPoint();
    }
}
