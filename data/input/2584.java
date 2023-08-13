public class NotifExecutorTest {
    public static void main(String[] args) throws Exception {
        System.out.println("Tests to use an executor to send notifications.");
        final MBeanServer mbs = MBeanServerFactory.createMBeanServer();
        final ObjectName mbean = new ObjectName ("Default:name=NotificationEmitter");
        final MyListener myLister = new MyListener();
        final NotificationListener nullListener = new NotificationListener() {
                public void handleNotification(Notification n, Object hb) {
                }
            };
        final Object[] params = new Object[] {new Integer(nb)};
        final String[] signatures = new String[] {"java.lang.Integer"};
        System.out.println(">>> Test with a null executor.");
        mbs.registerMBean(new NotificationEmitter(null), mbean);
        mbs.addNotificationListener(mbean, myLister, null, null);
        mbs.addNotificationListener(mbean, nullListener, null, null);
        mbs.invoke(mbean, "sendNotifications", params, signatures);
        check(nb, 0);
        mbs.unregisterMBean(mbean);
        System.out.println(">>> Test with a executor.");
        mbs.registerMBean(new NotificationEmitter(
                           new NotifExecutorTest.MyExecutor()), mbean);
        mbs.addNotificationListener(mbean, myLister, null, null);
        mbs.addNotificationListener(mbean, nullListener, null, null);
        mbs.invoke(mbean, "sendNotifications", params, signatures);
        check(nb, nb*2);
        System.out.println(">>> Test without listener.");
        mbs.removeNotificationListener(mbean, myLister);
        mbs.removeNotificationListener(mbean, nullListener);
        mbs.invoke(mbean, "sendNotifications", params, signatures);
        check(0, 0);
    }
    private static void check(int notifs, int called) throws Exception {
        synchronized (lock) {
            for (int i = 0; i < 10; i++) {
                if (receivedNotifs < notifs) {
                    lock.wait(1000);
                }
            }
        }
        Thread.sleep(1000);
        synchronized (lock) {
            if (receivedNotifs != notifs) {
                throw new RuntimeException("The listener expected to receive " +
                                           notifs + " notifs, but got " + receivedNotifs);
            } else {
                System.out.println(">>> The listener recieved as expected: "+receivedNotifs);
            }
            if (calledTimes != called) {
                throw new RuntimeException("The notif executor should be called " +
                                           called + " times, but got " + calledTimes);
            } else {
                System.out.println(">>> The executor was called as expected: "+calledTimes);
            }
        }
        receivedNotifs = 0;
        calledTimes = 0;
    }
    private static class MyListener implements NotificationListener {
        public void handleNotification(Notification notif, Object handback) {
            synchronized(lock) {
                if(++receivedNotifs >= nb) {
                    lock.notifyAll();
                }
            }
        }
    }
    public static class NotificationEmitter
        extends NotificationBroadcasterSupport
        implements NotificationEmitterMBean {
        public NotificationEmitter(Executor executor) {
            super(executor);
        }
        public void sendNotifications(Integer nb) {
            System.out.println(">>> NotificationEmitter: asked to send " +
                               "notifications: " + nb);
            Notification notif;
            for (int i = 1; i <= nb.intValue(); i++) {
                notif = new Notification(null, this, ++seqno);
                super.sendNotification(notif);
            }
        }
    }
    public interface NotificationEmitterMBean {
        public void sendNotifications(Integer nb);
    }
    public static class MyExecutor extends ThreadPoolExecutor {
        public MyExecutor() {
            super(1, 1, 1L, TimeUnit.MILLISECONDS,
                  new ArrayBlockingQueue(nb*5));
        }
        public synchronized void execute(Runnable job) {
            synchronized(lock) {
                calledTimes++;
            }
            super.execute(job);
        }
    }
    private static int nb = 10;
    private static int receivedNotifs = 0;
    private static int[] lock = new int[0];
    private static volatile long seqno;
    private static int calledTimes = 0;
}
