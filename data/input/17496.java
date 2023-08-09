public class ServerTableEntry
{
    private final static int DE_ACTIVATED = 0;
    private final static int ACTIVATING   = 1;
    private final static int ACTIVATED    = 2;
    private final static int RUNNING      = 3;
    private final static int HELD_DOWN    = 4;
    private String printState()
    {
        String str = "UNKNOWN";
        switch (state) {
        case (DE_ACTIVATED) : str = "DE_ACTIVATED"; break;
        case (ACTIVATING  ) : str = "ACTIVATING  "; break;
        case (ACTIVATED   ) : str = "ACTIVATED   "; break;
        case (RUNNING     ) : str = "RUNNING     "; break;
        case (HELD_DOWN   ) : str = "HELD_DOWN   "; break;
        default: break;
        }
        return str;
    }
    private final static long waitTime    = 2000;
    private static final int ActivationRetryMax = 5;
    private int state;
    private int serverId;
    private HashMap orbAndPortInfo;
    private Server serverObj;
    private ServerDef serverDef;
    private Process process;
    private int activateRetryCount=0;
    private String activationCmd;
    private ActivationSystemException wrapper ;
    public String toString()
    {
        return "ServerTableEntry[" + "state=" + printState() +
            " serverId=" + serverId +
            " activateRetryCount=" + activateRetryCount + "]" ;
    }
    private static String javaHome, classPath, fileSep, pathSep;
    static {
        javaHome  = System.getProperty("java.home");
        classPath = System.getProperty("java.class.path");
        fileSep   = System.getProperty("file.separator");
        pathSep   = System.getProperty("path.separator");
    }
    ServerTableEntry( ActivationSystemException wrapper,
        int serverId, ServerDef serverDef, int initialPort,
        String dbDirName, boolean verify, boolean debug )
    {
        this.wrapper = wrapper ;
        this.serverId = serverId;
        this.serverDef = serverDef;
        this.debug = debug ;
        orbAndPortInfo = new HashMap(255);
        activateRetryCount = 0;
        state = ACTIVATING;
        activationCmd =
            javaHome + fileSep + "bin" + fileSep + "java " +
            serverDef.serverVmArgs + " " +
            "-Dioser=" + System.getProperty( "ioser" ) + " " +
            "-D" + ORBConstants.INITIAL_PORT_PROPERTY   + "=" + initialPort + " " +
            "-D" + ORBConstants.DB_DIR_PROPERTY         + "=" + dbDirName + " " +
            "-D" + ORBConstants.ACTIVATED_PROPERTY      + "=true " +
            "-D" + ORBConstants.SERVER_ID_PROPERTY      + "=" + serverId + " " +
            "-D" + ORBConstants.SERVER_NAME_PROPERTY    + "=" + serverDef.serverName + " " +
            (verify ? "-D" + ORBConstants.SERVER_DEF_VERIFY_PROPERTY + "=true ": "") +
            "-classpath " + classPath +
            (serverDef.serverClassPath.equals("") == true ? "" : pathSep) +
            serverDef.serverClassPath +
            " com.sun.corba.se.impl.activation.ServerMain " + serverDef.serverArgs
            + (debug ? " -debug" : "") ;
        if (debug) System.out.println(
                                      "ServerTableEntry constructed with activation command " +
                                      activationCmd);
    }
    public int verify()
    {
        try {
            if (debug)
                System.out.println("Server being verified w/" + activationCmd);
            process = Runtime.getRuntime().exec(activationCmd);
            int result = process.waitFor();
            if (debug)
                printDebug( "verify", "returns " + ServerMain.printResult( result ) ) ;
            return result ;
        } catch (Exception e) {
            if (debug)
                printDebug( "verify", "returns unknown error because of exception " +
                            e ) ;
            return ServerMain.UNKNOWN_ERROR;
        }
    }
    private void printDebug(String method, String msg)
    {
        System.out.println("ServerTableEntry: method  =" + method);
        System.out.println("ServerTableEntry: server  =" + serverId);
        System.out.println("ServerTableEntry: state   =" + printState());
        System.out.println("ServerTableEntry: message =" + msg);
        System.out.println();
    }
    synchronized void activate() throws org.omg.CORBA.SystemException
    {
        state = ACTIVATED;
        try {
            if (debug)
                printDebug("activate", "activating server");
            process = Runtime.getRuntime().exec(activationCmd);
        } catch (Exception e) {
            deActivate();
            if (debug)
                printDebug("activate", "throwing premature process exit");
            throw wrapper.unableToStartProcess() ;
        }
    }
    synchronized void register(Server server)
    {
        if (state == ACTIVATED) {
            serverObj = server;
            if (debug)
                printDebug("register", "process registered back");
        } else {
            if (debug)
                printDebug("register", "throwing premature process exit");
            throw wrapper.serverNotExpectedToRegister() ;
        }
    }
    synchronized void registerPorts( String orbId, EndPointInfo [] endpointList)
        throws ORBAlreadyRegistered
    {
        if (orbAndPortInfo.containsKey(orbId)) {
            throw new ORBAlreadyRegistered(orbId);
        }
        int numListenerPorts = endpointList.length;
        EndPointInfo [] serverListenerPorts = new EndPointInfo[numListenerPorts];
        for (int i = 0; i < numListenerPorts; i++) {
            serverListenerPorts[i] = new EndPointInfo (endpointList[i].endpointType, endpointList[i].port);
        if (debug)
            System.out.println("registering type: " + serverListenerPorts[i].endpointType  +  "  port  " + serverListenerPorts[i].port);
        }
        orbAndPortInfo.put(orbId, serverListenerPorts);
        if (state == ACTIVATED) {
            state = RUNNING;
            notifyAll();
        }
        if (debug)
            printDebug("registerPorts", "process registered Ports");
    }
    void install()
    {
        Server localServerObj = null;
        synchronized ( this ) {
            if (state == RUNNING)
                localServerObj = serverObj;
            else
                throw wrapper.serverNotRunning() ;
        }
        if (localServerObj != null) {
            localServerObj.install() ;
        }
    }
    void uninstall()
    {
        Server localServerObj = null;
        Process localProcess = null;
        synchronized (this) {
            localServerObj = serverObj;
            localProcess = process;
            if (state == RUNNING) {
                deActivate();
            } else {
                throw wrapper.serverNotRunning() ;
            }
        }
        try {
            if (localServerObj != null) {
                localServerObj.shutdown(); 
                localServerObj.uninstall() ; 
            }
            if (localProcess != null) {
                localProcess.destroy();
            }
        } catch (Exception ex) {
        }
    }
    synchronized void holdDown()
    {
        state = HELD_DOWN;
        if (debug)
            printDebug( "holdDown", "server held down" ) ;
        notifyAll();
    }
    synchronized void deActivate()
    {
        state = DE_ACTIVATED;
        if (debug)
            printDebug( "deActivate", "server deactivated" ) ;
        notifyAll();
    }
    synchronized void checkProcessHealth( ) {
        if( state == RUNNING ) {
            try {
                int exitVal = process.exitValue();
            } catch (IllegalThreadStateException e1) {
                return;
            }
            synchronized ( this ) {
                orbAndPortInfo.clear();
                deActivate();
            }
        }
    }
    synchronized boolean isValid()
    {
        if ((state == ACTIVATING) || (state == HELD_DOWN)) {
            if (debug)
                printDebug( "isValid", "returns true" ) ;
            return true;
        }
        try {
            int exitVal = process.exitValue();
        } catch (IllegalThreadStateException e1) {
            return true;
        }
        if (state == ACTIVATED) {
            if (activateRetryCount < ActivationRetryMax) {
                if (debug)
                    printDebug("isValid", "reactivating server");
                activateRetryCount++;
                activate();
                return true;
            }
            if (debug)
                printDebug("isValid", "holding server down");
            holdDown();
            return true;
        }
        deActivate();
        return false;
    }
    synchronized ORBPortInfo[] lookup(String endpointType) throws ServerHeldDown
    {
        while ((state == ACTIVATING) || (state == ACTIVATED)) {
            try {
                wait(waitTime);
                if (!isValid()) break;
            } catch(Exception e) {}
        }
        ORBPortInfo[] orbAndPortList = null;
        if (state == RUNNING) {
            orbAndPortList = new ORBPortInfo[orbAndPortInfo.size()];
            Iterator setORBids = orbAndPortInfo.keySet().iterator();
            try {
                int numElements = 0;
                int i;
                int port;
                while (setORBids.hasNext()) {
                    String orbId = (String) setORBids.next();
                    EndPointInfo [] serverListenerPorts = (EndPointInfo []) orbAndPortInfo.get(orbId);
                    port = -1;
                    for (i = 0; i < serverListenerPorts.length; i++) {
                        if (debug)
                            System.out.println("lookup num-ports " + serverListenerPorts.length + "   " +
                                serverListenerPorts[i].endpointType + "   " +
                                serverListenerPorts[i].port );
                        if ((serverListenerPorts[i].endpointType).equals(endpointType)) {
                            port = serverListenerPorts[i].port;
                            break;
                        }
                    }
                    orbAndPortList[numElements] = new ORBPortInfo(orbId, port);
                    numElements++;
                }
            } catch (NoSuchElementException e) {
            }
            return orbAndPortList;
        }
        if (debug)
            printDebug("lookup", "throwing server held down error");
        throw new ServerHeldDown( serverId ) ;
    }
    synchronized EndPointInfo[] lookupForORB(String orbId)
        throws ServerHeldDown, InvalidORBid
    {
        while ((state == ACTIVATING) || (state == ACTIVATED)) {
            try {
                wait(waitTime);
                if (!isValid()) break;
            } catch(Exception e) {}
        }
        EndPointInfo[] portList = null;
        if (state == RUNNING) {
            try {
                EndPointInfo [] serverListenerPorts = (EndPointInfo []) orbAndPortInfo.get(orbId);
                portList = new EndPointInfo[serverListenerPorts.length];
                for (int i = 0; i < serverListenerPorts.length; i++) {
                   if (debug)
                      System.out.println("lookup num-ports " + serverListenerPorts.length + "   "
                             + serverListenerPorts[i].endpointType + "   " +
                             serverListenerPorts[i].port );
                   portList[i] = new EndPointInfo(serverListenerPorts[i].endpointType, serverListenerPorts[i].port);
                }
            } catch (NoSuchElementException e) {
                throw new InvalidORBid();
            }
            return portList;
        }
        if (debug)
            printDebug("lookup", "throwing server held down error");
        throw new ServerHeldDown( serverId ) ;
    }
    synchronized String[] getORBList()
    {
        String [] orbList = new String[orbAndPortInfo.size()];
        Iterator setORBids = orbAndPortInfo.keySet().iterator();
        try {
            int numElements = 0;
            while (setORBids.hasNext()) {
                String orbId = (String) setORBids.next();
                orbList[numElements++] = orbId ;
            }
        } catch (NoSuchElementException e) {
        }
        return orbList;
    }
    int getServerId()
    {
        return serverId;
    }
    boolean isActive()
    {
        return (state == RUNNING) || (state == ACTIVATED);
    }
    synchronized void destroy()
    {
        Server localServerObj = null;
        Process localProcess = null;
        synchronized (this) {
            localServerObj = serverObj;
            localProcess = process;
            deActivate();
        }
        try {
            if (localServerObj != null)
                localServerObj.shutdown();
            if (debug)
                printDebug( "destroy", "server shutdown successfully" ) ;
        } catch (Exception ex) {
            if (debug)
                printDebug( "destroy",
                            "server shutdown threw exception" + ex ) ;
        }
        try {
            if (localProcess != null)
                localProcess.destroy();
            if (debug)
                printDebug( "destroy", "process destroyed successfully" ) ;
        } catch (Exception ex) {
            if (debug)
                printDebug( "destroy",
                            "process destroy threw exception" + ex ) ;
        }
    }
    private boolean debug = false;
}
