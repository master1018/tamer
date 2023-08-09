public class NotifReconnectDeadlockTest {
    public static void main(String[] args) throws Exception {
        System.out.println(
           ">>> Tests reconnection done by a fetching notif thread.");
        ObjectName oname = new ObjectName ("Default:name=NotificationEmitter");
        JMXServiceURL url = new JMXServiceURL("rmi", null, 0);
        Map env = new HashMap(2);
        env.put("jmx.remote.x.server.connection.timeout", new Long(serverTimeout));
        env.put("jmx.remote.x.client.connection.check.period", new Long(Long.MAX_VALUE));
        final MBeanServer mbs = MBeanServerFactory.newMBeanServer();
        mbs.registerMBean(new NotificationEmitter(), oname);
        JMXConnectorServer server = JMXConnectorServerFactory.newJMXConnectorServer(
                                                                               url,
                                                                               env,
                                                                               mbs);
        server.start();
        JMXServiceURL addr = server.getAddress();
        JMXConnector client = JMXConnectorFactory.connect(addr, env);
        Thread.sleep(100); 
        client.getMBeanServerConnection().addNotificationListener(oname,
                                                                  listener,
                                                                  null,
                                                                  null);
        client.addConnectionNotificationListener(listener, null, null);
        final long end = System.currentTimeMillis()+120000;
        synchronized(lock) {
            while(clientState == null && System.currentTimeMillis() < end) {
                mbs.invoke(oname, "sendNotifications",
                           new Object[] {new Notification("MyType", "", 0)},
                           new String[] {"javax.management.Notification"});
                try {
                    lock.wait(10);
                } catch (Exception e) {}
            }
        }
        if (clientState == null) {
            throw new RuntimeException(
                  "No reconnection happened, need to reconfigure the test.");
        } else if (JMXConnectionNotification.FAILED.equals(clientState) ||
                   JMXConnectionNotification.CLOSED.equals(clientState)) {
            throw new RuntimeException("Failed to reconnect.");
        }
        System.out.println(">>> Passed!");
        client.removeConnectionNotificationListener(listener);
        client.close();
        server.stop();
    }
    public static class NotificationEmitter extends NotificationBroadcasterSupport
        implements NotificationEmitterMBean {
        public void sendNotifications(Notification n) {
            sendNotification(n);
        }
    }
    public interface NotificationEmitterMBean {
        public void sendNotifications(Notification n);
    }
    private final static NotificationListener listener = new NotificationListener() {
            public void handleNotification(Notification n, Object hb) {
                if (n instanceof JMXConnectionNotification) {
                    if (!JMXConnectionNotification.NOTIFS_LOST.equals(n.getType())) {
                        clientState = n.getType();
                        System.out.println(
                           ">>> The client state has been changed to: "+clientState);
                        synchronized(lock) {
                            lock.notifyAll();
                        }
                    }
                    return;
                }
                System.out.println(">>> Do sleep to make reconnection.");
                synchronized(lock) {
                    try {
                        lock.wait(listenerSleep);
                    } catch (Exception e) {
                    }
                }
            }
        };
    private static final long serverTimeout = 1000;
    private static final long listenerSleep = serverTimeout*6;
    private static String clientState = null;
    private static final int[] lock = new int[0];
}
