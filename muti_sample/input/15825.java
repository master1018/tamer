public class ScanManagerTest extends TestCase {
    public ScanManagerTest(String testName) {
        super(testName);
    }
    protected void setUp() throws Exception {
    }
    protected void tearDown() throws Exception {
    }
    public static Test suite() {
        TestSuite suite = new TestSuite(ScanManagerTest.class);
        return suite;
    }
    public void testMakeSingletonName() {
        System.out.println("makeSingletonName");
        Class clazz = ScanManagerMXBean.class;
        ObjectName expResult = ScanManager.SCAN_MANAGER_NAME;
        ObjectName result = ScanManager.makeSingletonName(clazz);
        assertEquals(expResult, result);
    }
    public void testRegister() throws Exception {
        System.out.println("register");
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        ScanManagerMXBean result = ScanManager.register(mbs);
        try {
            assertEquals(STOPPED,result.getState());
        } finally {
            try {
                mbs.unregisterMBean(ScanManager.SCAN_MANAGER_NAME);
            } catch (Exception x) {
                System.err.println("Failed to cleanup: "+x);
            }
        }
    }
    public interface Call {
        public void call() throws Exception;
        public void cancel() throws Exception;
    }
    public void testAddNotificationListener() throws Exception {
        System.out.println("addNotificationListener");
        final ScanManagerMXBean manager = ScanManager.register();
        final Call op = new Call() {
            public void call() throws Exception {
                manager.schedule(100000,0);
            }
            public void cancel() throws Exception {
                manager.stop();
            }
        };
        try {
            doTestOperation(manager,op,
                            EnumSet.of(RUNNING,SCHEDULED),
                            "schedule");
        } finally {
            try {
                ManagementFactory.getPlatformMBeanServer().
                        unregisterMBean(ScanManager.SCAN_MANAGER_NAME);
            } catch (Exception x) {
                System.err.println("Failed to cleanup: "+x);
            }
        }
    }
    private void doTestOperation(
            ScanManagerMXBean proxy,
            Call op,
            EnumSet<ScanState> after,
            String testName)
        throws Exception {
        System.out.println("doTestOperation: "+testName);
        final LinkedBlockingQueue<Notification> queue =
                new LinkedBlockingQueue<Notification>();
        NotificationListener listener = new NotificationListener() {
            public void handleNotification(Notification notification,
                        Object handback) {
                try {
                    queue.put(notification);
                } catch (Exception x) {
                    System.err.println("Failed to queue notif: "+x);
                }
            }
        };
        NotificationFilter filter = null;
        Object handback = null;
        final ScanState before;
        final NotificationEmitter emitter = (NotificationEmitter)proxy;
        emitter.addNotificationListener(listener, filter, handback);
        before = proxy.getState();
        op.call();
        try {
            final Notification notification =
                    queue.poll(3000,TimeUnit.MILLISECONDS);
            assertEquals(AttributeChangeNotification.ATTRIBUTE_CHANGE,
                    notification.getType());
            assertEquals(AttributeChangeNotification.class,
                    notification.getClass());
            assertEquals(ScanManager.SCAN_MANAGER_NAME,
                    notification.getSource());
            AttributeChangeNotification acn =
                    (AttributeChangeNotification)notification;
            assertEquals("State",acn.getAttributeName());
            assertEquals(ScanState.class.getName(),acn.getAttributeType());
            assertEquals(before,ScanState.valueOf((String)acn.getOldValue()));
            assertContained(after,ScanState.valueOf((String)acn.getNewValue()));
            emitter.removeNotificationListener(listener,filter,handback);
        } finally {
            try {
                op.cancel();
            } catch (Exception x) {
                System.err.println("Failed to cleanup: "+x);
            }
        }
    }
    public void testPreRegister() throws Exception {
        System.out.println("preRegister");
        MBeanServer server = ManagementFactory.getPlatformMBeanServer();
        ObjectName name = new ObjectName("DownUnder:type=Wombat");
        ScanManager instance = new ScanManager();
        ObjectName expResult = ScanManager.SCAN_MANAGER_NAME;
        ObjectName result;
        try {
            result = instance.preRegister(server, name);
            throw new RuntimeException("bad name accepted!");
        } catch (IllegalArgumentException x) {
            result = instance.preRegister(server, null);
        }
        assertEquals(expResult, result);
        result = instance.preRegister(server, ScanManager.SCAN_MANAGER_NAME);
        assertEquals(expResult, result);
    }
    public void testGetState() throws IOException, InstanceNotFoundException {
        System.out.println("getState");
        ScanManager instance = new ScanManager();
        ScanState expResult = ScanState.STOPPED;
        ScanState result = instance.getState();
        assertEquals(expResult, result);
        instance.start();
        final ScanState afterStart = instance.getState();
        assertContained(EnumSet.of(RUNNING,SCHEDULED,COMPLETED),afterStart);
        instance.stop();
        assertEquals(STOPPED,instance.getState());
        instance.schedule(1000000L,1000000L);
        assertEquals(SCHEDULED,instance.getState());
        instance.stop();
        assertEquals(STOPPED,instance.getState());
    }
    public void testSchedule() throws Exception {
        System.out.println("schedule");
        final long delay = 10000L;
        final long interval = 10000L;
        final ScanManagerMXBean manager = ScanManager.register();
        final Call op = new Call() {
            public void call() throws Exception {
                manager.schedule(delay,interval);
                assertEquals(SCHEDULED,manager.getState());
            }
            public void cancel() throws Exception {
                manager.stop();
            }
        };
        try {
            doTestOperation(manager,op,EnumSet.of(SCHEDULED),
                    "schedule");
        } finally {
            try {
                ManagementFactory.getPlatformMBeanServer().
                        unregisterMBean(ScanManager.SCAN_MANAGER_NAME);
            } catch (Exception x) {
                System.err.println("Failed to cleanup: "+x);
            }
        }
    }
    public static void assertContained(EnumSet<ScanState> allowed,
            ScanState state) {
         final String msg = String.valueOf(state) + " is not one of " + allowed;
         assertTrue(msg,allowed.contains(state));
    }
    public void testStop() throws Exception {
        System.out.println("stop");
        final ScanManagerMXBean manager = ScanManager.register();
        try {
            manager.schedule(1000000,0);
            assertContained(EnumSet.of(SCHEDULED),manager.getState());
            final Call op = new Call() {
                public void call() throws Exception {
                    manager.stop();
                    assertEquals(STOPPED,manager.getState());
                }
                public void cancel() throws Exception {
                    if (manager.getState() != STOPPED)
                        manager.stop();
                }
            };
            doTestOperation(manager,op,EnumSet.of(STOPPED),"stop");
        } finally {
            try {
                ManagementFactory.getPlatformMBeanServer().
                        unregisterMBean(ScanManager.SCAN_MANAGER_NAME);
            } catch (Exception x) {
                System.err.println("Failed to cleanup: "+x);
            }
        }
    }
    public void testStart() throws Exception {
        final ScanManagerMXBean manager = ScanManager.register();
        try {
            final Call op = new Call() {
                public void call() throws Exception {
                    assertEquals(STOPPED,manager.getState());
                    manager.start();
                    assertContained(EnumSet.of(RUNNING,SCHEDULED,COMPLETED),
                            manager.getState());
                }
                public void cancel() throws Exception {
                    manager.stop();
                }
            };
            doTestOperation(manager,op,EnumSet.of(RUNNING,SCHEDULED,COMPLETED),
                    "start");
        } finally {
            try {
                ManagementFactory.getPlatformMBeanServer().
                        unregisterMBean(ScanManager.SCAN_MANAGER_NAME);
            } catch (Exception x) {
                System.err.println("Failed to cleanup: "+x);
            }
        }
    }
}
