abstract class ClientHandler implements Runnable {
    public ClientHandler(CommunicatorServer server, int id, MBeanServer f, ObjectName n) {
        adaptorServer = server ;
        requestId = id ;
        mbs = f ;
        objectName = n ;
        interruptCalled = false ;
        dbgTag = makeDebugTag() ;
        thread =  createThread(this);
    }
    Thread createThread(Runnable r) {
        return new Thread(this);
    }
    public void interrupt() {
        SNMP_ADAPTOR_LOGGER.entering(dbgTag, "interrupt");
        interruptCalled = true ;
        if (thread != null) {
            thread.interrupt() ;
        }
        SNMP_ADAPTOR_LOGGER.exiting(dbgTag, "interrupt");
    }
    public void join() {
        if (thread != null) {
        try {
            thread.join() ;
        }
        catch(InterruptedException x) {
        }
        }
    }
    public void run() {
        try {
            adaptorServer.notifyClientHandlerCreated(this) ;
            doRun() ;
        }
        finally {
            adaptorServer.notifyClientHandlerDeleted(this) ;
        }
    }
    public abstract void doRun() ;
    protected CommunicatorServer adaptorServer = null ;
    protected int requestId = -1 ;
    protected MBeanServer mbs = null ;
    protected ObjectName objectName = null ;
    protected Thread thread = null ;
    protected boolean interruptCalled = false ;
    protected String dbgTag = null ;
    protected String makeDebugTag() {
        return "ClientHandler[" + adaptorServer.getProtocol() + ":" + adaptorServer.getPort() + "][" + requestId + "]";
    }
}
