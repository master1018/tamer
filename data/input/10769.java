public class ServerNotifForwarder {
    public ServerNotifForwarder(MBeanServer mbeanServer,
                                Map<String, ?> env,
                                NotificationBuffer notifBuffer,
                                String connectionId) {
        this.mbeanServer = mbeanServer;
        this.notifBuffer = notifBuffer;
        this.connectionId = connectionId;
        connectionTimeout = EnvHelp.getServerConnectionTimeout(env);
        checkNotificationEmission = EnvHelp.computeBooleanFromString(
            env,
            "jmx.remote.x.check.notification.emission",false);
        notificationAccessController =
                EnvHelp.getNotificationAccessController(env);
    }
    public Integer addNotificationListener(final ObjectName name,
        final NotificationFilter filter)
        throws InstanceNotFoundException, IOException {
        if (logger.traceOn()) {
            logger.trace("addNotificationListener",
                "Add a listener at " + name);
        }
        checkState();
        checkMBeanPermission(name, "addNotificationListener");
        if (notificationAccessController != null) {
            notificationAccessController.addNotificationListener(
                connectionId, name, getSubject());
        }
        try {
            boolean instanceOf =
            AccessController.doPrivileged(
                    new PrivilegedExceptionAction<Boolean>() {
                        public Boolean run() throws InstanceNotFoundException {
                            return mbeanServer.isInstanceOf(name, broadcasterClass);
                        }
            });
            if (!instanceOf) {
                throw new IllegalArgumentException("The specified MBean [" +
                    name + "] is not a " +
                    "NotificationBroadcaster " +
                    "object.");
            }
        } catch (PrivilegedActionException e) {
            throw (InstanceNotFoundException) extractException(e);
        }
        final Integer id = getListenerID();
        ObjectName nn = name;
        if (name.getDomain() == null || name.getDomain().equals("")) {
            try {
                nn = ObjectName.getInstance(mbeanServer.getDefaultDomain(),
                                            name.getKeyPropertyList());
            } catch (MalformedObjectNameException mfoe) {
                IOException ioe = new IOException(mfoe.getMessage());
                ioe.initCause(mfoe);
                throw ioe;
            }
        }
        synchronized (listenerMap) {
            IdAndFilter idaf = new IdAndFilter(id, filter);
            Set<IdAndFilter> set = listenerMap.get(nn);
            if (set == null)
                set = Collections.singleton(idaf);
            else {
                if (set.size() == 1)
                    set = new HashSet<IdAndFilter>(set);
                set.add(idaf);
            }
            listenerMap.put(nn, set);
        }
        return id;
    }
    public void removeNotificationListener(ObjectName name,
        Integer[] listenerIDs)
        throws Exception {
        if (logger.traceOn()) {
            logger.trace("removeNotificationListener",
                "Remove some listeners from " + name);
        }
        checkState();
        checkMBeanPermission(name, "removeNotificationListener");
        if (notificationAccessController != null) {
            notificationAccessController.removeNotificationListener(
                connectionId, name, getSubject());
        }
        Exception re = null;
        for (int i = 0 ; i < listenerIDs.length ; i++) {
            try {
                removeNotificationListener(name, listenerIDs[i]);
            } catch (Exception e) {
                if (re != null) {
                    re = e;
                }
            }
        }
        if (re != null) {
            throw re;
        }
    }
    public void removeNotificationListener(ObjectName name, Integer listenerID)
    throws
        InstanceNotFoundException,
        ListenerNotFoundException,
        IOException {
        if (logger.traceOn()) {
            logger.trace("removeNotificationListener",
                "Remove the listener " + listenerID + " from " + name);
        }
        checkState();
        if (name != null && !name.isPattern()) {
            if (!mbeanServer.isRegistered(name)) {
                throw new InstanceNotFoundException("The MBean " + name +
                    " is not registered.");
            }
        }
        synchronized (listenerMap) {
            Set<IdAndFilter> set = listenerMap.get(name);
            IdAndFilter idaf = new IdAndFilter(listenerID, null);
            if (set == null || !set.contains(idaf))
                throw new ListenerNotFoundException("Listener not found");
            if (set.size() == 1)
                listenerMap.remove(name);
            else
                set.remove(idaf);
        }
    }
    private final NotificationBufferFilter bufferFilter =
            new NotificationBufferFilter() {
        public void apply(List<TargetedNotification> targetedNotifs,
                          ObjectName source, Notification notif) {
            final IdAndFilter[] candidates;
            synchronized (listenerMap) {
                final Set<IdAndFilter> set = listenerMap.get(source);
                if (set == null) {
                    logger.debug("bufferFilter", "no listeners for this name");
                    return;
                }
                candidates = new IdAndFilter[set.size()];
                set.toArray(candidates);
            }
            for (IdAndFilter idaf : candidates) {
                final NotificationFilter nf = idaf.getFilter();
                if (nf == null || nf.isNotificationEnabled(notif)) {
                    logger.debug("bufferFilter", "filter matches");
                    final TargetedNotification tn =
                            new TargetedNotification(notif, idaf.getId());
                    if (allowNotificationEmission(source, tn))
                        targetedNotifs.add(tn);
                }
            }
        }
    };
    public NotificationResult fetchNotifs(long startSequenceNumber,
        long timeout,
        int maxNotifications) {
        if (logger.traceOn()) {
            logger.trace("fetchNotifs", "Fetching notifications, the " +
                "startSequenceNumber is " + startSequenceNumber +
                ", the timeout is " + timeout +
                ", the maxNotifications is " + maxNotifications);
        }
        NotificationResult nr;
        final long t = Math.min(connectionTimeout, timeout);
        try {
            nr = notifBuffer.fetchNotifications(bufferFilter,
                startSequenceNumber,
                t, maxNotifications);
            snoopOnUnregister(nr);
        } catch (InterruptedException ire) {
            nr = new NotificationResult(0L, 0L, new TargetedNotification[0]);
        }
        if (logger.traceOn()) {
            logger.trace("fetchNotifs", "Forwarding the notifs: "+nr);
        }
        return nr;
    }
    private void snoopOnUnregister(NotificationResult nr) {
        Set<IdAndFilter> delegateSet = listenerMap.get(MBeanServerDelegate.DELEGATE_NAME);
        if (delegateSet == null || delegateSet.isEmpty()) {
            return;
        }
        for (TargetedNotification tn : nr.getTargetedNotifications()) {
            Integer id = tn.getListenerID();
            for (IdAndFilter idaf : delegateSet) {
                if (idaf.id == id) {
                    Notification n = tn.getNotification();
                    if (n instanceof MBeanServerNotification &&
                            n.getType().equals(MBeanServerNotification.UNREGISTRATION_NOTIFICATION)) {
                        MBeanServerNotification mbsn = (MBeanServerNotification) n;
                        ObjectName gone = mbsn.getMBeanName();
                        synchronized (listenerMap) {
                            listenerMap.remove(gone);
                        }
                    }
                }
            }
        }
    }
    public void terminate() {
        if (logger.traceOn()) {
            logger.trace("terminate", "Be called.");
        }
        synchronized(terminationLock) {
            if (terminated) {
                return;
            }
            terminated = true;
            synchronized(listenerMap) {
                listenerMap.clear();
            }
        }
        if (logger.traceOn()) {
            logger.trace("terminate", "Terminated.");
        }
    }
    private Subject getSubject() {
        return Subject.getSubject(AccessController.getContext());
    }
    private void checkState() throws IOException {
        synchronized(terminationLock) {
            if (terminated) {
                throw new IOException("The connection has been terminated.");
            }
        }
    }
    private Integer getListenerID() {
        synchronized(listenerCounterLock) {
            return listenerCounter++;
        }
    }
    public void checkMBeanPermission(
            final ObjectName name, final String actions)
            throws InstanceNotFoundException, SecurityException {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            AccessControlContext acc = AccessController.getContext();
            ObjectInstance oi;
            try {
                oi = AccessController.doPrivileged(
                    new PrivilegedExceptionAction<ObjectInstance>() {
                        public ObjectInstance run()
                        throws InstanceNotFoundException {
                            return mbeanServer.getObjectInstance(name);
                        }
                });
            } catch (PrivilegedActionException e) {
                throw (InstanceNotFoundException) extractException(e);
            }
            String classname = oi.getClassName();
            MBeanPermission perm = new MBeanPermission(
                classname,
                null,
                name,
                actions);
            sm.checkPermission(perm, acc);
        }
    }
    private boolean allowNotificationEmission(ObjectName name,
                                              TargetedNotification tn) {
        try {
            if (checkNotificationEmission) {
                checkMBeanPermission(name, "addNotificationListener");
            }
            if (notificationAccessController != null) {
                notificationAccessController.fetchNotification(
                        connectionId, name, tn.getNotification(), getSubject());
            }
            return true;
        } catch (SecurityException e) {
            if (logger.debugOn()) {
                logger.debug("fetchNotifs", "Notification " +
                        tn.getNotification() + " not forwarded: the " +
                        "caller didn't have the required access rights");
            }
            return false;
        } catch (Exception e) {
            if (logger.debugOn()) {
                logger.debug("fetchNotifs", "Notification " +
                        tn.getNotification() + " not forwarded: " +
                        "got an unexpected exception: " + e);
            }
            return false;
        }
    }
    private static Exception extractException(Exception e) {
        while (e instanceof PrivilegedActionException) {
            e = ((PrivilegedActionException)e).getException();
        }
        return e;
    }
    private static class IdAndFilter {
        private Integer id;
        private NotificationFilter filter;
        IdAndFilter(Integer id, NotificationFilter filter) {
            this.id = id;
            this.filter = filter;
        }
        Integer getId() {
            return this.id;
        }
        NotificationFilter getFilter() {
            return this.filter;
        }
        @Override
        public int hashCode() {
            return id.hashCode();
        }
        @Override
        public boolean equals(Object o) {
            return ((o instanceof IdAndFilter) &&
                    ((IdAndFilter) o).getId().equals(getId()));
        }
    }
    private MBeanServer mbeanServer;
    private final String connectionId;
    private final long connectionTimeout;
    private static int listenerCounter = 0;
    private final static int[] listenerCounterLock = new int[0];
    private NotificationBuffer notifBuffer;
    private final Map<ObjectName, Set<IdAndFilter>> listenerMap =
            new HashMap<ObjectName, Set<IdAndFilter>>();
    private boolean terminated = false;
    private final int[] terminationLock = new int[0];
    static final String broadcasterClass =
        NotificationBroadcaster.class.getName();
    private final boolean checkNotificationEmission;
    private final NotificationAccessController notificationAccessController;
    private static final ClassLogger logger =
        new ClassLogger("javax.management.remote.misc", "ServerNotifForwarder");
}
