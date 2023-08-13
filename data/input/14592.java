public class ORBD
{
    private int initSvcPort;
    protected void initializeBootNaming(ORB orb)
    {
        initSvcPort = orb.getORBData().getORBInitialPort();
        Acceptor acceptor;
        if (orb.getORBData().getLegacySocketFactory() == null) {
            acceptor =
                new SocketOrChannelAcceptorImpl(
                    orb,
                    initSvcPort,
                    LegacyServerSocketEndPointInfo.BOOT_NAMING,
                    SocketInfo.IIOP_CLEAR_TEXT);
        } else {
            acceptor =
                new SocketFactoryAcceptorImpl(
                    orb,
                    initSvcPort,
                    LegacyServerSocketEndPointInfo.BOOT_NAMING,
                    SocketInfo.IIOP_CLEAR_TEXT);
        }
        orb.getCorbaTransportManager().registerAcceptor(acceptor);
    }
    protected ORB createORB(String[] args)
    {
        Properties props = System.getProperties();
        props.put( ORBConstants.SERVER_ID_PROPERTY, "1000" ) ;
        props.put( ORBConstants.PERSISTENT_SERVER_PORT_PROPERTY,
            props.getProperty( ORBConstants.ORBD_PORT_PROPERTY,
                Integer.toString(
                    ORBConstants.DEFAULT_ACTIVATION_PORT ) ) ) ;
        props.put("org.omg.CORBA.ORBClass",
            "com.sun.corba.se.impl.orb.ORBImpl");
        return (ORB) ORB.init(args, props);
    }
    private void run(String[] args)
    {
        try {
            processArgs(args);
            ORB orb = createORB(args);
            if (orb.orbdDebugFlag)
                System.out.println( "ORBD begins initialization." ) ;
            boolean firstRun = createSystemDirs( ORBConstants.DEFAULT_DB_DIR );
            startActivationObjects(orb);
            if (firstRun) 
                installOrbServers(getRepository(), getActivator());
            if (orb.orbdDebugFlag) {
                System.out.println( "ORBD is ready." ) ;
                System.out.println("ORBD serverid: " +
                        System.getProperty(ORBConstants.SERVER_ID_PROPERTY));
                System.out.println("activation dbdir: " +
                        System.getProperty(ORBConstants.DB_DIR_PROPERTY));
                System.out.println("activation port: " +
                        System.getProperty(ORBConstants.ORBD_PORT_PROPERTY));
                String pollingTime = System.getProperty(
                    ORBConstants.SERVER_POLLING_TIME);
                if( pollingTime == null ) {
                    pollingTime = Integer.toString(
                        ORBConstants.DEFAULT_SERVER_POLLING_TIME );
                }
                System.out.println("activation Server Polling Time: " +
                        pollingTime + " milli-seconds ");
                String startupDelay = System.getProperty(
                    ORBConstants.SERVER_STARTUP_DELAY);
                if( startupDelay == null ) {
                    startupDelay = Integer.toString(
                        ORBConstants.DEFAULT_SERVER_STARTUP_DELAY );
                }
                System.out.println("activation Server Startup Delay: " +
                        startupDelay + " milli-seconds " );
            }
            NameServiceStartThread theThread =
                new NameServiceStartThread( orb, dbDir );
            theThread.start( );
            orb.run();
        } catch( org.omg.CORBA.COMM_FAILURE cex ) {
            System.out.println( CorbaResourceUtil.getText("orbd.commfailure"));
            System.out.println( cex );
            cex.printStackTrace();
        } catch( org.omg.CORBA.INTERNAL iex ) {
            System.out.println( CorbaResourceUtil.getText(
                "orbd.internalexception"));
            System.out.println( iex );
            iex.printStackTrace();
        } catch (Exception ex) {
            System.out.println(CorbaResourceUtil.getText(
                "orbd.usage", "orbd"));
            System.out.println( ex );
            ex.printStackTrace();
        }
    }
    private void processArgs(String[] args)
    {
        Properties props = System.getProperties();
        for (int i=0; i < args.length; i++) {
            if (args[i].equals("-port")) {
                if ((i+1) < args.length) {
                    props.put(ORBConstants.ORBD_PORT_PROPERTY, args[++i]);
                } else {
                    System.out.println(CorbaResourceUtil.getText(
                        "orbd.usage", "orbd"));
                }
            } else if (args[i].equals("-defaultdb")) {
                if ((i+1) < args.length) {
                    props.put(ORBConstants.DB_DIR_PROPERTY, args[++i]);
                } else {
                    System.out.println(CorbaResourceUtil.getText(
                        "orbd.usage", "orbd"));
                }
            } else if (args[i].equals("-serverid")) {
                if ((i+1) < args.length) {
                    props.put(ORBConstants.SERVER_ID_PROPERTY, args[++i]);
                } else {
                    System.out.println(CorbaResourceUtil.getText(
                        "orbd.usage", "orbd"));
                }
            } else if (args[i].equals("-serverPollingTime")) {
                if ((i+1) < args.length) {
                    props.put(ORBConstants.SERVER_POLLING_TIME, args[++i]);
                } else {
                    System.out.println(CorbaResourceUtil.getText(
                        "orbd.usage", "orbd"));
                }
            } else if (args[i].equals("-serverStartupDelay")) {
                if ((i+1) < args.length) {
                    props.put(ORBConstants.SERVER_STARTUP_DELAY, args[++i]);
                } else {
                    System.out.println(CorbaResourceUtil.getText(
                        "orbd.usage", "orbd"));
                }
            }
        }
    }
    protected boolean createSystemDirs(String defaultDbDir)
    {
        boolean dirCreated = false;
        Properties props = System.getProperties();
        String fileSep = props.getProperty("file.separator");
        dbDir = new File (props.getProperty( ORBConstants.DB_DIR_PROPERTY,
            props.getProperty("user.dir") + fileSep + defaultDbDir));
        dbDirName = dbDir.getAbsolutePath();
        props.put(ORBConstants.DB_DIR_PROPERTY, dbDirName);
        if (!dbDir.exists()) {
            dbDir.mkdir();
            dirCreated = true;
        }
        File logDir = new File (dbDir, ORBConstants.SERVER_LOG_DIR ) ;
        if (!logDir.exists()) logDir.mkdir();
        return dirCreated;
    }
    protected File dbDir;
    protected File getDbDir()
    {
        return dbDir;
    }
    private String dbDirName;
    protected String getDbDirName()
    {
        return dbDirName;
    }
    protected void startActivationObjects(ORB orb) throws Exception
    {
        initializeBootNaming(orb);
        repository = new RepositoryImpl(orb, dbDir, orb.orbdDebugFlag );
        orb.register_initial_reference( ORBConstants.SERVER_REPOSITORY_NAME, repository );
        ServerManagerImpl serverMgr =
            new ServerManagerImpl( orb,
                                   orb.getCorbaTransportManager(),
                                   repository,
                                   getDbDirName(),
                                   orb.orbdDebugFlag );
        locator = LocatorHelper.narrow(serverMgr);
        orb.register_initial_reference( ORBConstants.SERVER_LOCATOR_NAME, locator );
        activator = ActivatorHelper.narrow(serverMgr);
        orb.register_initial_reference( ORBConstants.SERVER_ACTIVATOR_NAME, activator );
        TransientNameService nameService = new TransientNameService(orb,
            ORBConstants.TRANSIENT_NAME_SERVICE_NAME);
    }
    protected Locator locator;
    protected Locator getLocator()
    {
        return locator;
    }
    protected Activator activator;
    protected Activator getActivator()
    {
        return activator;
    }
    protected RepositoryImpl repository;
    protected RepositoryImpl getRepository()
    {
        return repository;
    }
    protected void installOrbServers(RepositoryImpl repository,
                                     Activator activator)
    {
        int serverId;
        String[] server;
        ServerDef serverDef;
        for (int i=0; i < orbServers.length; i++) {
            try {
                server = orbServers[i];
                serverDef = new ServerDef(server[1], server[2],
                                          server[3], server[4], server[5] );
                serverId = Integer.valueOf(orbServers[i][0]).intValue();
                repository.registerServer(serverDef, serverId);
                activator.activate(serverId);
            } catch (Exception ex) {}
        }
    }
    public static void main(String[] args) {
        ORBD orbd = new ORBD();
        orbd.run(args);
    }
    private static String[][] orbServers = {
        {""}
    };
}
