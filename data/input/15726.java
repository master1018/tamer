public class NotificationBufferDeadlockTest {
    public static void main(String[] args) throws Exception {
        System.out.println("Check no deadlock if notif sent while initial " +
                           "remote listeners being added");
        final String[] protos = {"rmi", "iiop", "jmxmp"};
        for (String p : protos) {
            try {
                test(p);
            } catch (Exception e) {
                System.out.println("TEST FAILED: GOT EXCEPTION:");
                e.printStackTrace(System.out);
                failure = e.toString();
            }
        }
        if (failure == null)
            return;
        else
            throw new Exception("TEST FAILED: " + failure);
    }
    private static void test(String proto) throws Exception {
        System.out.println("Testing protocol " + proto);
        MBeanServer mbs = MBeanServerFactory.newMBeanServer();
        ObjectName testName = newName();
        DeadlockTest test = new DeadlockTest();
        mbs.registerMBean(test, testName);
        JMXServiceURL url = new JMXServiceURL("service:jmx:" + proto + ":
        JMXConnectorServer cs;
        try {
            cs =
                JMXConnectorServerFactory.newJMXConnectorServer(url, null, mbs);
        } catch (MalformedURLException e) {
            System.out.println("...protocol not supported, ignoring");
            return;
        }
        MBeanServerForwarder createDuringQueryForwarder = (MBeanServerForwarder)
            Proxy.newProxyInstance(new Object() {}.getClass().getClassLoader(),
                                   new Class[] {MBeanServerForwarder.class},
                                   new CreateDuringQueryInvocationHandler());
        cs.setMBeanServerForwarder(createDuringQueryForwarder);
        cs.start();
        JMXServiceURL addr = cs.getAddress();
        JMXConnector cc = JMXConnectorFactory.connect(addr);
        MBeanServerConnection mbsc = cc.getMBeanServerConnection();
        try {
            String fail = test(mbsc, testName);
            if (fail != null)
                System.out.println("FAILED: " + fail);
            failure = fail;
        } finally {
            cc.close();
            cs.stop();
        }
    }
    private static String test(MBeanServerConnection mbsc,
                               ObjectName testName) throws Exception {
        NotificationListener dummyListener = new NotificationListener() {
            public void handleNotification(Notification n, Object h) {
            }
        };
        thisFailure = null;
        mbsc.addNotificationListener(testName, dummyListener, null, null);
        if (thisFailure != null)
            return thisFailure;
        ObjectName newName = newName();
        mbsc.createMBean(DeadlockTest.class.getName(), newName);
        if (thisFailure != null)
            return thisFailure;
        Set<ObjectName> names =
            mbsc.queryNames(new ObjectName("d:type=DeadlockTest,*"), null);
        System.out.printf("...found %d test MBeans\n", names.size());
        sources.clear();
        countListener = new MyListener(names.size());
        for (ObjectName name : names)
            mbsc.addNotificationListener(name, countListener, null, null);
        if (thisFailure != null)
            return thisFailure;
        for (ObjectName name : names)
            mbsc.invoke(name, "send", null, null);
        if (!countListener.waiting(MAX_WAITING_TIME)) {
            return "did not get " + names.size() + " notifs as expected\n";
        }
        if (!sources.containsAll(names))
            return "missing names: " + sources;
        return thisFailure;
    }
    public static interface DeadlockTestMBean {
        public void send();
    }
    public static class DeadlockTest extends NotificationBroadcasterSupport
            implements DeadlockTestMBean {
        @Override
        public void addNotificationListener(NotificationListener listener,
                                            NotificationFilter filter,
                                            Object handback) {
            super.addNotificationListener(listener, filter, handback);
            Thread t = new Thread() {
                @Override
                public void run() {
                    Notification n =
                        new Notification("type", DeadlockTest.this, 0L);
                    DeadlockTest.this.sendNotification(n);
                }
            };
            t.start();
            try {
                t.join(5000L);
            } catch (Exception e) {
                thisFailure = "Join exception: " + e;
            }
            if (t.isAlive())
                thisFailure = "Deadlock detected";
        }
        public void send() {
            sendNotification(new Notification(TESTING_TYPE, DeadlockTest.this, 1L));
        }
    }
    private static class CreateDuringQueryInvocationHandler
            implements InvocationHandler {
        public Object invoke(Object proxy, Method m, Object[] args)
                throws Throwable {
            if (m.getName().equals("setMBeanServer")) {
                mbs = (MBeanServer) args[0];
                return null;
            }
            createMBeanIfQuery(m);
            Object ret = m.invoke(mbs, args);
            createMBeanIfQuery(m);
            return ret;
        }
        private void createMBeanIfQuery(Method m) throws InterruptedException {
            if (m.getName().equals("queryNames")) {
                Thread t = new Thread() {
                    public void run() {
                        try {
                            mbs.createMBean(DeadlockTest.class.getName(),
                                            newName());
                        } catch (Exception e) {
                            e.printStackTrace();
                            thisFailure = e.toString();
                        }
                    }
                };
                t.start();
                t.join(5000);
                if (t.isAlive())
                    failure = "Query deadlock detected";
            }
        }
        private MBeanServer mbs;
    }
    private static synchronized ObjectName newName() {
        try {
            return new ObjectName("d:type=DeadlockTest,instance=" +
                                  ++nextNameIndex);
        } catch (MalformedObjectNameException e) {
            throw new IllegalArgumentException("bad ObjectName", e);
        }
    }
    private static class MyListener implements NotificationListener {
        public MyListener(int waitNB) {
            this.waitNB= waitNB;
        }
        public void handleNotification(Notification n, Object h) {
            System.out.println("MyListener got: "+n.getSource()+" "+n.getType());
            synchronized(this) {
                if (TESTING_TYPE.equals(n.getType())) {
                    sources.add((ObjectName) n.getSource());
                    if (sources.size() == waitNB) {
                        this.notifyAll();
                    }
                }
            }
        }
        public boolean waiting(long timeout) {
            final long startTime = System.currentTimeMillis();
            long toWait = timeout;
            synchronized(this) {
                while(sources.size() < waitNB && toWait > 0) {
                    try {
                        this.wait(toWait);
                    } catch (InterruptedException ire) {
                        break;
                    }
                    toWait = timeout -
                        (System.currentTimeMillis() - startTime);
                }
            }
            return sources.size() == waitNB;
        }
        private final int waitNB;
    }
    static String thisFailure;
    static String failure;
    static int nextNameIndex;
    static final long MAX_WAITING_TIME = 10000;
    private static MyListener countListener;
    private static final List<ObjectName> sources = new Vector();
    private static final String TESTING_TYPE = "testing_type";
}
