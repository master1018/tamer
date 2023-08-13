public class StandardEmitterMBean extends StandardMBean
        implements NotificationEmitter {
    private final NotificationEmitter emitter;
    private final MBeanNotificationInfo[] notificationInfo;
    public <T> StandardEmitterMBean(T implementation, Class<T> mbeanInterface,
                                    NotificationEmitter emitter) {
        super(implementation, mbeanInterface, false);
        if (emitter == null)
            throw new IllegalArgumentException("Null emitter");
        this.emitter = emitter;
        this.notificationInfo = emitter.getNotificationInfo();
    }
    public <T> StandardEmitterMBean(T implementation, Class<T> mbeanInterface,
                                    boolean isMXBean,
                                    NotificationEmitter emitter) {
        super(implementation, mbeanInterface, isMXBean);
        if (emitter == null)
            throw new IllegalArgumentException("Null emitter");
        this.emitter = emitter;
        this.notificationInfo = emitter.getNotificationInfo();
    }
    protected StandardEmitterMBean(Class<?> mbeanInterface,
                                   NotificationEmitter emitter) {
        super(mbeanInterface, false);
        if (emitter == null)
            throw new IllegalArgumentException("Null emitter");
        this.emitter = emitter;
        this.notificationInfo = emitter.getNotificationInfo();
    }
    protected StandardEmitterMBean(Class<?> mbeanInterface, boolean isMXBean,
                                   NotificationEmitter emitter) {
        super(mbeanInterface, isMXBean);
        if (emitter == null)
            throw new IllegalArgumentException("Null emitter");
        this.emitter = emitter;
        this.notificationInfo = emitter.getNotificationInfo();
    }
    public void removeNotificationListener(NotificationListener listener)
            throws ListenerNotFoundException {
        emitter.removeNotificationListener(listener);
    }
    public void removeNotificationListener(NotificationListener listener,
                                           NotificationFilter filter,
                                           Object handback)
            throws ListenerNotFoundException {
        emitter.removeNotificationListener(listener, filter, handback);
    }
    public void addNotificationListener(NotificationListener listener,
                                        NotificationFilter filter,
                                        Object handback) {
        emitter.addNotificationListener(listener, filter, handback);
    }
    public MBeanNotificationInfo[] getNotificationInfo() {
        return notificationInfo;
    }
    public void sendNotification(Notification n) {
        if (emitter instanceof NotificationBroadcasterSupport)
            ((NotificationBroadcasterSupport) emitter).sendNotification(n);
        else {
            final String msg =
                "Cannot sendNotification when emitter is not an " +
                "instance of NotificationBroadcasterSupport: " +
                emitter.getClass().getName();
            throw new ClassCastException(msg);
        }
    }
    @Override
    MBeanNotificationInfo[] getNotifications(MBeanInfo info) {
        return getNotificationInfo();
    }
}
