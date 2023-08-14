public class DeadListenerTest {
    public static void main(String[] args) throws Exception {
        final ObjectName delegateName = MBeanServerDelegate.DELEGATE_NAME;
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        Noddy mbean = new Noddy();
        ObjectName name = new ObjectName("d:k=v");
        mbs.registerMBean(mbean, name);
        JMXServiceURL url = new JMXServiceURL("service:jmx:rmi:
        SnoopRMIServerImpl rmiServer = new SnoopRMIServerImpl();
        RMIConnectorServer cs = new RMIConnectorServer(url, null, rmiServer, mbs);
        cs.start();
        JMXServiceURL addr = cs.getAddress();
        assertTrue("No connections in new connector server", rmiServer.connections.isEmpty());
        JMXConnector cc = JMXConnectorFactory.connect(addr);
        MBeanServerConnection mbsc = cc.getMBeanServerConnection();
        assertTrue("One connection on server after client connect", rmiServer.connections.size() == 1);
        RMIConnectionImpl connection = rmiServer.connections.get(0);
        Method getServerNotifFwdM = RMIConnectionImpl.class.getDeclaredMethod("getServerNotifFwd");
        getServerNotifFwdM.setAccessible(true);
        ServerNotifForwarder serverNotifForwarder = (ServerNotifForwarder) getServerNotifFwdM.invoke(connection);
        Field listenerMapF = ServerNotifForwarder.class.getDeclaredField("listenerMap");
        listenerMapF.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<ObjectName, Set<?>> listenerMap = (Map<ObjectName, Set<?>>) listenerMapF.get(serverNotifForwarder);
        assertTrue("Server listenerMap initially empty", mapWithoutKey(listenerMap, delegateName).isEmpty());
        CountListener count1 = new CountListener();
        mbsc.addNotificationListener(name, count1, null, null);
        CountListener count2 = new CountListener();
        NotificationFilterSupport dummyFilter = new NotificationFilterSupport();
        dummyFilter.enableType("");
        mbsc.addNotificationListener(name, count2, dummyFilter, "noddy");
        assertTrue("One entry in listenerMap for two listeners on same MBean", mapWithoutKey(listenerMap, delegateName).size() == 1);
        Set<?> set = listenerMap.get(name);
        assertTrue("Set in listenerMap for MBean has two elements", set != null && set.size() == 2);
        assertTrue("Initial value of count1 == 0", count1.count() == 0);
        assertTrue("Initial value of count2 == 0", count2.count() == 0);
        Notification notif = new Notification("type", name, 0);
        mbean.sendNotification(notif);
        long deadline = System.currentTimeMillis() + 2000;
        while ((count1.count() != 1 || count2.count() != 1) && System.currentTimeMillis() < deadline) {
            Thread.sleep(10);
        }
        assertTrue("New value of count1 == 1", count1.count() == 1);
        assertTrue("Initial value of count2 == 1", count2.count() == 1);
        CountListener count3 = new CountListener();
        try {
            mbsc.removeNotificationListener(name, count3);
            assertTrue("Remove of nonexistent listener succeeded but should not have", false);
        } catch (ListenerNotFoundException e) {
        }
        ObjectName nonexistent = new ObjectName("foo:bar=baz");
        assertTrue("Nonexistent is nonexistent", !mbs.isRegistered(nonexistent));
        try {
            mbsc.removeNotificationListener(nonexistent, count3);
            assertTrue("Remove of listener from nonexistent MBean succeeded but should not have", false);
        } catch (ListenerNotFoundException e) {
        }
        mbs.unregisterMBean(name);
        mbean.sendNotification(notif);
        Thread.sleep(200);
        assertTrue("New value of count1 == 1", count1.count() == 1);
        assertTrue("Initial value of count2 == 1", count2.count() == 1);
        Set<?> setForUnreg = listenerMap.get(name);
        assertTrue("No trace of unregistered MBean: " + setForUnreg, setForUnreg == null);
        try {
            mbsc.removeNotificationListener(name, count1);
            assertTrue("Remove of count1 listener should have failed", false);
        } catch (ListenerNotFoundException e) {
        }
        try {
            mbsc.removeNotificationListener(name, count2, dummyFilter, "noddy");
            assertTrue("Remove of count2 listener should have failed", false);
        } catch (ListenerNotFoundException e) {
        }
    }
    private static <K, V> Map<K, V> mapWithoutKey(Map<K, V> map, K key) {
        Map<K, V> copy = new HashMap<K, V>(map);
        copy.remove(key);
        return copy;
    }
    public static interface NoddyMBean {}
    public static class Noddy extends NotificationBroadcasterSupport implements NoddyMBean {}
    public static class CountListener implements NotificationListener {
        final AtomicInteger count;
        public CountListener() {
            this.count = new AtomicInteger();
        }
        int count() {
            return count.get();
        }
        public void handleNotification(Notification notification, Object handback) {
            count.incrementAndGet();
        }
    }
    private static void assertTrue(String what, boolean cond) {
        if (!cond) {
            throw new AssertionError("Assertion failed: " + what);
        }
    }
    private static class SnoopRMIServerImpl extends RMIJRMPServerImpl {
        final List<RMIConnectionImpl> connections = new ArrayList<RMIConnectionImpl>();
        SnoopRMIServerImpl() throws IOException {
            super(0, null, null, null);
        }
        @Override
        protected RMIConnection makeClient(String id, Subject subject) throws IOException {
            RMIConnectionImpl conn = (RMIConnectionImpl) super.makeClient(id, subject);
            connections.add(conn);
            return conn;
        }
    }
}
