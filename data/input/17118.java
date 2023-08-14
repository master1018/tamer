public abstract class CommunicatorServer
    implements Runnable, MBeanRegistration, NotificationBroadcaster,
               CommunicatorServerMBean {
    public static final int ONLINE = 0 ;
    public static final int OFFLINE = 1 ;
    public static final int STOPPING = 2 ;
    public static final int STARTING = 3 ;
    public static final int SNMP_TYPE = 4 ;
     transient volatile int state = OFFLINE ;
    ObjectName objectName ;
    MBeanServer topMBS;
    MBeanServer bottomMBS;
    transient String dbgTag = null ;
    int maxActiveClientCount = 1 ;
    transient int servedClientCount = 0 ;
    String host = null ;
    int port = -1 ;
    private transient Object stateLock = new Object();
    private transient Vector<ClientHandler>
            clientHandlerVector = new Vector<ClientHandler>() ;
    private transient Thread fatherThread = Thread.currentThread() ;
    private transient Thread mainThread = null ;
    private volatile boolean stopRequested = false ;
    private boolean interrupted = false;
    private transient Exception startException = null;
    private transient long notifCount = 0;
    private transient NotificationBroadcasterSupport notifBroadcaster =
        new NotificationBroadcasterSupport();
    private transient MBeanNotificationInfo[] notifInfos = null;
    public CommunicatorServer(int connectorType)
        throws IllegalArgumentException {
        switch (connectorType) {
        case SNMP_TYPE :
            break;
        default:
            throw new IllegalArgumentException("Invalid connector Type") ;
        }
        dbgTag = makeDebugTag() ;
    }
    protected Thread createMainThread() {
        return new Thread (this, makeThreadName());
    }
    public void start(long timeout)
        throws CommunicationException, InterruptedException {
        boolean start;
        synchronized (stateLock) {
            if (state == STOPPING) {
                waitState(OFFLINE, 60000);
            }
            start = (state == OFFLINE);
            if (start) {
                changeState(STARTING);
                stopRequested = false;
                interrupted = false;
                startException = null;
            }
        }
        if (!start) {
            if (SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINER)) {
                SNMP_ADAPTOR_LOGGER.logp(Level.FINER, dbgTag,
                    "start","Connector is not OFFLINE");
            }
            return;
        }
        if (SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINER)) {
            SNMP_ADAPTOR_LOGGER.logp(Level.FINER, dbgTag,
                "start","--> Start connector ");
        }
        mainThread = createMainThread();
        mainThread.start() ;
        if (timeout > 0) waitForStart(timeout);
    }
    public void start() {
        try {
            start(0);
        } catch (InterruptedException x) {
            if (SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINER)) {
                SNMP_ADAPTOR_LOGGER.logp(Level.FINER, dbgTag,
                    "start","interrupted", x);
            }
        }
    }
    public void stop() {
        synchronized (stateLock) {
            if (state == OFFLINE || state == STOPPING) {
                if (SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINER)) {
                    SNMP_ADAPTOR_LOGGER.logp(Level.FINER, dbgTag,
                        "stop","Connector is not ONLINE");
                }
                return;
            }
            changeState(STOPPING);
            if (SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINER)) {
                SNMP_ADAPTOR_LOGGER.logp(Level.FINER, dbgTag,
                    "stop","Interrupt main thread");
            }
            stopRequested = true ;
            if (!interrupted) {
                interrupted = true;
                mainThread.interrupt();
            }
        }
        if (SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINER)) {
            SNMP_ADAPTOR_LOGGER.logp(Level.FINER, dbgTag,
                "stop","terminateAllClient");
        }
        terminateAllClient() ;
        synchronized (stateLock) {
            if (state == STARTING)
                changeState(OFFLINE);
        }
    }
    public boolean isActive() {
        synchronized (stateLock) {
            return (state == ONLINE);
        }
    }
    public boolean waitState(int wantedState, long timeOut) {
        if (SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINER)) {
            SNMP_ADAPTOR_LOGGER.logp(Level.FINER, dbgTag,
                "waitState", wantedState + "(0on,1off,2st) TO=" + timeOut +
                  " ; current state = " + getStateString());
        }
        long endTime = 0;
        if (timeOut > 0)
            endTime = System.currentTimeMillis() + timeOut;
        synchronized (stateLock) {
            while (state != wantedState) {
                if (timeOut < 0) {
                    if (SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINER)) {
                        SNMP_ADAPTOR_LOGGER.logp(Level.FINER, dbgTag,
                            "waitState", "timeOut < 0, return without wait");
                    }
                    return false;
                } else {
                    try {
                        if (timeOut > 0) {
                            long toWait = endTime - System.currentTimeMillis();
                            if (toWait <= 0) {
                                if (SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINER)) {
                                    SNMP_ADAPTOR_LOGGER.logp(Level.FINER, dbgTag,
                                        "waitState", "timed out");
                                }
                                return false;
                            }
                            stateLock.wait(toWait);
                        } else {  
                            stateLock.wait();
                        }
                    } catch (InterruptedException e) {
                        if (SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINER)) {
                            SNMP_ADAPTOR_LOGGER.logp(Level.FINER, dbgTag,
                                "waitState", "wait interrupted");
                        }
                        return (state == wantedState);
                    }
                }
            }
            if (SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINER)) {
                SNMP_ADAPTOR_LOGGER.logp(Level.FINER, dbgTag,
                    "waitState","returning in desired state");
            }
            return true;
        }
    }
    private void waitForStart(long timeout)
        throws CommunicationException, InterruptedException {
        if (SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINER)) {
            SNMP_ADAPTOR_LOGGER.logp(Level.FINER, dbgTag,
                "waitForStart", "Timeout=" + timeout +
                 " ; current state = " + getStateString());
        }
        final long startTime = System.currentTimeMillis();
        synchronized (stateLock) {
            while (state == STARTING) {
                final long elapsed = System.currentTimeMillis() - startTime;
                final long remainingTime = timeout-elapsed;
                if (remainingTime < 0) {
                    if (SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINER)) {
                        SNMP_ADAPTOR_LOGGER.logp(Level.FINER, dbgTag,
                            "waitForStart", "timeout < 0, return without wait");
                    }
                    throw new InterruptedException("Timeout expired");
                }
                try {
                    stateLock.wait(remainingTime);
                } catch (InterruptedException e) {
                    if (SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINER)) {
                        SNMP_ADAPTOR_LOGGER.logp(Level.FINER, dbgTag,
                            "waitForStart", "wait interrupted");
                    }
                    if (state != ONLINE) throw e;
                }
            }
            if (state == ONLINE) {
                if (SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINER)) {
                    SNMP_ADAPTOR_LOGGER.logp(Level.FINER, dbgTag,
                        "waitForStart", "started");
                }
                return;
            } else if (startException instanceof CommunicationException) {
                throw (CommunicationException)startException;
            } else if (startException instanceof InterruptedException) {
                throw (InterruptedException)startException;
            } else if (startException != null) {
                throw new CommunicationException(startException,
                                                 "Failed to start: "+
                                                 startException);
            } else {
                throw new CommunicationException("Failed to start: state is "+
                                                 getStringForState(state));
            }
        }
    }
    public int getState() {
        synchronized (stateLock) {
            return state ;
        }
    }
    public String getStateString() {
        return getStringForState(state) ;
    }
    public String getHost() {
        try {
            host = InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            host = "Unknown host";
        }
        return host ;
    }
    public int getPort() {
        synchronized (stateLock) {
            return port ;
        }
    }
    public void setPort(int port) throws java.lang.IllegalStateException {
        synchronized (stateLock) {
            if ((state == ONLINE) || (state == STARTING))
                throw new IllegalStateException("Stop server before " +
                                                "carrying out this operation");
            this.port = port;
            dbgTag = makeDebugTag();
        }
    }
    public abstract String getProtocol() ;
    int getServedClientCount() {
        return servedClientCount ;
    }
    int getActiveClientCount() {
        int result = clientHandlerVector.size() ;
        return result ;
    }
    int getMaxActiveClientCount() {
        return maxActiveClientCount ;
    }
    void setMaxActiveClientCount(int c)
        throws java.lang.IllegalStateException {
        synchronized (stateLock) {
            if ((state == ONLINE) || (state == STARTING)) {
                throw new IllegalStateException(
                          "Stop server before carrying out this operation");
            }
            maxActiveClientCount = c ;
        }
    }
    void notifyClientHandlerCreated(ClientHandler h) {
        clientHandlerVector.addElement(h) ;
    }
    synchronized void notifyClientHandlerDeleted(ClientHandler h) {
        clientHandlerVector.removeElement(h);
        notifyAll();
    }
    protected int getBindTries() {
        return 50;
    }
    protected long getBindSleepTime() {
        return 100;
    }
    public void run() {
        int i = 0;
        boolean success = false;
        try {
            final int  bindRetries = getBindTries();
            final long sleepTime   = getBindSleepTime();
            while (i < bindRetries && !success) {
                try {
                    doBind();
                    success = true;
                } catch (CommunicationException ce) {
                    i++;
                    try {
                        Thread.sleep(sleepTime);
                    } catch (InterruptedException ie) {
                        throw ie;
                    }
                }
            }
            if (!success) {
                doBind();
            }
        } catch(Exception x) {
            if (SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINEST)) {
                SNMP_ADAPTOR_LOGGER.logp(Level.FINEST, dbgTag,
                    "run", "Got unexpected exception", x);
            }
            synchronized(stateLock) {
                startException = x;
                changeState(OFFLINE);
            }
            if (SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINER)) {
                SNMP_ADAPTOR_LOGGER.logp(Level.FINER, dbgTag,
                    "run","State is OFFLINE");
            }
            doError(x);
            return;
        }
        try {
            changeState(ONLINE) ;
            if (SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINER)) {
                SNMP_ADAPTOR_LOGGER.logp(Level.FINER, dbgTag,
                    "run","State is ONLINE");
            }
            while (!stopRequested) {
                servedClientCount++;
                doReceive() ;
                waitIfTooManyClients() ;
                doProcess() ;
            }
            if (SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINER)) {
                SNMP_ADAPTOR_LOGGER.logp(Level.FINER, dbgTag,
                    "run","Stop has been requested");
            }
        } catch(InterruptedException x) {
            if (SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINEST)) {
                SNMP_ADAPTOR_LOGGER.logp(Level.FINEST, dbgTag,
                    "run","Interrupt caught");
            }
            changeState(STOPPING);
        } catch(Exception x) {
            if (SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINEST)) {
                SNMP_ADAPTOR_LOGGER.logp(Level.FINEST, dbgTag,
                    "run","Got unexpected exception", x);
            }
            changeState(STOPPING);
        } finally {
            synchronized (stateLock) {
                interrupted = true;
                Thread.currentThread().interrupted();
            }
            try {
                doUnbind() ;
                waitClientTermination() ;
                changeState(OFFLINE);
                if (SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINER)) {
                    SNMP_ADAPTOR_LOGGER.logp(Level.FINER, dbgTag,
                        "run","State is OFFLINE");
                }
            } catch(Exception x) {
                if (SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINEST)) {
                    SNMP_ADAPTOR_LOGGER.logp(Level.FINEST, dbgTag,
                        "run","Got unexpected exception", x);
                }
                changeState(OFFLINE);
            }
        }
    }
    protected abstract void doError(Exception e) throws CommunicationException;
    protected abstract void doBind()
        throws CommunicationException, InterruptedException ;
    protected abstract void doReceive()
        throws CommunicationException, InterruptedException ;
    protected abstract void doProcess()
        throws CommunicationException, InterruptedException ;
    protected abstract void doUnbind()
        throws CommunicationException, InterruptedException ;
    public synchronized MBeanServer getMBeanServer() {
        return topMBS;
    }
    public synchronized void setMBeanServer(MBeanServer newMBS)
            throws IllegalArgumentException, IllegalStateException {
        synchronized (stateLock) {
            if (state == ONLINE || state == STARTING)
                throw new IllegalStateException("Stop server before " +
                                                "carrying out this operation");
        }
        final String error =
            "MBeanServer argument must be MBean server where this " +
            "server is registered, or an MBeanServerForwarder " +
            "leading to that server";
        Vector<MBeanServer> seenMBS = new Vector<MBeanServer>();
        for (MBeanServer mbs = newMBS;
             mbs != bottomMBS;
             mbs = ((MBeanServerForwarder) mbs).getMBeanServer()) {
            if (!(mbs instanceof MBeanServerForwarder))
                throw new IllegalArgumentException(error);
            if (seenMBS.contains(mbs))
                throw new IllegalArgumentException("MBeanServerForwarder " +
                                                   "loop");
            seenMBS.addElement(mbs);
        }
        topMBS = newMBS;
    }
    ObjectName getObjectName() {
        return objectName ;
    }
    void changeState(int newState) {
        int oldState;
        synchronized (stateLock) {
            if (state == newState)
                return;
            oldState = state;
            state = newState;
            stateLock.notifyAll();
        }
        sendStateChangeNotification(oldState, newState);
    }
    String makeDebugTag() {
        return "CommunicatorServer["+ getProtocol() + ":" + getPort() + "]" ;
    }
    String makeThreadName() {
        String result ;
        if (objectName == null)
            result = "CommunicatorServer" ;
        else
            result = objectName.toString() ;
        return result ;
    }
    private synchronized void waitIfTooManyClients()
        throws InterruptedException {
        while (getActiveClientCount() >= maxActiveClientCount) {
            if (SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINER)) {
                SNMP_ADAPTOR_LOGGER.logp(Level.FINER, dbgTag,
                    "waitIfTooManyClients","Waiting for a client to terminate");
            }
            wait();
        }
    }
    private void waitClientTermination() {
        int s = clientHandlerVector.size() ;
        if (SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINER)) {
            if (s >= 1) {
                SNMP_ADAPTOR_LOGGER.logp(Level.FINER, dbgTag,
                "waitClientTermination","waiting for " +
                      s + " clients to terminate");
            }
        }
        while (! clientHandlerVector.isEmpty()) {
            try {
                clientHandlerVector.firstElement().join();
            } catch (NoSuchElementException x) {
                if (SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINER)) {
                    SNMP_ADAPTOR_LOGGER.logp(Level.FINER, dbgTag,
                        "waitClientTermination","No elements left",  x);
                }
            }
        }
        if (SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINER)) {
            if (s >= 1) {
                SNMP_ADAPTOR_LOGGER.logp(Level.FINER, dbgTag,
                    "waitClientTermination","Ok, let's go...");
            }
        }
    }
    private void terminateAllClient() {
        final int s = clientHandlerVector.size() ;
        if (SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINER)) {
            if (s >= 1) {
                SNMP_ADAPTOR_LOGGER.logp(Level.FINER, dbgTag,
                    "terminateAllClient","Interrupting " + s + " clients");
            }
        }
        final  ClientHandler[] handlers =
                clientHandlerVector.toArray(new ClientHandler[0]);
         for (ClientHandler h : handlers) {
             try {
                 h.interrupt() ;
             } catch (Exception x) {
                 if (SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINER)) {
                     SNMP_ADAPTOR_LOGGER.logp(Level.FINER, dbgTag,
                             "terminateAllClient",
                             "Failed to interrupt pending request. " +
                             "Ignore the exception.", x);
                 }
            }
        }
    }
    private void readObject(ObjectInputStream stream)
        throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        stateLock = new Object();
        state = OFFLINE;
        stopRequested = false;
        servedClientCount = 0;
        clientHandlerVector = new Vector<ClientHandler>();
        fatherThread = Thread.currentThread();
        mainThread = null;
        notifCount = 0;
        notifInfos = null;
        notifBroadcaster = new NotificationBroadcasterSupport();
        dbgTag = makeDebugTag();
    }
    public void addNotificationListener(NotificationListener listener,
                                        NotificationFilter filter,
                                        Object handback)
        throws java.lang.IllegalArgumentException {
        if (SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINEST)) {
            SNMP_ADAPTOR_LOGGER.logp(Level.FINEST, dbgTag,
                "addNotificationListener","Adding listener "+ listener +
                  " with filter "+ filter + " and handback "+ handback);
        }
        notifBroadcaster.addNotificationListener(listener, filter, handback);
    }
    public void removeNotificationListener(NotificationListener listener)
        throws ListenerNotFoundException {
        if (SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINEST)) {
            SNMP_ADAPTOR_LOGGER.logp(Level.FINEST, dbgTag,
                "removeNotificationListener","Removing listener "+ listener);
        }
        notifBroadcaster.removeNotificationListener(listener);
    }
    public MBeanNotificationInfo[] getNotificationInfo() {
        if (notifInfos == null) {
            notifInfos = new MBeanNotificationInfo[1];
            String[] notifTypes = {
                AttributeChangeNotification.ATTRIBUTE_CHANGE};
            notifInfos[0] = new MBeanNotificationInfo( notifTypes,
                     AttributeChangeNotification.class.getName(),
                     "Sent to notify that the value of the State attribute "+
                     "of this CommunicatorServer instance has changed.");
        }
        return notifInfos;
    }
    private void sendStateChangeNotification(int oldState, int newState) {
        String oldStateString = getStringForState(oldState);
        String newStateString = getStringForState(newState);
        String message = new StringBuffer().append(dbgTag)
            .append(" The value of attribute State has changed from ")
            .append(oldState).append(" (").append(oldStateString)
            .append(") to ").append(newState).append(" (")
            .append(newStateString).append(").").toString();
        notifCount++;
        AttributeChangeNotification notif =
            new AttributeChangeNotification(this,    
                         notifCount,                 
                         System.currentTimeMillis(), 
                         message,                    
                         "State",                    
                         "int",                      
                         new Integer(oldState),      
                         new Integer(newState) );    
        if (SNMP_ADAPTOR_LOGGER.isLoggable(Level.FINEST)) {
            SNMP_ADAPTOR_LOGGER.logp(Level.FINEST, dbgTag,
                "sendStateChangeNotification","Sending AttributeChangeNotification #"
                    + notifCount + " with message: "+ message);
        }
        notifBroadcaster.sendNotification(notif);
    }
    private static String getStringForState(int s) {
        switch (s) {
        case ONLINE:   return "ONLINE";
        case STARTING: return "STARTING";
        case OFFLINE:  return "OFFLINE";
        case STOPPING: return "STOPPING";
        default:       return "UNDEFINED";
        }
    }
    public ObjectName preRegister(MBeanServer server, ObjectName name)
            throws java.lang.Exception {
        objectName = name;
        synchronized (this) {
            if (bottomMBS != null) {
                throw new IllegalArgumentException("connector already " +
                                                   "registered in an MBean " +
                                                   "server");
            }
            topMBS = bottomMBS = server;
        }
        dbgTag = makeDebugTag();
        return name;
    }
    public void postRegister(Boolean registrationDone) {
        if (!registrationDone.booleanValue()) {
            synchronized (this) {
                topMBS = bottomMBS = null;
            }
        }
    }
    public void preDeregister() throws java.lang.Exception {
        synchronized (this) {
            topMBS = bottomMBS = null;
        }
        objectName = null ;
        final int cstate = getState();
        if ((cstate == ONLINE) || ( cstate == STARTING)) {
            stop() ;
        }
    }
    public void postDeregister(){
    }
    Class loadClass(String className)
        throws ClassNotFoundException {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            final ClassLoaderRepository clr =
                MBeanServerFactory.getClassLoaderRepository(bottomMBS);
            if (clr == null) throw new ClassNotFoundException(className);
            return clr.loadClass(className);
        }
    }
}
