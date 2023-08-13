public class EmptyDomainNotificationTest {
    public static interface SimpleMBean {
        public void emitNotification();
    }
    public static class Simple
        extends NotificationBroadcasterSupport
        implements SimpleMBean {
        public void emitNotification() {
            sendNotification(new Notification("simple", this, 0));
        }
    }
    public static class Listener implements NotificationListener {
        public void handleNotification(Notification n, Object h) {
        System.out.println(
          "EmptyDomainNotificationTest-Listener-handleNotification: receives:" + n);
            if (n.getType().equals("simple")) {
                synchronized(this) {
                    received++;
                    this.notifyAll();
                }
            }
        }
        public int received;
    }
    public static void main(String[] args) throws Exception {
        final MBeanServer mbs = MBeanServerFactory.createMBeanServer();
        final JMXServiceURL url = new JMXServiceURL("service:jmx:rmi:
        JMXConnectorServer server = JMXConnectorServerFactory.newJMXConnectorServer(url, null, mbs);
        server.start();
        JMXConnector client = JMXConnectorFactory.connect(server.getAddress(), null);
        final MBeanServerConnection mbsc = client.getMBeanServerConnection();
        final ObjectName mbean = ObjectName.getInstance(":type=Simple");
        mbsc.createMBean(Simple.class.getName(), mbean);
        System.out.println("EmptyDomainNotificationTest-main: add a listener ...");
        final Listener li = new Listener();
        mbsc.addNotificationListener(mbean, li, null, null);
        System.out.println("EmptyDomainNotificationTest-main: ask to send a notif ...");
        mbsc.invoke(mbean, "emitNotification", null, null);
        System.out.println("EmptyDomainNotificationTest-main: waiting notif...");
        final long stopTime = System.currentTimeMillis() + 2000;
        synchronized(li) {
            long toWait = stopTime - System.currentTimeMillis();
            while (li.received < 1 && toWait > 0) {
                li.wait(toWait);
                toWait = stopTime - System.currentTimeMillis();
            }
        }
        if (li.received < 1) {
            throw new RuntimeException("No notif received!");
        } else if (li.received > 1) {
            throw new RuntimeException("Wait one notif but got: "+li.received);
        }
        System.out.println("EmptyDomainNotificationTest-main: Got the expected notif!");
        System.out.println("EmptyDomainNotificationTest-main: remove the listener.");
        mbsc.removeNotificationListener(mbean, li);
        client.close();
        server.stop();
        System.out.println("EmptyDomainNotificationTest-main: Bye.");
    }
}
