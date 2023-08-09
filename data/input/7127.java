public class SelectorImpl
    extends
        Thread
    implements
        com.sun.corba.se.pept.transport.Selector
{
    private ORB orb;
    private Selector selector;
    private long timeout;
    private List deferredRegistrations;
    private List interestOpsList;
    private HashMap listenerThreads;
    private Map readerThreads;
    private boolean selectorStarted;
    private boolean closed;
    private ORBUtilSystemException wrapper ;
    public SelectorImpl(ORB orb)
    {
        this.orb = orb;
        selector = null;
        selectorStarted = false;
        timeout = 60000;
        deferredRegistrations = new ArrayList();
        interestOpsList = new ArrayList();
        listenerThreads = new HashMap();
        readerThreads = java.util.Collections.synchronizedMap(new HashMap());
        closed = false;
        wrapper = ORBUtilSystemException.get(orb,CORBALogDomains.RPC_TRANSPORT);
    }
    public void setTimeout(long timeout)
    {
        this.timeout = timeout;
    }
    public long getTimeout()
    {
        return timeout;
    }
    public void registerInterestOps(EventHandler eventHandler)
    {
        if (orb.transportDebugFlag) {
            dprint(".registerInterestOps:-> " + eventHandler);
        }
        SelectionKey selectionKey = eventHandler.getSelectionKey();
        if (selectionKey.isValid()) {
            int ehOps = eventHandler.getInterestOps();
            SelectionKeyAndOp keyAndOp = new SelectionKeyAndOp(selectionKey, ehOps);
            synchronized(interestOpsList) {
                interestOpsList.add(keyAndOp);
            }
            selector.wakeup();
        }
        else {
            wrapper.selectionKeyInvalid(eventHandler.toString());
            if (orb.transportDebugFlag) {
                dprint(".registerInterestOps: EventHandler SelectionKey not valid " + eventHandler);
            }
        }
        if (orb.transportDebugFlag) {
            dprint(".registerInterestOps:<- ");
        }
    }
    public void registerForEvent(EventHandler eventHandler)
    {
        if (orb.transportDebugFlag) {
            dprint(".registerForEvent: " + eventHandler);
        }
        if (isClosed()) {
            if (orb.transportDebugFlag) {
                dprint(".registerForEvent: closed: " + eventHandler);
            }
            return;
        }
        if (eventHandler.shouldUseSelectThreadToWait()) {
            synchronized (deferredRegistrations) {
                deferredRegistrations.add(eventHandler);
            }
            if (! selectorStarted) {
                startSelector();
            }
            selector.wakeup();
            return;
        }
        switch (eventHandler.getInterestOps()) {
        case SelectionKey.OP_ACCEPT :
            createListenerThread(eventHandler);
            break;
        case SelectionKey.OP_READ :
            createReaderThread(eventHandler);
            break;
        default:
            if (orb.transportDebugFlag) {
                dprint(".registerForEvent: default: " + eventHandler);
            }
            throw new RuntimeException(
                "SelectorImpl.registerForEvent: unknown interest ops");
        }
    }
    public void unregisterForEvent(EventHandler eventHandler)
    {
        if (orb.transportDebugFlag) {
            dprint(".unregisterForEvent: " + eventHandler);
        }
        if (isClosed()) {
            if (orb.transportDebugFlag) {
                dprint(".unregisterForEvent: closed: " + eventHandler);
            }
            return;
        }
        if (eventHandler.shouldUseSelectThreadToWait()) {
            SelectionKey selectionKey ;
            synchronized(deferredRegistrations) {
                selectionKey = eventHandler.getSelectionKey();
            }
            if (selectionKey != null) {
                selectionKey.cancel();
            }
            selector.wakeup();
            return;
        }
        switch (eventHandler.getInterestOps()) {
        case SelectionKey.OP_ACCEPT :
            destroyListenerThread(eventHandler);
            break;
        case SelectionKey.OP_READ :
            destroyReaderThread(eventHandler);
            break;
        default:
            if (orb.transportDebugFlag) {
                dprint(".unregisterForEvent: default: " + eventHandler);
            }
            throw new RuntimeException(
                "SelectorImpl.uregisterForEvent: unknown interest ops");
        }
    }
    public void close()
    {
        if (orb.transportDebugFlag) {
            dprint(".close");
        }
        if (isClosed()) {
            if (orb.transportDebugFlag) {
                dprint(".close: already closed");
            }
            return;
        }
        setClosed(true);
        Iterator i;
        i = listenerThreads.values().iterator();
        while (i.hasNext()) {
            ListenerThread listenerThread = (ListenerThread) i.next();
            listenerThread.close();
        }
        i = readerThreads.values().iterator();
        while (i.hasNext()) {
            ReaderThread readerThread = (ReaderThread) i.next();
            readerThread.close();
        }
        try {
            if (selector != null) {
                selector.wakeup();
            }
        } catch (Throwable t) {
            if (orb.transportDebugFlag) {
                dprint(".close: selector.close: " + t);
            }
        }
    }
    public void run()
    {
        setName("SelectorThread");
        while (!closed) {
            try {
                int n = 0;
                if (timeout == 0 && orb.transportDebugFlag) {
                    dprint(".run: Beginning of selection cycle");
                }
                handleDeferredRegistrations();
                enableInterestOps();
                try {
                    n = selector.select(timeout);
                } catch (IOException  e) {
                    if (orb.transportDebugFlag) {
                        dprint(".run: selector.select: " + e);
                    }
                }
                if (closed) {
                    selector.close();
                    if (orb.transportDebugFlag) {
                        dprint(".run: closed - .run return");
                    }
                    return;
                }
                Iterator iterator = selector.selectedKeys().iterator();
                if (orb.transportDebugFlag) {
                    if (iterator.hasNext()) {
                        dprint(".run: n = " + n);
                    }
                }
                while (iterator.hasNext()) {
                    SelectionKey selectionKey = (SelectionKey) iterator.next();
                    iterator.remove();
                    EventHandler eventHandler = (EventHandler)
                        selectionKey.attachment();
                    try {
                        eventHandler.handleEvent();
                    } catch (Throwable t) {
                        if (orb.transportDebugFlag) {
                            dprint(".run: eventHandler.handleEvent", t);
                        }
                    }
                }
                if (timeout == 0 && orb.transportDebugFlag) {
                    dprint(".run: End of selection cycle");
                }
            } catch (Throwable t) {
                if (orb.transportDebugFlag) {
                    dprint(".run: ignoring", t);
                }
            }
        }
    }
    private synchronized boolean isClosed ()
    {
        return closed;
    }
    private synchronized void setClosed(boolean closed)
    {
        this.closed = closed;
    }
    private void startSelector()
    {
        try {
            selector = Selector.open();
        } catch (IOException e) {
            if (orb.transportDebugFlag) {
                dprint(".startSelector: Selector.open: IOException: " + e);
            }
            RuntimeException rte =
                new RuntimeException(".startSelector: Selector.open exception");
            rte.initCause(e);
            throw rte;
        }
        setDaemon(true);
        start();
        selectorStarted = true;
        if (orb.transportDebugFlag) {
            dprint(".startSelector: selector.start completed.");
        }
    }
    private void handleDeferredRegistrations()
    {
        synchronized (deferredRegistrations) {
            int deferredListSize = deferredRegistrations.size();
            for (int i = 0; i < deferredListSize; i++) {
                EventHandler eventHandler =
                    (EventHandler)deferredRegistrations.get(i);
                if (orb.transportDebugFlag) {
                    dprint(".handleDeferredRegistrations: " + eventHandler);
                }
                SelectableChannel channel = eventHandler.getChannel();
                SelectionKey selectionKey = null;
                try {
                    selectionKey =
                        channel.register(selector,
                                         eventHandler.getInterestOps(),
                                         (Object)eventHandler);
                } catch (ClosedChannelException e) {
                    if (orb.transportDebugFlag) {
                        dprint(".handleDeferredRegistrations: " + e);
                    }
                }
                eventHandler.setSelectionKey(selectionKey);
            }
            deferredRegistrations.clear();
        }
    }
    private void enableInterestOps()
    {
        synchronized (interestOpsList) {
            int listSize = interestOpsList.size();
            if (listSize > 0) {
                if (orb.transportDebugFlag) {
                    dprint(".enableInterestOps:->");
                }
                SelectionKey selectionKey = null;
                SelectionKeyAndOp keyAndOp = null;
                int keyOp, selectionKeyOps = 0;
                for (int i = 0; i < listSize; i++) {
                    keyAndOp = (SelectionKeyAndOp)interestOpsList.get(i);
                    selectionKey = keyAndOp.selectionKey;
                    if (selectionKey.isValid()) {
                        if (orb.transportDebugFlag) {
                            dprint(".enableInterestOps: " + keyAndOp);
                        }
                        keyOp = keyAndOp.keyOp;
                        selectionKeyOps = selectionKey.interestOps();
                        selectionKey.interestOps(selectionKeyOps | keyOp);
                    }
                }
                interestOpsList.clear();
                if (orb.transportDebugFlag) {
                    dprint(".enableInterestOps:<-");
                }
            }
        }
    }
    private void createListenerThread(EventHandler eventHandler)
    {
        if (orb.transportDebugFlag) {
            dprint(".createListenerThread: " + eventHandler);
        }
        Acceptor acceptor = eventHandler.getAcceptor();
        ListenerThread listenerThread =
            new ListenerThreadImpl(orb, acceptor, this);
        listenerThreads.put(eventHandler, listenerThread);
        Throwable throwable = null;
        try {
            orb.getThreadPoolManager().getThreadPool(0)
                .getWorkQueue(0).addWork((Work)listenerThread);
        } catch (NoSuchThreadPoolException e) {
            throwable = e;
        } catch (NoSuchWorkQueueException e) {
            throwable = e;
        }
        if (throwable != null) {
            RuntimeException rte = new RuntimeException(throwable.toString());
            rte.initCause(throwable);
            throw rte;
        }
    }
    private void destroyListenerThread(EventHandler eventHandler)
    {
        if (orb.transportDebugFlag) {
            dprint(".destroyListenerThread: " + eventHandler);
        }
        ListenerThread listenerThread = (ListenerThread)
            listenerThreads.get(eventHandler);
        if (listenerThread == null) {
            if (orb.transportDebugFlag) {
                dprint(".destroyListenerThread: cannot find ListenerThread - ignoring.");
            }
            return;
        }
        listenerThreads.remove(eventHandler);
        listenerThread.close();
    }
    private void createReaderThread(EventHandler eventHandler)
    {
        if (orb.transportDebugFlag) {
            dprint(".createReaderThread: " + eventHandler);
        }
        Connection connection = eventHandler.getConnection();
        ReaderThread readerThread =
            new ReaderThreadImpl(orb, connection, this);
        readerThreads.put(eventHandler, readerThread);
        Throwable throwable = null;
        try {
            orb.getThreadPoolManager().getThreadPool(0)
                .getWorkQueue(0).addWork((Work)readerThread);
        } catch (NoSuchThreadPoolException e) {
            throwable = e;
        } catch (NoSuchWorkQueueException e) {
            throwable = e;
        }
        if (throwable != null) {
            RuntimeException rte = new RuntimeException(throwable.toString());
            rte.initCause(throwable);
            throw rte;
        }
    }
    private void destroyReaderThread(EventHandler eventHandler)
    {
        if (orb.transportDebugFlag) {
            dprint(".destroyReaderThread: " + eventHandler);
        }
        ReaderThread readerThread = (ReaderThread)
            readerThreads.get(eventHandler);
        if (readerThread == null) {
            if (orb.transportDebugFlag) {
                dprint(".destroyReaderThread: cannot find ReaderThread - ignoring.");
            }
            return;
        }
        readerThreads.remove(eventHandler);
        readerThread.close();
    }
    private void dprint(String msg)
    {
        ORBUtility.dprint("SelectorImpl", msg);
    }
    protected void dprint(String msg, Throwable t)
    {
        dprint(msg);
        t.printStackTrace(System.out);
    }
    private class SelectionKeyAndOp
    {
        public int keyOp;
        public SelectionKey selectionKey;
        public SelectionKeyAndOp(SelectionKey selectionKey, int keyOp) {
            this.selectionKey = selectionKey;
            this.keyOp = keyOp;
        }
    }
}
