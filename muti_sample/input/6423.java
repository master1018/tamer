public class ScanDirAgent {
    private static final Logger LOG =
            Logger.getLogger(ScanDirAgent.class.getName());
    private volatile ScanManagerMXBean proxy = null;
    private final BlockingQueue<Notification> queue;
    private final NotificationListener listener;
    public ScanDirAgent() {
        queue = new LinkedBlockingQueue<Notification>();
        listener = new NotificationListener() {
            public void handleNotification(Notification notification,
                                           Object handback) {
                try {
                    LOG.finer("Queuing received notification "+notification);
                    queue.put(notification);
                } catch (InterruptedException ex) {
                }
            }
        };
    }
    public void init() throws IOException, JMException {
        proxy = ScanManager.register();
        ((NotificationEmitter)proxy).addNotificationListener(listener,null,null);
    }
    public void cleanup() throws IOException, JMException {
        try {
            ((NotificationEmitter)proxy).
                    removeNotificationListener(listener,null,null);
        } finally {
            ManagementFactory.getPlatformMBeanServer().
                unregisterMBean(ScanManager.SCAN_MANAGER_NAME);
        }
    }
    public void waitForClose() throws IOException, JMException {
        while(proxy.getState() != ScanState.CLOSED ) {
            try {
                queue.poll(30,TimeUnit.SECONDS);
            } catch (InterruptedException ex) {
            }
        }
    }
    public static void main(String[] args)
        throws IOException, JMException {
        System.out.println("Initializing ScanManager...");
        final ScanDirAgent agent = new ScanDirAgent();
        agent.init();
        try {
            System.out.println("Waiting for ScanManager to close...");
            agent.waitForClose();
        } finally {
            System.out.println("Cleaning up...");
            agent.cleanup();
        }
    }
}
