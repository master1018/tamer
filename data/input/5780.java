public class ExecutorTest {
    private static final String EXECUTOR_PROPERTY =
        "jmx.remote.x.fetch.notifications.executor";
    private static final String NOTIF_TYPE = "test.type";
    public static void main(String[] args) throws Exception {
        String lastfail = null;
        for (String proto : new String[] {"rmi", "iiop", "jmxmp"}) {
            JMXServiceURL url = new JMXServiceURL(proto, null, 0);
            JMXConnectorServer cs;
            MBeanServer mbs = MBeanServerFactory.newMBeanServer();
            try {
                cs = JMXConnectorServerFactory.newJMXConnectorServer(url,
                                                                     null,
                                                                     mbs);
            } catch (MalformedURLException e) {
                System.out.println();
                System.out.println("Ignoring protocol: " + proto);
                continue;
            }
            String fail;
            try {
                fail = test(proto);
                if (fail != null)
                    System.out.println("TEST FAILED: " + fail);
            } catch (Exception e) {
                e.printStackTrace(System.out);
                fail = e.toString();
            }
            if (lastfail == null)
                lastfail = fail;
        }
        if (lastfail == null)
            return;
        System.out.println();
        System.out.println("TEST FAILED");
        throw new Exception("Test failed: " + lastfail);
    }
    private static enum TestType {NO_EXECUTOR, NULL_EXECUTOR, COUNT_EXECUTOR};
    private static String test(String proto) throws Exception {
        System.out.println();
        System.out.println("TEST: " + proto);
        ClassLoader myClassLoader =
            CountInvocationHandler.class.getClassLoader();
        ExecutorService wrappedExecutor = Executors.newCachedThreadPool();
        CountInvocationHandler executorHandler =
            new CountInvocationHandler(wrappedExecutor);
        Executor countExecutor = (Executor)
            Proxy.newProxyInstance(myClassLoader,
                                   new Class<?>[] {Executor.class},
                                   executorHandler);
        JMXServiceURL url = new JMXServiceURL(proto, null, 0);
        for (TestType test : TestType.values()) {
            Map<String, Executor> env = new HashMap<String, Executor>();
            switch (test) {
            case NO_EXECUTOR:
                System.out.println("Test with no executor in env");
                break;
            case NULL_EXECUTOR:
                System.out.println("Test with null executor in env");
                env.put(EXECUTOR_PROPERTY, null);
                break;
            case COUNT_EXECUTOR:
                System.out.println("Test with non-null executor in env");
                env.put(EXECUTOR_PROPERTY, countExecutor);
                break;
            }
            MBeanServer mbs = MBeanServerFactory.newMBeanServer();
            ObjectName emitName = new ObjectName("blah:type=Emitter");
            mbs.registerMBean(new Emitter(), emitName);
            JMXConnectorServer cs =
                JMXConnectorServerFactory.newJMXConnectorServer(url, null, mbs);
            cs.start();
            JMXServiceURL addr = cs.getAddress();
            JMXConnector cc = JMXConnectorFactory.connect(addr, env);
            MBeanServerConnection mbsc = cc.getMBeanServerConnection();
            EmitterMBean emitter = (EmitterMBean)
                MBeanServerInvocationHandler.newProxyInstance(mbsc,
                                                              emitName,
                                                              EmitterMBean.class,
                                                              false);
            SemaphoreListener listener = new SemaphoreListener();
            NotificationFilterSupport filter = new NotificationFilterSupport();
            filter.enableType(NOTIF_TYPE);
            mbsc.addNotificationListener(emitName, listener, filter, null);
            final int NOTIF_COUNT = 10;
            for (int i = 0; i < NOTIF_COUNT; i++) {
                emitter.emit();
                listener.await();
            }
            Thread.sleep(1);
            listener.checkUnavailable();
            System.out.println("Got notifications");
            cc.close();
            cs.stop();
            if (test == TestType.COUNT_EXECUTOR) {
                Method m = Executor.class.getMethod("execute", Runnable.class);
                Integer count = executorHandler.methodCount.get(m);
                if (count == null || count < NOTIF_COUNT)
                    return "Incorrect method count for execute: " + count;
                System.out.println("Executor was called enough times");
            }
        }
        wrappedExecutor.shutdown();
        return null;
    }
    public static interface EmitterMBean {
        public void emit();
    }
    public static class Emitter
            extends NotificationBroadcasterSupport implements EmitterMBean {
        public void emit() {
            sendNotification(new Notification(NOTIF_TYPE, this, seq++));
        }
        private long seq = 1;
    }
    private static class SemaphoreListener implements NotificationListener {
        void await() throws InterruptedException {
            semaphore.acquire();
        }
        void checkUnavailable() throws Exception {
            if (semaphore.tryAcquire())
                throw new Exception("Got extra notifications!");
        }
        public void handleNotification(Notification n, Object h) {
            semaphore.release();
        }
        private final Semaphore semaphore = new Semaphore(0);
    }
    private static class CountInvocationHandler implements InvocationHandler {
        final Map<Method, Integer> methodCount =
            new HashMap<Method, Integer>();
        private final Object wrapped;
        public CountInvocationHandler(Object wrapped) {
            this.wrapped = wrapped;
        }
        public Object invoke(Object proxy, Method method, Object[] args)
                throws Throwable {
            synchronized (methodCount) {
                Integer count = methodCount.get(method);
                if (count == null)
                    count = 0;
                methodCount.put(method, count + 1);
            }
            return method.invoke(wrapped, (Object[]) args);
        }
    }
}
