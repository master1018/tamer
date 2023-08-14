public class StringMonitorDeadlockTest {
    public static void main(String[] args) throws Exception {
        if (args.length != 1)
            throw new Exception("Arg should be test number");
        int testNo = Integer.parseInt(args[0]) - 1;
        TestCase test = testCases[testNo];
        System.out.println("Test: " + test.getDescription());
        test.run();
        System.out.println("Test passed");
    }
    private static enum When {IN_GET_ATTRIBUTE, IN_NOTIFY};
    private static abstract class TestCase {
        TestCase(String description, When when) {
            this.description = description;
            this.when = when;
        }
        void run() throws Exception {
            final MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
            final ObjectName observedName = new ObjectName("a:b=c");
            final ObjectName monitorName = new ObjectName("a:type=Monitor");
            mbs.registerMBean(new StringMonitor(), monitorName);
            final StringMonitorMBean monitorProxy =
                JMX.newMBeanProxy(mbs, monitorName, StringMonitorMBean.class);
            final TestMBean observedProxy =
                JMX.newMBeanProxy(mbs, observedName, TestMBean.class);
            final Runnable sensitiveThing = new Runnable() {
                public void run() {
                    doSensitiveThing(monitorProxy, observedName);
                }
            };
            final Runnable nothing = new Runnable() {
                public void run() {}
            };
            final Runnable withinGetAttribute =
                (when == When.IN_GET_ATTRIBUTE) ? sensitiveThing : nothing;
            mbs.registerMBean(new Test(withinGetAttribute), observedName);
            monitorProxy.addObservedObject(observedName);
            monitorProxy.setObservedAttribute("Thing");
            monitorProxy.setStringToCompare("old");
            monitorProxy.setGranularityPeriod(10L); 
            monitorProxy.setNotifyDiffer(true);
            monitorProxy.start();
            final int initGetCount = observedProxy.getGetCount();
            int getCount = initGetCount;
            for (int i = 0; i < 500; i++) { 
                getCount = observedProxy.getGetCount();
                if (getCount != initGetCount)
                    break;
                Thread.sleep(10);
            }
            if (getCount <= initGetCount)
                throw new Exception("Test failed: presumable deadlock");
            if (when == When.IN_NOTIFY) {
                final AtomicInteger notifCount = new AtomicInteger();
                final NotificationListener listener = new NotificationListener() {
                    public void handleNotification(Notification n, Object h) {
                        Thread t = new Thread(sensitiveThing);
                        t.start();
                        try {
                            t.join();
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                        notifCount.incrementAndGet();
                    }
                };
                mbs.addNotificationListener(monitorName, listener, null, null);
                observedProxy.setThing("new");
                for (int i = 0; i < 500 && notifCount.get() == 0; i++)
                    Thread.sleep(10);
                if (notifCount.get() == 0)
                    throw new Exception("Test failed: presumable deadlock");
            }
        }
        abstract void doSensitiveThing(StringMonitorMBean monitorProxy,
                                       ObjectName observedName);
        String getDescription() {
            return description;
        }
        private final String description;
        private final When when;
    }
    private static final TestCase[] testCases = {
        new TestCase("Remove monitored MBean within monitored getAttribute",
                     When.IN_GET_ATTRIBUTE) {
            @Override
            void doSensitiveThing(StringMonitorMBean monitorProxy,
                                  ObjectName observedName) {
                monitorProxy.removeObservedObject(observedName);
            }
        },
        new TestCase("Stop monitor within monitored getAttribute",
                     When.IN_GET_ATTRIBUTE) {
            @Override
            void doSensitiveThing(StringMonitorMBean monitorProxy,
                                  ObjectName observedName) {
                monitorProxy.stop();
            }
        },
        new TestCase("Remove monitored MBean within threshold listener",
                     When.IN_NOTIFY) {
            @Override
            void doSensitiveThing(StringMonitorMBean monitorProxy,
                                  ObjectName observedName) {
                monitorProxy.removeObservedObject(observedName);
            }
        },
        new TestCase("Stop monitor within threshold listener",
                     When.IN_NOTIFY) {
            @Override
            void doSensitiveThing(StringMonitorMBean monitorProxy,
                                  ObjectName observedName) {
                monitorProxy.stop();
            }
        },
    };
    public static interface TestMBean {
        public String getThing();
        public void setThing(String thing);
        public int getGetCount();
    }
    public static class Test implements TestMBean {
        public Test(Runnable runWithinGetAttribute) {
            this.runWithinGetAttribute = runWithinGetAttribute;
        }
        public String getThing() {
            Thread t = new Thread(runWithinGetAttribute);
            t.start();
            try {
                t.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            getCount++;
            return thing;
        }
        public void setThing(String thing) {
            this.thing = thing;
        }
        public int getGetCount() {
            return getCount;
        }
        private final Runnable runWithinGetAttribute;
        private volatile int getCount;
        private volatile String thing;
    }
}
