public class BroadcasterSupportDeadlockTest {
    public static void main(String[] args) throws Exception {
        try {
            Class.forName(ManagementFactory.class.getName());
        } catch (Throwable t) {
            System.out.println("TEST CANNOT RUN: needs JDK 5 at least");
            return;
        }
        final MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        final BroadcasterMBean mbean = new Broadcaster();
        final ObjectName name = new ObjectName("test:type=Broadcaster");
        mbs.registerMBean(mbean, name);
        ThreadMXBean threads = ManagementFactory.getThreadMXBean();
        threads.setThreadContentionMonitoringEnabled(true);
        final Semaphore semaphore = new Semaphore(0);
        Thread t1 = new Thread() {
            public void run() {
                try {
                    mbs.invoke(name, "block",
                               new Object[] {semaphore},
                               new String[] {Semaphore.class.getName()});
                } catch (Exception e) {
                    e.printStackTrace(System.out);
                } finally {
                    System.out.println("TEST INCORRECT: block returned");
                    System.exit(1);
                }
            }
        };
        t1.setDaemon(true);
        t1.start();
        semaphore.acquire();
        Thread.sleep(100);
        while (t1.getState() != Thread.State.WAITING)
            Thread.sleep(1);
        final NotificationListener listener = new NotificationListener() {
            public void handleNotification(Notification n, Object h) {}
        };
        Thread t2 = new Thread() {
            public void run() {
                try {
                    mbs.addNotificationListener(name, listener, null, null);
                } catch (Exception e) {
                    System.out.println("TEST INCORRECT: addNL failed:");
                    e.printStackTrace(System.out);
                }
            }
        };
        t2.setDaemon(true);
        t2.start();
        Thread.sleep(100);
        for (int i = 0; i < 1000; i++) {
            t2.join(1);
            switch (t2.getState()) {
            case TERMINATED:
                System.out.println("TEST PASSED");
                return;
            case BLOCKED:
                java.util.Map<Thread,StackTraceElement[]> traces =
                    Thread.getAllStackTraces();
                showStackTrace("Thread 1", traces.get(t1));
                showStackTrace("Thread 2", traces.get(t2));
                System.out.println("TEST FAILED: deadlock");
                System.exit(1);
                break;
            default:
                break;
            }
        }
        System.out.println("TEST FAILED BUT DID NOT NOTICE DEADLOCK");
        Thread.sleep(10000);
        System.exit(1);
    }
    private static void showStackTrace(String title,
                                       StackTraceElement[] stack) {
        System.out.println("---" + title + "---");
        if (stack == null)
            System.out.println("<no stack trace???>");
        else {
            for (StackTraceElement elmt : stack)
                System.out.println("    " + elmt);
        }
        System.out.println();
    }
    public static interface BroadcasterMBean {
        public void block(Semaphore semaphore);
    }
    public static class Broadcaster
            extends NotificationBroadcasterSupport
            implements BroadcasterMBean {
        public synchronized void block(Semaphore semaphore) {
            Object lock = new Object();
            synchronized (lock) {
                try {
                    semaphore.release();
                    lock.wait(); 
                } catch (InterruptedException e) {
                    System.out.println("TEST INCORRECT: lock interrupted:");
                    e.printStackTrace(System.out);
                    System.exit(1);
                }
            }
        }
    }
}
