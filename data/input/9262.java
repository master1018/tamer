public class NotificationBroadcasterSupport implements NotificationEmitter {
    public NotificationBroadcasterSupport() {
        this(null, (MBeanNotificationInfo[]) null);
    }
    public NotificationBroadcasterSupport(Executor executor) {
        this(executor, (MBeanNotificationInfo[]) null);
    }
    public NotificationBroadcasterSupport(MBeanNotificationInfo... info) {
        this(null, info);
    }
    public NotificationBroadcasterSupport(Executor executor,
                                          MBeanNotificationInfo... info) {
        this.executor = (executor != null) ? executor : defaultExecutor;
        notifInfo = info == null ? NO_NOTIFICATION_INFO : info.clone();
    }
    public void addNotificationListener(NotificationListener listener,
                                        NotificationFilter filter,
                                        Object handback) {
        if (listener == null) {
            throw new IllegalArgumentException ("Listener can't be null") ;
        }
        listenerList.add(new ListenerInfo(listener, filter, handback));
    }
    public void removeNotificationListener(NotificationListener listener)
            throws ListenerNotFoundException {
        ListenerInfo wildcard = new WildcardListenerInfo(listener);
        boolean removed =
            listenerList.removeAll(Collections.singleton(wildcard));
        if (!removed)
            throw new ListenerNotFoundException("Listener not registered");
    }
    public void removeNotificationListener(NotificationListener listener,
                                           NotificationFilter filter,
                                           Object handback)
            throws ListenerNotFoundException {
        ListenerInfo li = new ListenerInfo(listener, filter, handback);
        boolean removed = listenerList.remove(li);
        if (!removed) {
            throw new ListenerNotFoundException("Listener not registered " +
                                                "(with this filter and " +
                                                "handback)");
        }
    }
    public MBeanNotificationInfo[] getNotificationInfo() {
        if (notifInfo.length == 0)
            return notifInfo;
        else
            return notifInfo.clone();
    }
    public void sendNotification(Notification notification) {
        if (notification == null) {
            return;
        }
        boolean enabled;
        for (ListenerInfo li : listenerList) {
            try {
                enabled = li.filter == null ||
                    li.filter.isNotificationEnabled(notification);
            } catch (Exception e) {
                if (logger.debugOn()) {
                    logger.debug("sendNotification", e);
                }
                continue;
            }
            if (enabled) {
                executor.execute(new SendNotifJob(notification, li));
            }
        }
    }
    protected void handleNotification(NotificationListener listener,
                                      Notification notif, Object handback) {
        listener.handleNotification(notif, handback);
    }
    private static class ListenerInfo {
        NotificationListener listener;
        NotificationFilter filter;
        Object handback;
        ListenerInfo(NotificationListener listener,
                     NotificationFilter filter,
                     Object handback) {
            this.listener = listener;
            this.filter = filter;
            this.handback = handback;
        }
        public boolean equals(Object o) {
            if (!(o instanceof ListenerInfo))
                return false;
            ListenerInfo li = (ListenerInfo) o;
            if (li instanceof WildcardListenerInfo)
                return (li.listener == listener);
            else
                return (li.listener == listener && li.filter == filter
                        && li.handback == handback);
        }
    }
    private static class WildcardListenerInfo extends ListenerInfo {
        WildcardListenerInfo(NotificationListener listener) {
            super(listener, null, null);
        }
        public boolean equals(Object o) {
            assert (!(o instanceof WildcardListenerInfo));
            return o.equals(this);
        }
    }
    private List<ListenerInfo> listenerList =
        new CopyOnWriteArrayList<ListenerInfo>();
    private final Executor executor;
    private final MBeanNotificationInfo[] notifInfo;
    private final static Executor defaultExecutor = new Executor() {
            public void execute(Runnable r) {
                r.run();
            }
        };
    private static final MBeanNotificationInfo[] NO_NOTIFICATION_INFO =
        new MBeanNotificationInfo[0];
    private class SendNotifJob implements Runnable {
        public SendNotifJob(Notification notif, ListenerInfo listenerInfo) {
            this.notif = notif;
            this.listenerInfo = listenerInfo;
        }
        public void run() {
            try {
                handleNotification(listenerInfo.listener,
                                   notif, listenerInfo.handback);
            } catch (Exception e) {
                if (logger.debugOn()) {
                    logger.debug("SendNotifJob-run", e);
                }
            }
        }
        private final Notification notif;
        private final ListenerInfo listenerInfo;
    }
    private static final ClassLogger logger =
        new ClassLogger("javax.management", "NotificationBroadcasterSupport");
}
