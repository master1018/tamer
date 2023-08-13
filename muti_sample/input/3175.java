public class SocketOrChannelAcceptorImpl
    extends
        EventHandlerBase
    implements
        CorbaAcceptor,
        SocketOrChannelAcceptor,
        Work,
        SocketInfo,
        LegacyServerSocketEndPointInfo
{
    protected ServerSocketChannel serverSocketChannel;
    protected ServerSocket serverSocket;
    protected int port;
    protected long enqueueTime;
    protected boolean initialized;
    protected ORBUtilSystemException wrapper ;
    protected InboundConnectionCache connectionCache;
    protected String type = "";
    protected String name = "";
    protected String hostname;
    protected int locatorPort;
    public SocketOrChannelAcceptorImpl(ORB orb)
    {
        this.orb = orb;
        wrapper = ORBUtilSystemException.get( orb,
            CORBALogDomains.RPC_TRANSPORT ) ;
        setWork(this);
        initialized = false;
        this.hostname = orb.getORBData().getORBServerHost();
        this.name = LegacyServerSocketEndPointInfo.NO_NAME;
        this.locatorPort = -1;
    }
    public SocketOrChannelAcceptorImpl(ORB orb, int port)
    {
        this(orb);
        this.port = port;
    }
    public SocketOrChannelAcceptorImpl(ORB orb, int port,
                                       String name, String type)
    {
        this(orb, port);
        this.name = name;
        this.type = type;
    }
    public boolean initialize()
    {
        if (initialized) {
            return false;
        }
        if (orb.transportDebugFlag) {
            dprint(".initialize: " + this);
        }
        InetSocketAddress inetSocketAddress = null;
        try {
            if (orb.getORBData().getListenOnAllInterfaces().equals(ORBConstants.LISTEN_ON_ALL_INTERFACES)) {
                inetSocketAddress = new InetSocketAddress(port);
            } else {
                String host = orb.getORBData().getORBServerHost();
                inetSocketAddress = new InetSocketAddress(host, port);
            }
            serverSocket = orb.getORBData().getSocketFactory()
                .createServerSocket(type, inetSocketAddress);
            internalInitialize();
        } catch (Throwable t) {
            throw wrapper.createListenerFailed( t, Integer.toString(port) ) ;
        }
        initialized = true;
        return true;
    }
    protected void internalInitialize()
        throws Exception
    {
        port = serverSocket.getLocalPort();
        orb.getCorbaTransportManager().getInboundConnectionCache(this);
        serverSocketChannel = serverSocket.getChannel();
        if (serverSocketChannel != null) {
            setUseSelectThreadToWait(
                orb.getORBData().acceptorSocketUseSelectThreadToWait());
            serverSocketChannel.configureBlocking(
                ! orb.getORBData().acceptorSocketUseSelectThreadToWait());
        } else {
            setUseSelectThreadToWait(false);
        }
        setUseWorkerThreadForEvent(
            orb.getORBData().acceptorSocketUseWorkerThreadForEvent());
    }
    public boolean initialized()
    {
        return initialized;
    }
    public String getConnectionCacheType()
    {
        return this.getClass().toString();
    }
    public void setConnectionCache(InboundConnectionCache connectionCache)
    {
        this.connectionCache = connectionCache;
    }
    public InboundConnectionCache getConnectionCache()
    {
        return connectionCache;
    }
    public boolean shouldRegisterAcceptEvent()
    {
        return true;
    }
    public void accept()
    {
        try {
            SocketChannel socketChannel = null;
            Socket socket = null;
            if (serverSocketChannel == null) {
                socket = serverSocket.accept();
            } else {
                socketChannel = serverSocketChannel.accept();
                socket = socketChannel.socket();
            }
            orb.getORBData().getSocketFactory()
                .setAcceptedSocketOptions(this, serverSocket, socket);
            if (orb.transportDebugFlag) {
                dprint(".accept: " +
                       (serverSocketChannel == null
                        ? serverSocket.toString()
                        : serverSocketChannel.toString()));
            }
            CorbaConnection connection =
                new SocketOrChannelConnectionImpl(orb, this, socket);
            if (orb.transportDebugFlag) {
                dprint(".accept: new: " + connection);
            }
            getConnectionCache().put(this, connection);
            if (connection.shouldRegisterServerReadEvent()) {
                Selector selector = orb.getTransportManager().getSelector(0);
                selector.registerForEvent(connection.getEventHandler());
            }
            getConnectionCache().reclaim();
        } catch (IOException e) {
            if (orb.transportDebugFlag) {
                dprint(".accept:", e);
            }
            orb.getTransportManager().getSelector(0).unregisterForEvent(this);
            orb.getTransportManager().getSelector(0).registerForEvent(this);
        }
    }
    public void close ()
    {
        try {
            if (orb.transportDebugFlag) {
                dprint(".close->:");
            }
            Selector selector = orb.getTransportManager().getSelector(0);
            selector.unregisterForEvent(this);
            if (serverSocketChannel != null) {
                serverSocketChannel.close();
            }
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            if (orb.transportDebugFlag) {
                dprint(".close:", e);
            }
        } finally {
            if (orb.transportDebugFlag) {
                dprint(".close<-:");
            }
        }
    }
    public EventHandler getEventHandler()
    {
        return this;
    }
    public String getObjectAdapterId()
    {
        return null;
    }
    public String getObjectAdapterManagerId()
    {
        return null;
    }
    public void addToIORTemplate(IORTemplate iorTemplate,
                                 Policies policies,
                                 String codebase)
    {
        Iterator iterator = iorTemplate.iteratorById(
            org.omg.IOP.TAG_INTERNET_IOP.value);
        String hostname = orb.getORBData().getORBServerHost();
        if (iterator.hasNext()) {
            IIOPAddress iiopAddress =
                IIOPFactories.makeIIOPAddress(orb, hostname, port);
            AlternateIIOPAddressComponent iiopAddressComponent =
                IIOPFactories.makeAlternateIIOPAddressComponent(iiopAddress);
            while (iterator.hasNext()) {
                TaggedProfileTemplate taggedProfileTemplate =
                    (TaggedProfileTemplate) iterator.next();
                taggedProfileTemplate.add(iiopAddressComponent);
            }
        } else {
            GIOPVersion version = orb.getORBData().getGIOPVersion();
            int templatePort;
            if (policies.forceZeroPort()) {
                templatePort = 0;
            } else if (policies.isTransient()) {
                templatePort = port;
            } else {
                templatePort = orb.getLegacyServerSocketManager()
                   .legacyGetPersistentServerPort(SocketInfo.IIOP_CLEAR_TEXT);
            }
            IIOPAddress addr =
                IIOPFactories.makeIIOPAddress(orb, hostname, templatePort);
            IIOPProfileTemplate iiopProfile =
                IIOPFactories.makeIIOPProfileTemplate(orb, version, addr);
            if (version.supportsIORIIOPProfileComponents()) {
                iiopProfile.add(IIOPFactories.makeCodeSetsComponent(orb));
                iiopProfile.add(IIOPFactories.makeMaxStreamFormatVersionComponent());
                RequestPartitioningPolicy rpPolicy = (RequestPartitioningPolicy)
                    policies.get_effective_policy(
                                      ORBConstants.REQUEST_PARTITIONING_POLICY);
                if (rpPolicy != null) {
                    iiopProfile.add(
                         IIOPFactories.makeRequestPartitioningComponent(
                             rpPolicy.getValue()));
                }
                if (codebase != null && codebase != "") {
                    iiopProfile.add(IIOPFactories. makeJavaCodebaseComponent(codebase));
                }
                if (orb.getORBData().isJavaSerializationEnabled()) {
                    iiopProfile.add(
                           IIOPFactories.makeJavaSerializationComponent());
                }
            }
            iorTemplate.add(iiopProfile);
        }
    }
    public String getMonitoringName()
    {
        return "AcceptedConnections";
    }
    public SelectableChannel getChannel()
    {
        return serverSocketChannel;
    }
    public int getInterestOps()
    {
        return SelectionKey.OP_ACCEPT;
    }
    public Acceptor getAcceptor()
    {
        return this;
    }
    public Connection getConnection()
    {
        throw new RuntimeException("Should not happen.");
    }
    public void doWork()
    {
        try {
            if (orb.transportDebugFlag) {
                dprint(".doWork->: " + this);
            }
            if (selectionKey.isAcceptable()) {
                        accept();
            } else {
                if (orb.transportDebugFlag) {
                    dprint(".doWork: ! selectionKey.isAcceptable: " + this);
                }
            }
        } catch (SecurityException se) {
            if (orb.transportDebugFlag) {
                dprint(".doWork: ignoring SecurityException: "
                       + se
                       + " " + this);
            }
            String permissionStr = ORBUtility.getClassSecurityInfo(getClass());
            wrapper.securityExceptionInAccept(se, permissionStr);
        } catch (Exception ex) {
            if (orb.transportDebugFlag) {
                dprint(".doWork: ignoring Exception: "
                       + ex
                       + " " + this);
            }
            wrapper.exceptionInAccept(ex);
        } catch (Throwable t) {
            if (orb.transportDebugFlag) {
                dprint(".doWork: ignoring Throwable: "
                       + t
                       + " " + this);
            }
        } finally {
            Selector selector = orb.getTransportManager().getSelector(0);
            selector.registerInterestOps(this);
            if (orb.transportDebugFlag) {
                dprint(".doWork<-:" + this);
            }
        }
    }
    public void setEnqueueTime(long timeInMillis)
    {
        enqueueTime = timeInMillis;
    }
    public long getEnqueueTime()
    {
        return enqueueTime;
    }
    public MessageMediator createMessageMediator(Broker broker,
                                                 Connection connection)
    {
        ContactInfo contactInfo = new SocketOrChannelContactInfoImpl();
        return contactInfo.createMessageMediator(broker, connection);
    }
    public MessageMediator finishCreatingMessageMediator(Broker broker,
                                                         Connection connection,
                                                         MessageMediator messageMediator)
    {
        ContactInfo contactInfo = new SocketOrChannelContactInfoImpl();
        return contactInfo.finishCreatingMessageMediator(broker,
                                          connection, messageMediator);
    }
    public InputObject createInputObject(Broker broker,
                                         MessageMediator messageMediator)
    {
        CorbaMessageMediator corbaMessageMediator = (CorbaMessageMediator)
            messageMediator;
        return new CDRInputObject((ORB)broker,
                                  (CorbaConnection)messageMediator.getConnection(),
                                  corbaMessageMediator.getDispatchBuffer(),
                                  corbaMessageMediator.getDispatchHeader());
    }
    public OutputObject createOutputObject(Broker broker,
                                           MessageMediator messageMediator)
    {
        CorbaMessageMediator corbaMessageMediator = (CorbaMessageMediator)
            messageMediator;
        return new CDROutputObject((ORB) broker, corbaMessageMediator,
                                   corbaMessageMediator.getReplyHeader(),
                                   corbaMessageMediator.getStreamFormatVersion());
    }
    public ServerSocket getServerSocket()
    {
        return serverSocket;
    }
    public String toString()
    {
        String sock;
        if (serverSocketChannel == null) {
            if (serverSocket == null) {
                sock = "(not initialized)";
            } else {
                sock = serverSocket.toString();
            }
        } else {
            sock = serverSocketChannel.toString();
        }
        return
            toStringName() +
            "["
            + sock + " "
            + type + " "
            + shouldUseSelectThreadToWait() + " "
            + shouldUseWorkerThreadForEvent()
            + "]" ;
    }
    protected String toStringName()
    {
        return "SocketOrChannelAcceptorImpl";
    }
    protected void dprint(String msg)
    {
        ORBUtility.dprint(toStringName(), msg);
    }
    protected void dprint(String msg, Throwable t)
    {
        dprint(msg);
        t.printStackTrace(System.out);
    }
    public String getType()
    {
        return type;
    }
    public String getHostName()
    {
        return hostname;
    }
    public String getHost()
    {
        return hostname;
    }
    public int getPort()
    {
        return port;
    }
    public int getLocatorPort()
    {
        return locatorPort;
    }
    public void setLocatorPort (int port)
    {
        locatorPort = port;
    }
    public String getName()
    {
        String result =
            name.equals(LegacyServerSocketEndPointInfo.NO_NAME) ?
            this.toString() : name;
        return result;
    }
}
