public abstract class ClientNotifForwarder {
    private final AccessControlContext acc;
    public ClientNotifForwarder(Map env) {
        this(null, env);
    }
    private static int threadId;
    private static class LinearExecutor implements Executor {
        public synchronized void execute(Runnable command) {
            if (this.command != null)
                throw new IllegalArgumentException("More than one command");
            this.command = command;
            if (thread == null) {
                thread = new Thread() {
                    @Override
                    public void run() {
                        while (true) {
                            Runnable r;
                            synchronized (LinearExecutor.this) {
                                if (LinearExecutor.this.command == null) {
                                    thread = null;
                                    return;
                                } else {
                                    r = LinearExecutor.this.command;
                                    LinearExecutor.this.command = null;
                                }
                            }
                            r.run();
                        }
                    }
                };
                thread.setDaemon(true);
                thread.setName("ClientNotifForwarder-" + ++threadId);
                thread.start();
            }
        }
        private Runnable command;
        private Thread thread;
    }
    public ClientNotifForwarder(ClassLoader defaultClassLoader, Map<String, ?> env) {
        maxNotifications = EnvHelp.getMaxFetchNotifNumber(env);
        timeout = EnvHelp.getFetchTimeout(env);
        Executor ex = (Executor)
            env.get("jmx.remote.x.fetch.notifications.executor");
        if (ex == null)
            ex = new LinearExecutor();
        else if (logger.traceOn())
            logger.trace("ClientNotifForwarder", "executor is " + ex);
        this.defaultClassLoader = defaultClassLoader;
        this.executor = ex;
        this.acc = AccessController.getContext();
    }
    abstract protected NotificationResult fetchNotifs(long clientSequenceNumber,
                                                      int maxNotifications,
                                                      long timeout)
            throws IOException, ClassNotFoundException;
    abstract protected Integer addListenerForMBeanRemovedNotif()
        throws IOException, InstanceNotFoundException;
    abstract protected void removeListenerForMBeanRemovedNotif(Integer id)
        throws IOException, InstanceNotFoundException,
               ListenerNotFoundException;
    abstract protected void lostNotifs(String message, long number);
    public synchronized void addNotificationListener(Integer listenerID,
                                        ObjectName name,
                                        NotificationListener listener,
                                        NotificationFilter filter,
                                        Object handback,
                                        Subject delegationSubject)
            throws IOException, InstanceNotFoundException {
        if (logger.traceOn()) {
            logger.trace("addNotificationListener",
                         "Add the listener "+listener+" at "+name);
        }
        infoList.put(listenerID,
                     new ClientListenerInfo(listenerID,
                                            name,
                                            listener,
                                            filter,
                                            handback,
                                            delegationSubject));
        init(false);
    }
    public synchronized Integer[]
        removeNotificationListener(ObjectName name,
                                   NotificationListener listener)
        throws ListenerNotFoundException, IOException {
        beforeRemove();
        if (logger.traceOn()) {
            logger.trace("removeNotificationListener",
                         "Remove the listener "+listener+" from "+name);
        }
        List<Integer> ids = new ArrayList<Integer>();
        List<ClientListenerInfo> values =
                new ArrayList<ClientListenerInfo>(infoList.values());
        for (int i=values.size()-1; i>=0; i--) {
            ClientListenerInfo li = values.get(i);
            if (li.sameAs(name, listener)) {
                ids.add(li.getListenerID());
                infoList.remove(li.getListenerID());
            }
        }
        if (ids.isEmpty())
            throw new ListenerNotFoundException("Listener not found");
        return ids.toArray(new Integer[0]);
    }
    public synchronized Integer
        removeNotificationListener(ObjectName name,
                                   NotificationListener listener,
                                   NotificationFilter filter,
                                   Object handback)
            throws ListenerNotFoundException, IOException {
        if (logger.traceOn()) {
            logger.trace("removeNotificationListener",
                         "Remove the listener "+listener+" from "+name);
        }
        beforeRemove();
        Integer id = null;
        List<ClientListenerInfo> values =
                new ArrayList<ClientListenerInfo>(infoList.values());
        for (int i=values.size()-1; i>=0; i--) {
            ClientListenerInfo li = values.get(i);
            if (li.sameAs(name, listener, filter, handback)) {
                id=li.getListenerID();
                infoList.remove(id);
                break;
            }
        }
        if (id == null)
            throw new ListenerNotFoundException("Listener not found");
        return id;
    }
    public synchronized Integer[] removeNotificationListener(ObjectName name) {
        if (logger.traceOn()) {
            logger.trace("removeNotificationListener",
                         "Remove all listeners registered at "+name);
        }
        List<Integer> ids = new ArrayList<Integer>();
        List<ClientListenerInfo> values =
                new ArrayList<ClientListenerInfo>(infoList.values());
        for (int i=values.size()-1; i>=0; i--) {
            ClientListenerInfo li = values.get(i);
            if (li.sameAs(name)) {
                ids.add(li.getListenerID());
                infoList.remove(li.getListenerID());
            }
        }
        return ids.toArray(new Integer[0]);
    }
    public synchronized ClientListenerInfo[] preReconnection() throws IOException {
        if (state == TERMINATED || beingReconnected) { 
            throw new IOException("Illegal state.");
        }
        final ClientListenerInfo[] tmp =
            infoList.values().toArray(new ClientListenerInfo[0]);
        beingReconnected = true;
        infoList.clear();
        return tmp;
    }
    public synchronized void postReconnection(ClientListenerInfo[] listenerInfos)
        throws IOException {
        if (state == TERMINATED) {
            return;
        }
        while (state == STOPPING) {
            try {
                wait();
            } catch (InterruptedException ire) {
                IOException ioe = new IOException(ire.toString());
                EnvHelp.initCause(ioe, ire);
                throw ioe;
            }
        }
        final boolean trace = logger.traceOn();
        final int len   = listenerInfos.length;
        for (int i=0; i<len; i++) {
            if (trace) {
                logger.trace("addNotificationListeners",
                             "Add a listener at "+
                             listenerInfos[i].getListenerID());
            }
            infoList.put(listenerInfos[i].getListenerID(), listenerInfos[i]);
        }
        beingReconnected = false;
        notifyAll();
        if (currentFetchThread == Thread.currentThread() ||
              state == STARTING || state == STARTED) { 
            try {
                mbeanRemovedNotifID = addListenerForMBeanRemovedNotif();
            } catch (Exception e) {
                final String msg =
                    "Failed to register a listener to the mbean " +
                    "server: the client will not do clean when an MBean " +
                    "is unregistered";
                if (logger.traceOn()) {
                    logger.trace("init", msg, e);
                }
            }
        } else {
              while (state == STOPPING) {
                  try {
                      wait();
                  } catch (InterruptedException ire) {
                      IOException ioe = new IOException(ire.toString());
                      EnvHelp.initCause(ioe, ire);
                      throw ioe;
                  }
              }
              if (listenerInfos.length > 0) { 
                  init(true); 
              } else if (infoList.size() > 0) { 
                  init(false); 
              }
          }
    }
    public synchronized void terminate() {
        if (state == TERMINATED) {
            return;
        }
        if (logger.traceOn()) {
            logger.trace("terminate", "Terminating...");
        }
        if (state == STARTED) {
           infoList.clear();
        }
        setState(TERMINATED);
    }
    private class NotifFetcher implements Runnable {
        private volatile boolean alreadyLogged = false;
        private void logOnce(String msg, SecurityException x) {
            if (alreadyLogged) return;
            logger.config("setContextClassLoader",msg);
            if (x != null) logger.fine("setContextClassLoader", x);
            alreadyLogged = true;
        }
        private final ClassLoader setContextClassLoader(final ClassLoader loader) {
            final AccessControlContext ctxt = ClientNotifForwarder.this.acc;
            if (ctxt == null) {
                logOnce("AccessControlContext must not be null.",null);
                throw new SecurityException("AccessControlContext must not be null");
            }
            return AccessController.doPrivileged(
                new PrivilegedAction<ClassLoader>() {
                    public ClassLoader run() {
                        try {
                            final ClassLoader previous =
                                Thread.currentThread().getContextClassLoader();
                            if (loader == previous) return previous;
                            Thread.currentThread().setContextClassLoader(loader);
                            return previous;
                        } catch (SecurityException x) {
                            logOnce("Permission to set ContextClassLoader missing. " +
                                    "Notifications will not be dispatched. " +
                                    "Please check your Java policy configuration: " +
                                    x, x);
                            throw x;
                        }
                    }
                }, ctxt);
        }
        public void run() {
            final ClassLoader previous;
            if (defaultClassLoader != null) {
                previous = setContextClassLoader(defaultClassLoader);
            } else {
                previous = null;
            }
            try {
                doRun();
            } finally {
                if (defaultClassLoader != null) {
                    setContextClassLoader(previous);
                }
            }
        }
        private void doRun() {
            synchronized (ClientNotifForwarder.this) {
                currentFetchThread = Thread.currentThread();
                if (state == STARTING) {
                    setState(STARTED);
                }
            }
            NotificationResult nr = null;
            if (!shouldStop() && (nr = fetchNotifs()) != null) {
                final TargetedNotification[] notifs =
                    nr.getTargetedNotifications();
                final int len = notifs.length;
                final Map<Integer, ClientListenerInfo> listeners;
                final Integer myListenerID;
                long missed = 0;
                synchronized(ClientNotifForwarder.this) {
                    if (clientSequenceNumber >= 0) {
                        missed = nr.getEarliestSequenceNumber() -
                            clientSequenceNumber;
                    }
                    clientSequenceNumber = nr.getNextSequenceNumber();
                    listeners = new HashMap<Integer, ClientListenerInfo>();
                    for (int i = 0 ; i < len ; i++) {
                        final TargetedNotification tn = notifs[i];
                        final Integer listenerID = tn.getListenerID();
                        if (!listenerID.equals(mbeanRemovedNotifID)) {
                            final ClientListenerInfo li = infoList.get(listenerID);
                            if (li != null) {
                                listeners.put(listenerID, li);
                            }
                            continue;
                        }
                        final Notification notif = tn.getNotification();
                        final String unreg =
                            MBeanServerNotification.UNREGISTRATION_NOTIFICATION;
                        if (notif instanceof MBeanServerNotification &&
                            notif.getType().equals(unreg)) {
                            MBeanServerNotification mbsn =
                                (MBeanServerNotification) notif;
                            ObjectName name = mbsn.getMBeanName();
                            removeNotificationListener(name);
                        }
                    }
                    myListenerID = mbeanRemovedNotifID;
                }
                if (missed > 0) {
                    final String msg =
                        "May have lost up to " + missed +
                        " notification" + (missed == 1 ? "" : "s");
                    lostNotifs(msg, missed);
                    logger.trace("NotifFetcher.run", msg);
                }
                for (int i = 0 ; i < len ; i++) {
                    final TargetedNotification tn = notifs[i];
                    dispatchNotification(tn,myListenerID,listeners);
                }
            }
            synchronized (ClientNotifForwarder.this) {
                currentFetchThread = null;
            }
            if (nr == null || shouldStop()) {
                setState(STOPPED);
                try {
                      removeListenerForMBeanRemovedNotif(mbeanRemovedNotifID);
                } catch (Exception e) {
                    if (logger.traceOn()) {
                        logger.trace("NotifFetcher-run",
                                "removeListenerForMBeanRemovedNotif", e);
                    }
                }
            } else {
                executor.execute(this);
            }
        }
        void dispatchNotification(TargetedNotification tn,
                                  Integer myListenerID,
                                  Map<Integer, ClientListenerInfo> listeners) {
            final Notification notif = tn.getNotification();
            final Integer listenerID = tn.getListenerID();
            if (listenerID.equals(myListenerID)) return;
            final ClientListenerInfo li = listeners.get(listenerID);
            if (li == null) {
                logger.trace("NotifFetcher.dispatch",
                             "Listener ID not in map");
                return;
            }
            NotificationListener l = li.getListener();
            Object h = li.getHandback();
            try {
                l.handleNotification(notif, h);
            } catch (RuntimeException e) {
                final String msg =
                    "Failed to forward a notification " +
                    "to a listener";
                logger.trace("NotifFetcher-run", msg, e);
            }
        }
        private NotificationResult fetchNotifs() {
            try {
                NotificationResult nr = ClientNotifForwarder.this.
                    fetchNotifs(clientSequenceNumber,maxNotifications,
                                timeout);
                if (logger.traceOn()) {
                    logger.trace("NotifFetcher-run",
                                 "Got notifications from the server: "+nr);
                }
                return nr;
            } catch (ClassNotFoundException e) {
                logger.trace("NotifFetcher.fetchNotifs", e);
                return fetchOneNotif();
            } catch (NotSerializableException e) {
                logger.trace("NotifFetcher.fetchNotifs", e);
                return fetchOneNotif();
            } catch (IOException ioe) {
                if (!shouldStop()) {
                    logger.error("NotifFetcher-run",
                                 "Failed to fetch notification, " +
                                 "stopping thread. Error is: " + ioe, ioe);
                    logger.debug("NotifFetcher-run",ioe);
                }
                return null;
            }
        }
        private NotificationResult fetchOneNotif() {
            ClientNotifForwarder cnf = ClientNotifForwarder.this;
            long startSequenceNumber = clientSequenceNumber;
            int notFoundCount = 0;
            NotificationResult result = null;
            long firstEarliest = -1;
            while (result == null && !shouldStop()) {
                NotificationResult nr;
                try {
                    nr = cnf.fetchNotifs(startSequenceNumber, 0, 0L);
                } catch (ClassNotFoundException e) {
                    logger.warning("NotifFetcher.fetchOneNotif",
                                   "Impossible exception: " + e);
                    logger.debug("NotifFetcher.fetchOneNotif",e);
                    return null;
                } catch (IOException e) {
                    if (!shouldStop())
                        logger.trace("NotifFetcher.fetchOneNotif", e);
                    return null;
                }
                if (shouldStop())
                    return null;
                startSequenceNumber = nr.getNextSequenceNumber();
                if (firstEarliest < 0)
                    firstEarliest = nr.getEarliestSequenceNumber();
                try {
                    result = cnf.fetchNotifs(startSequenceNumber, 1, 0L);
                } catch (Exception e) {
                    if (e instanceof ClassNotFoundException
                        || e instanceof NotSerializableException) {
                        logger.warning("NotifFetcher.fetchOneNotif",
                                     "Failed to deserialize a notification: "+e.toString());
                        if (logger.traceOn()) {
                            logger.trace("NotifFetcher.fetchOneNotif",
                                         "Failed to deserialize a notification.", e);
                        }
                        notFoundCount++;
                        startSequenceNumber++;
                    } else {
                        if (!shouldStop())
                            logger.trace("NotifFetcher.fetchOneNotif", e);
                        return null;
                    }
                }
            }
            if (notFoundCount > 0) {
                final String msg =
                    "Dropped " + notFoundCount + " notification" +
                    (notFoundCount == 1 ? "" : "s") +
                    " because classes were missing locally";
                lostNotifs(msg, notFoundCount);
                if (result != null) {
                    result = new NotificationResult(
                            firstEarliest, result.getNextSequenceNumber(),
                            result.getTargetedNotifications());
                }
            }
            return result;
        }
        private boolean shouldStop() {
            synchronized (ClientNotifForwarder.this) {
                if (state != STARTED) {
                    return true;
                } else if (infoList.size() == 0) {
                    setState(STOPPING);
                    return true;
                }
                return false;
            }
        }
    }
    private synchronized void setState(int newState) {
        if (state == TERMINATED) {
            return;
        }
        state = newState;
        this.notifyAll();
    }
    private synchronized void init(boolean reconnected) throws IOException {
        switch (state) {
        case STARTED:
            return;
        case STARTING:
            return;
        case TERMINATED:
            throw new IOException("The ClientNotifForwarder has been terminated.");
        case STOPPING:
            if (beingReconnected == true) {
                return;
            }
            while (state == STOPPING) { 
                try {
                    wait();
                } catch (InterruptedException ire) {
                    IOException ioe = new IOException(ire.toString());
                    EnvHelp.initCause(ioe, ire);
                    throw ioe;
                }
            }
            init(reconnected);
            return;
        case STOPPED:
            if (beingReconnected == true) {
                return;
            }
            if (logger.traceOn()) {
                logger.trace("init", "Initializing...");
            }
            if (!reconnected) {
                try {
                    NotificationResult nr = fetchNotifs(-1, 0, 0);
                    clientSequenceNumber = nr.getNextSequenceNumber();
                } catch (ClassNotFoundException e) {
                    logger.warning("init", "Impossible exception: "+ e);
                    logger.debug("init",e);
                }
            }
            try {
                mbeanRemovedNotifID = addListenerForMBeanRemovedNotif();
            } catch (Exception e) {
                final String msg =
                    "Failed to register a listener to the mbean " +
                    "server: the client will not do clean when an MBean " +
                    "is unregistered";
                if (logger.traceOn()) {
                    logger.trace("init", msg, e);
                }
            }
            setState(STARTING);
            executor.execute(new NotifFetcher());
            return;
        default:
            throw new IOException("Unknown state.");
        }
    }
    private synchronized void beforeRemove() throws IOException {
        while (beingReconnected) {
            if (state == TERMINATED) {
                throw new IOException("Terminated.");
            }
            try {
                wait();
            } catch (InterruptedException ire) {
                IOException ioe = new IOException(ire.toString());
                EnvHelp.initCause(ioe, ire);
                throw ioe;
            }
        }
        if (state == TERMINATED) {
            throw new IOException("Terminated.");
        }
    }
    private final ClassLoader defaultClassLoader;
    private final Executor executor;
    private final Map<Integer, ClientListenerInfo> infoList =
            new HashMap<Integer, ClientListenerInfo>();
    private long clientSequenceNumber = -1;
    private final int maxNotifications;
    private final long timeout;
    private Integer mbeanRemovedNotifID = null;
    private Thread currentFetchThread;
    private static final int STARTING = 0;
    private static final int STARTED = 1;
    private static final int STOPPING = 2;
    private static final int STOPPED = 3;
    private static final int TERMINATED = 4;
    private int state = STOPPED;
    private boolean beingReconnected = false;
    private static final ClassLogger logger =
        new ClassLogger("javax.management.remote.misc",
                        "ClientNotifForwarder");
}
