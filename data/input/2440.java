public class ArrayNotificationBuffer implements NotificationBuffer {
    private boolean disposed = false;
    private static final Object globalLock = new Object();
    private static final
        HashMap<MBeanServer,ArrayNotificationBuffer> mbsToBuffer =
        new HashMap<MBeanServer,ArrayNotificationBuffer>(1);
    private final Collection<ShareBuffer> sharers = new HashSet<ShareBuffer>(1);
    public static NotificationBuffer getNotificationBuffer(
            MBeanServer mbs, Map<String, ?> env) {
        if (env == null)
            env = Collections.emptyMap();
        int queueSize = EnvHelp.getNotifBufferSize(env);
        ArrayNotificationBuffer buf;
        boolean create;
        NotificationBuffer sharer;
        synchronized (globalLock) {
            buf = mbsToBuffer.get(mbs);
            create = (buf == null);
            if (create) {
                buf = new ArrayNotificationBuffer(mbs, queueSize);
                mbsToBuffer.put(mbs, buf);
            }
            sharer = buf.new ShareBuffer(queueSize);
        }
        if (create)
            buf.createListeners();
        return sharer;
    }
    static void removeNotificationBuffer(MBeanServer mbs) {
        synchronized (globalLock) {
            mbsToBuffer.remove(mbs);
        }
    }
    void addSharer(ShareBuffer sharer) {
        synchronized (globalLock) {
            synchronized (this) {
                if (sharer.getSize() > queueSize)
                    resize(sharer.getSize());
            }
            sharers.add(sharer);
        }
    }
    private void removeSharer(ShareBuffer sharer) {
        boolean empty;
        synchronized (globalLock) {
            sharers.remove(sharer);
            empty = sharers.isEmpty();
            if (empty)
                removeNotificationBuffer(mBeanServer);
            else {
                int max = 0;
                for (ShareBuffer buf : sharers) {
                    int bufsize = buf.getSize();
                    if (bufsize > max)
                        max = bufsize;
                }
                if (max < queueSize)
                    resize(max);
            }
        }
        if (empty) {
            synchronized (this) {
                disposed = true;
                notifyAll();
            }
            destroyListeners();
        }
    }
    private synchronized void resize(int newSize) {
        if (newSize == queueSize)
            return;
        while (queue.size() > newSize)
            dropNotification();
        queue.resize(newSize);
        queueSize = newSize;
    }
    private class ShareBuffer implements NotificationBuffer {
        ShareBuffer(int size) {
            this.size = size;
            addSharer(this);
        }
        public NotificationResult
            fetchNotifications(NotificationBufferFilter filter,
                               long startSequenceNumber,
                               long timeout,
                               int maxNotifications)
                throws InterruptedException {
            NotificationBuffer buf = ArrayNotificationBuffer.this;
            return buf.fetchNotifications(filter, startSequenceNumber,
                                          timeout, maxNotifications);
        }
        public void dispose() {
            ArrayNotificationBuffer.this.removeSharer(this);
        }
        int getSize() {
            return size;
        }
        private final int size;
    }
    private ArrayNotificationBuffer(MBeanServer mbs, int queueSize) {
        if (logger.traceOn())
            logger.trace("Constructor", "queueSize=" + queueSize);
        if (mbs == null || queueSize < 1)
            throw new IllegalArgumentException("Bad args");
        this.mBeanServer = mbs;
        this.queueSize = queueSize;
        this.queue = new ArrayQueue<NamedNotification>(queueSize);
        this.earliestSequenceNumber = System.currentTimeMillis();
        this.nextSequenceNumber = this.earliestSequenceNumber;
        logger.trace("Constructor", "ends");
    }
    private synchronized boolean isDisposed() {
        return disposed;
    }
    public void dispose() {
        throw new UnsupportedOperationException();
    }
    public NotificationResult
        fetchNotifications(NotificationBufferFilter filter,
                           long startSequenceNumber,
                           long timeout,
                           int maxNotifications)
            throws InterruptedException {
        logger.trace("fetchNotifications", "starts");
        if (startSequenceNumber < 0 || isDisposed()) {
            synchronized(this) {
                return new NotificationResult(earliestSequenceNumber(),
                                              nextSequenceNumber(),
                                              new TargetedNotification[0]);
            }
        }
        if (filter == null
            || startSequenceNumber < 0 || timeout < 0
            || maxNotifications < 0) {
            logger.trace("fetchNotifications", "Bad args");
            throw new IllegalArgumentException("Bad args to fetch");
        }
        if (logger.debugOn()) {
            logger.trace("fetchNotifications",
                  "filter=" + filter + "; startSeq=" +
                  startSequenceNumber + "; timeout=" + timeout +
                  "; max=" + maxNotifications);
        }
        if (startSequenceNumber > nextSequenceNumber()) {
            final String msg = "Start sequence number too big: " +
                startSequenceNumber + " > " + nextSequenceNumber();
            logger.trace("fetchNotifications", msg);
            throw new IllegalArgumentException(msg);
        }
        long endTime = System.currentTimeMillis() + timeout;
        if (endTime < 0) 
            endTime = Long.MAX_VALUE;
        if (logger.debugOn())
            logger.debug("fetchNotifications", "endTime=" + endTime);
        long earliestSeq = -1;
        long nextSeq = startSequenceNumber;
        List<TargetedNotification> notifs =
            new ArrayList<TargetedNotification>();
        while (true) {
            logger.debug("fetchNotifications", "main loop starts");
            NamedNotification candidate;
            synchronized (this) {
                if (earliestSeq < 0) {
                    earliestSeq = earliestSequenceNumber();
                    if (logger.debugOn()) {
                        logger.debug("fetchNotifications",
                              "earliestSeq=" + earliestSeq);
                    }
                    if (nextSeq < earliestSeq) {
                        nextSeq = earliestSeq;
                        logger.debug("fetchNotifications",
                                     "nextSeq=earliestSeq");
                    }
                } else
                    earliestSeq = earliestSequenceNumber();
                if (nextSeq < earliestSeq) {
                    logger.trace("fetchNotifications",
                          "nextSeq=" + nextSeq + " < " + "earliestSeq=" +
                          earliestSeq + " so may have lost notifs");
                    break;
                }
                if (nextSeq < nextSequenceNumber()) {
                    candidate = notificationAt(nextSeq);
                    if (logger.debugOn()) {
                        logger.debug("fetchNotifications", "candidate: " +
                                     candidate);
                        logger.debug("fetchNotifications", "nextSeq now " +
                                     nextSeq);
                    }
                } else {
                    if (notifs.size() > 0) {
                        logger.debug("fetchNotifications",
                              "no more notifs but have some so don't wait");
                        break;
                    }
                    long toWait = endTime - System.currentTimeMillis();
                    if (toWait <= 0) {
                        logger.debug("fetchNotifications", "timeout");
                        break;
                    }
                    if (isDisposed()) {
                        if (logger.debugOn())
                            logger.debug("fetchNotifications",
                                         "dispose callled, no wait");
                        return new NotificationResult(earliestSequenceNumber(),
                                                  nextSequenceNumber(),
                                                  new TargetedNotification[0]);
                    }
                    if (logger.debugOn())
                        logger.debug("fetchNotifications",
                                     "wait(" + toWait + ")");
                    wait(toWait);
                    continue;
                }
            }
            ObjectName name = candidate.getObjectName();
            Notification notif = candidate.getNotification();
            List<TargetedNotification> matchedNotifs =
                new ArrayList<TargetedNotification>();
            logger.debug("fetchNotifications",
                         "applying filter to candidate");
            filter.apply(matchedNotifs, name, notif);
            if (matchedNotifs.size() > 0) {
                if (maxNotifications <= 0) {
                    logger.debug("fetchNotifications",
                                 "reached maxNotifications");
                    break;
                }
                --maxNotifications;
                if (logger.debugOn())
                    logger.debug("fetchNotifications", "add: " +
                                 matchedNotifs);
                notifs.addAll(matchedNotifs);
            }
            ++nextSeq;
        } 
        int nnotifs = notifs.size();
        TargetedNotification[] resultNotifs =
            new TargetedNotification[nnotifs];
        notifs.toArray(resultNotifs);
        NotificationResult nr =
            new NotificationResult(earliestSeq, nextSeq, resultNotifs);
        if (logger.debugOn())
            logger.debug("fetchNotifications", nr.toString());
        logger.trace("fetchNotifications", "ends");
        return nr;
    }
    synchronized long earliestSequenceNumber() {
        return earliestSequenceNumber;
    }
    synchronized long nextSequenceNumber() {
        return nextSequenceNumber;
    }
    synchronized void addNotification(NamedNotification notif) {
        if (logger.traceOn())
            logger.trace("addNotification", notif.toString());
        while (queue.size() >= queueSize) {
            dropNotification();
            if (logger.debugOn()) {
                logger.debug("addNotification",
                      "dropped oldest notif, earliestSeq=" +
                      earliestSequenceNumber);
            }
        }
        queue.add(notif);
        nextSequenceNumber++;
        if (logger.debugOn())
            logger.debug("addNotification", "nextSeq=" + nextSequenceNumber);
        notifyAll();
    }
    private void dropNotification() {
        queue.remove(0);
        earliestSequenceNumber++;
    }
    synchronized NamedNotification notificationAt(long seqNo) {
        long index = seqNo - earliestSequenceNumber;
        if (index < 0 || index > Integer.MAX_VALUE) {
            final String msg = "Bad sequence number: " + seqNo + " (earliest "
                + earliestSequenceNumber + ")";
            logger.trace("notificationAt", msg);
            throw new IllegalArgumentException(msg);
        }
        return queue.get((int) index);
    }
    private static class NamedNotification {
        NamedNotification(ObjectName sender, Notification notif) {
            this.sender = sender;
            this.notification = notif;
        }
        ObjectName getObjectName() {
            return sender;
        }
        Notification getNotification() {
            return notification;
        }
        public String toString() {
            return "NamedNotification(" + sender + ", " + notification + ")";
        }
        private final ObjectName sender;
        private final Notification notification;
    }
    private void createListeners() {
        logger.debug("createListeners", "starts");
        synchronized (this) {
            createdDuringQuery = new HashSet<ObjectName>();
        }
        try {
            addNotificationListener(MBeanServerDelegate.DELEGATE_NAME,
                                    creationListener, creationFilter, null);
            logger.debug("createListeners", "added creationListener");
        } catch (Exception e) {
            final String msg = "Can't add listener to MBean server delegate: ";
            RuntimeException re = new IllegalArgumentException(msg + e);
            EnvHelp.initCause(re, e);
            logger.fine("createListeners", msg + e);
            logger.debug("createListeners", e);
            throw re;
        }
        Set<ObjectName> names = queryNames(null, broadcasterQuery);
        names = new HashSet<ObjectName>(names);
        synchronized (this) {
            names.addAll(createdDuringQuery);
            createdDuringQuery = null;
        }
        for (ObjectName name : names)
            addBufferListener(name);
        logger.debug("createListeners", "ends");
    }
    private void addBufferListener(ObjectName name) {
        checkNoLocks();
        if (logger.debugOn())
            logger.debug("addBufferListener", name.toString());
        try {
            addNotificationListener(name, bufferListener, null, name);
        } catch (Exception e) {
            logger.trace("addBufferListener", e);
        }
    }
    private void removeBufferListener(ObjectName name) {
        checkNoLocks();
        if (logger.debugOn())
            logger.debug("removeBufferListener", name.toString());
        try {
            removeNotificationListener(name, bufferListener);
        } catch (Exception e) {
            logger.trace("removeBufferListener", e);
        }
    }
    private void addNotificationListener(final ObjectName name,
                                         final NotificationListener listener,
                                         final NotificationFilter filter,
                                         final Object handback)
            throws Exception {
        try {
            AccessController.doPrivileged(new PrivilegedExceptionAction<Void>() {
                public Void run() throws InstanceNotFoundException {
                    mBeanServer.addNotificationListener(name,
                                                        listener,
                                                        filter,
                                                        handback);
                    return null;
                }
            });
        } catch (Exception e) {
            throw extractException(e);
        }
    }
    private void removeNotificationListener(final ObjectName name,
                                            final NotificationListener listener)
            throws Exception {
        try {
            AccessController.doPrivileged(new PrivilegedExceptionAction<Void>() {
                public Void run() throws Exception {
                    mBeanServer.removeNotificationListener(name, listener);
                    return null;
                }
            });
        } catch (Exception e) {
            throw extractException(e);
        }
    }
    private Set<ObjectName> queryNames(final ObjectName name,
                                       final QueryExp query) {
        PrivilegedAction<Set<ObjectName>> act =
            new PrivilegedAction<Set<ObjectName>>() {
                public Set<ObjectName> run() {
                    return mBeanServer.queryNames(name, query);
                }
            };
        try {
            return AccessController.doPrivileged(act);
        } catch (RuntimeException e) {
            logger.fine("queryNames", "Failed to query names: " + e);
            logger.debug("queryNames", e);
            throw e;
        }
    }
    private static boolean isInstanceOf(final MBeanServer mbs,
                                        final ObjectName name,
                                        final String className) {
        PrivilegedExceptionAction<Boolean> act =
            new PrivilegedExceptionAction<Boolean>() {
                public Boolean run() throws InstanceNotFoundException {
                    return mbs.isInstanceOf(name, className);
                }
            };
        try {
            return AccessController.doPrivileged(act);
        } catch (Exception e) {
            logger.fine("isInstanceOf", "failed: " + e);
            logger.debug("isInstanceOf", e);
            return false;
        }
    }
    private void createdNotification(MBeanServerNotification n) {
        final String shouldEqual =
            MBeanServerNotification.REGISTRATION_NOTIFICATION;
        if (!n.getType().equals(shouldEqual)) {
            logger.warning("createNotification", "bad type: " + n.getType());
            return;
        }
        ObjectName name = n.getMBeanName();
        if (logger.debugOn())
            logger.debug("createdNotification", "for: " + name);
        synchronized (this) {
            if (createdDuringQuery != null) {
                createdDuringQuery.add(name);
                return;
            }
        }
        if (isInstanceOf(mBeanServer, name, broadcasterClass)) {
            addBufferListener(name);
            if (isDisposed())
                removeBufferListener(name);
        }
    }
    private class BufferListener implements NotificationListener {
        public void handleNotification(Notification notif, Object handback) {
            if (logger.debugOn()) {
                logger.debug("BufferListener.handleNotification",
                      "notif=" + notif + "; handback=" + handback);
            }
            ObjectName name = (ObjectName) handback;
            addNotification(new NamedNotification(name, notif));
        }
    }
    private final NotificationListener bufferListener = new BufferListener();
    private static class BroadcasterQuery
            extends QueryEval implements QueryExp {
        private static final long serialVersionUID = 7378487660587592048L;
        public boolean apply(final ObjectName name) {
            final MBeanServer mbs = QueryEval.getMBeanServer();
            return isInstanceOf(mbs, name, broadcasterClass);
        }
    }
    private static final QueryExp broadcasterQuery = new BroadcasterQuery();
    private static final NotificationFilter creationFilter;
    static {
        NotificationFilterSupport nfs = new NotificationFilterSupport();
        nfs.enableType(MBeanServerNotification.REGISTRATION_NOTIFICATION);
        creationFilter = nfs;
    }
    private final NotificationListener creationListener =
        new NotificationListener() {
            public void handleNotification(Notification notif,
                                           Object handback) {
                logger.debug("creationListener", "handleNotification called");
                createdNotification((MBeanServerNotification) notif);
            }
        };
    private void destroyListeners() {
        checkNoLocks();
        logger.debug("destroyListeners", "starts");
        try {
            removeNotificationListener(MBeanServerDelegate.DELEGATE_NAME,
                                       creationListener);
        } catch (Exception e) {
            logger.warning("remove listener from MBeanServer delegate", e);
        }
        Set<ObjectName> names = queryNames(null, broadcasterQuery);
        for (final ObjectName name : names) {
            if (logger.debugOn())
                logger.debug("destroyListeners",
                             "remove listener from " + name);
            removeBufferListener(name);
        }
        logger.debug("destroyListeners", "ends");
    }
    private void checkNoLocks() {
        if (Thread.holdsLock(this) || Thread.holdsLock(globalLock))
            logger.warning("checkNoLocks", "lock protocol violation");
    }
    private static Exception extractException(Exception e) {
        while (e instanceof PrivilegedActionException) {
            e = ((PrivilegedActionException)e).getException();
        }
        return e;
    }
    private static final ClassLogger logger =
        new ClassLogger("javax.management.remote.misc",
                        "ArrayNotificationBuffer");
    private final MBeanServer mBeanServer;
    private final ArrayQueue<NamedNotification> queue;
    private int queueSize;
    private long earliestSequenceNumber;
    private long nextSequenceNumber;
    private Set<ObjectName> createdDuringQuery;
    static final String broadcasterClass =
        NotificationBroadcaster.class.getName();
}
