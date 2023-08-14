public class ServerMain
{
    public final static int OK = 0;
    public final static int MAIN_CLASS_NOT_FOUND = 1;
    public final static int NO_MAIN_METHOD = 2;
    public final static int APPLICATION_ERROR = 3;
    public final static int UNKNOWN_ERROR = 4;
    public final static int NO_SERVER_ID = 5 ;
    public final static int REGISTRATION_FAILED = 6;
    public static String printResult( int result )
    {
        switch (result) {
            case OK :                   return "Server terminated normally" ;
            case MAIN_CLASS_NOT_FOUND : return "main class not found" ;
            case NO_MAIN_METHOD :       return "no main method" ;
            case APPLICATION_ERROR :    return "application error" ;
            case NO_SERVER_ID :         return "server ID not defined" ;
            case REGISTRATION_FAILED:   return "server registration failed" ;
            default :                   return "unknown error" ;
        }
    }
    private void redirectIOStreams()
    {
        try {
            String logDirName =
                System.getProperty( ORBConstants.DB_DIR_PROPERTY ) +
                System.getProperty("file.separator") +
                ORBConstants.SERVER_LOG_DIR +
                System.getProperty("file.separator");
            File logDir = new File(logDirName);
            String server = System.getProperty(
                ORBConstants.SERVER_ID_PROPERTY ) ;
            FileOutputStream foutStream =
                new FileOutputStream(logDirName + server+".out", true);
            FileOutputStream ferrStream =
                new FileOutputStream(logDirName + server+".err", true);
            PrintStream pSout = new PrintStream(foutStream, true);
            PrintStream pSerr = new PrintStream(ferrStream, true);
            System.setOut(pSout);
            System.setErr(pSerr);
            logInformation( "Server started" ) ;
        } catch (Exception ex) {}
    }
    private static void writeLogMessage( PrintStream pstream, String msg )
    {
        Date date = new Date();
        pstream.print( "[" + date.toString() + "] " + msg + "\n");
    }
    public static void logInformation( String msg )
    {
        writeLogMessage( System.out, "        " + msg ) ;
    }
    public static void logError( String msg )
    {
        writeLogMessage( System.out, "ERROR:  " + msg ) ;
        writeLogMessage( System.err, "ERROR:  " + msg ) ;
    }
    public static void logTerminal( String msg, int code )
    {
        if (code == 0) {
            writeLogMessage( System.out, "        " + msg ) ;
        } else {
            writeLogMessage( System.out, "FATAL:  " +
                printResult( code ) + ": " + msg ) ;
            writeLogMessage( System.err, "FATAL:  " +
                printResult( code ) + ": " + msg ) ;
        }
        System.exit( code ) ;
    }
    private Method getMainMethod( Class serverClass )
    {
        Class argTypes[] = new Class[] { String[].class } ;
        Method method = null ;
        try {
            method = serverClass.getDeclaredMethod( "main", argTypes ) ;
        } catch (Exception exc) {
            logTerminal( exc.getMessage(), NO_MAIN_METHOD ) ;
        }
        if (!isPublicStaticVoid( method ))
            logTerminal( "", NO_MAIN_METHOD ) ;
        return method ;
    }
    private boolean isPublicStaticVoid( Method method )
    {
        int modifiers =  method.getModifiers ();
        if (!Modifier.isPublic (modifiers) || !Modifier.isStatic (modifiers)) {
            logError( method.getName() + " is not public static" ) ;
            return false ;
        }
        if (method.getExceptionTypes ().length != 0) {
            logError( method.getName() + " declares exceptions" ) ;
            return false ;
        }
        if (!method.getReturnType().equals (Void.TYPE)) {
            logError( method.getName() + " does not have a void return type" ) ;
            return false ;
        }
        return true ;
    }
    private Method getNamedMethod( Class serverClass, String methodName )
    {
        Class argTypes[] = new Class[] { org.omg.CORBA.ORB.class } ;
        Method method = null ;
        try {
            method = serverClass.getDeclaredMethod( methodName, argTypes ) ;
        } catch (Exception exc) {
            return null ;
        }
        if (!isPublicStaticVoid( method ))
            return null ;
        return method ;
    }
    private void run(String[] args)
    {
        try {
            redirectIOStreams() ;
            String serverClassName = System.getProperty(
                ORBConstants.SERVER_NAME_PROPERTY ) ;
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            if (cl == null)
                cl = ClassLoader.getSystemClassLoader();
            Class serverClass = null;
            try {
                serverClass = Class.forName( serverClassName ) ;
            } catch (ClassNotFoundException ex) {
                serverClass = Class.forName( serverClassName, true, cl);
            }
            if (debug)
                System.out.println("class " + serverClassName + " found");
            Method mainMethod = getMainMethod( serverClass ) ;
            boolean serverVerifyFlag = Boolean.getBoolean(
                ORBConstants.SERVER_DEF_VERIFY_PROPERTY) ;
            if (serverVerifyFlag) {
                if (mainMethod == null)
                    logTerminal("", NO_MAIN_METHOD);
                else {
                    if (debug)
                        System.out.println("Valid Server");
                    logTerminal("", OK);
                }
            }
            registerCallback( serverClass ) ;
            Object params [] = new Object [1];
            params[0] = args;
            mainMethod.invoke(null, params);
        } catch (ClassNotFoundException e) {
            logTerminal("ClassNotFound exception: " + e.getMessage(),
                MAIN_CLASS_NOT_FOUND);
        } catch (Exception e) {
            logTerminal("Exception: " + e.getMessage(),
                APPLICATION_ERROR);
        }
    }
    public static void main(String[] args) {
        ServerMain server = new ServerMain();
        server.run(args);
    }
    private static final boolean debug = false;
    private int getServerId()
    {
        Integer serverId = Integer.getInteger( ORBConstants.SERVER_ID_PROPERTY ) ;
        if (serverId == null)
            logTerminal( "", NO_SERVER_ID ) ;
        return serverId.intValue() ;
    }
    private void registerCallback( Class serverClass )
    {
        Method installMethod = getNamedMethod( serverClass, "install" ) ;
        Method uninstallMethod = getNamedMethod( serverClass, "uninstall" ) ;
        Method shutdownMethod = getNamedMethod( serverClass, "shutdown" ) ;
        Properties props = new Properties() ;
        props.put( "org.omg.CORBA.ORBClass",
            "com.sun.corba.se.impl.orb.ORBImpl" ) ;
        props.put( ORBConstants.ACTIVATED_PROPERTY, "false" );
        String args[] = null ;
        ORB orb = ORB.init( args, props ) ;
        ServerCallback serverObj = new ServerCallback( orb,
            installMethod, uninstallMethod, shutdownMethod ) ;
        int serverId = getServerId() ;
        try {
            Activator activator = ActivatorHelper.narrow(
                orb.resolve_initial_references( ORBConstants.SERVER_ACTIVATOR_NAME ));
            activator.active(serverId, serverObj);
        } catch (Exception ex) {
            logTerminal( "exception " + ex.getMessage(),
                REGISTRATION_FAILED ) ;
        }
    }
}
class ServerCallback extends
    com.sun.corba.se.spi.activation._ServerImplBase
{
    private ORB orb;
    private Method installMethod ;
    private Method uninstallMethod ;
    private Method shutdownMethod ;
    private Object methodArgs[] ;
    ServerCallback(ORB orb, Method installMethod, Method uninstallMethod,
        Method shutdownMethod )
    {
        this.orb = orb;
        this.installMethod = installMethod ;
        this.uninstallMethod = uninstallMethod ;
        this.shutdownMethod = shutdownMethod ;
        orb.connect( this ) ;
        methodArgs = new Object[] { orb } ;
    }
    private void invokeMethod( Method method )
    {
        if (method != null)
            try {
                method.invoke( null, methodArgs ) ;
            } catch (Exception exc) {
                ServerMain.logError( "could not invoke " + method.getName() +
                    " method: " + exc.getMessage() ) ;
            }
    }
    public void shutdown()
    {
        ServerMain.logInformation( "Shutdown starting" ) ;
        invokeMethod( shutdownMethod ) ;
        orb.shutdown(true);
        ServerMain.logTerminal( "Shutdown completed", ServerMain.OK ) ;
    }
    public void install()
    {
        ServerMain.logInformation( "Install starting" ) ;
        invokeMethod( installMethod ) ;
        ServerMain.logInformation( "Install completed" ) ;
    }
    public void uninstall()
    {
        ServerMain.logInformation( "uninstall starting" ) ;
        invokeMethod( uninstallMethod ) ;
        ServerMain.logInformation( "uninstall completed" ) ;
    }
}
