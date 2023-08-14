public class ListenerThreadImpl
    implements
        ListenerThread,
        Work
{
    private ORB orb;
    private Acceptor acceptor;
    private Selector selector;
    private boolean keepRunning;
    private long enqueueTime;
    public ListenerThreadImpl(ORB orb, Acceptor acceptor, Selector selector)
    {
        this.orb = orb;
        this.acceptor = acceptor;
        this.selector = selector;
        keepRunning = true;
    }
    public Acceptor getAcceptor()
    {
        return acceptor;
    }
    public void close()
    {
        if (orb.transportDebugFlag) {
            dprint(".close: " + acceptor);
        }
        keepRunning = false;
    }
    public void doWork()
    {
        try {
            if (orb.transportDebugFlag) {
                dprint(".doWork: Start ListenerThread: " + acceptor);
            }
            while (keepRunning) {
                try {
                    if (orb.transportDebugFlag) {
                        dprint(".doWork: BEFORE ACCEPT CYCLE: " + acceptor);
                    }
                    acceptor.accept();
                    if (orb.transportDebugFlag) {
                        dprint(".doWork: AFTER ACCEPT CYCLE: " + acceptor);
                    }
                } catch (Throwable t) {
                    if (orb.transportDebugFlag) {
                        dprint(".doWork: Exception in accept: " + acceptor,t);
                    }
                    orb.getTransportManager().getSelector(0)
                        .unregisterForEvent(getAcceptor().getEventHandler());
                    getAcceptor().close();
                }
            }
        } finally {
            if (orb.transportDebugFlag) {
                dprint(".doWork: Terminated ListenerThread: " + acceptor);
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
    public String getName() { return "ListenerThread"; }
    private void dprint(String msg)
    {
        ORBUtility.dprint("ListenerThreadImpl", msg);
    }
    private void dprint(String msg, Throwable t)
    {
        dprint(msg);
        t.printStackTrace(System.out);
    }
}
