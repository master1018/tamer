public class DiffHBTest {
    private static final String[] protocols = {"rmi", "iiop", "jmxmp"};
    private static final MBeanServer mbs = MBeanServerFactory.createMBeanServer();
    private static ObjectName delegateName;
    private static ObjectName timerName;
    public static final String[] hbs = new String[] {"0", "1"};
    public static void main(String[] args) throws Exception {
        System.out.println(">>> test on one listener with two different handbacks.");
        delegateName = new ObjectName("JMImplementation:type=MBeanServerDelegate");
        timerName = new ObjectName("MBean:name=Timer");
        String errors = "";
        for (int i = 0; i < protocols.length; i++) {
            final String s = test(protocols[i]);
            if (s != null) {
                if ("".equals(errors)) {
                    errors = "Failed to " + protocols[i] + ": "+s;
                } else {
                    errors = "\tFailed to " + protocols[i] + ": "+s;
                }
            }
        }
        if ("".equals(errors)) {
            System.out.println(">>> Passed!");
        } else {
            System.out.println(">>> Failed!");
            throw new RuntimeException(errors);
        }
    }
    private static String test(String proto) throws Exception {
        System.out.println(">>> Test for protocol " + proto);
        JMXServiceURL u = new JMXServiceURL(proto, null, 0);
        JMXConnectorServer server;
        JMXConnector client;
        try {
            server =
                    JMXConnectorServerFactory.newJMXConnectorServer(u, null, mbs);
            server.start();
            JMXServiceURL addr = server.getAddress();
            client = JMXConnectorFactory.connect(addr, null);
        } catch (Exception e) {
            System.out.println(">>> not support: " + proto);
            return null;
        }
        MBeanServerConnection mserver = client.getMBeanServerConnection();
        System.out.print(">>>\t");
        for (int i=0; i<5; i++) {
            System.out.print(i + "\t");
            final MyListener dummyListener = new MyListener();
            mserver.addNotificationListener(
                    delegateName, dummyListener, null, hbs[0]);
            mserver.addNotificationListener(
                    delegateName, dummyListener, null, hbs[1]);
            mserver.createMBean("javax.management.timer.Timer", timerName);
            long remainingTime = waitingTime;
            final long startTime = System.currentTimeMillis();
            try {
                synchronized(dummyListener) {
                    while (!dummyListener.done && remainingTime > 0) {
                        dummyListener.wait(remainingTime);
                        remainingTime = waitingTime -
                                (System.currentTimeMillis() - startTime);
                    }
                    if (dummyListener.errorInfo != null) {
                        return dummyListener.errorInfo;
                    }
                }
            } finally {
                mserver.unregisterMBean(timerName);
                mserver.removeNotificationListener(delegateName, dummyListener);
            }
        }
        System.out.println("");
        client.close();
        server.stop();
        return null;
    }
    private static class MyListener implements NotificationListener {
        public boolean done = false;
        public String errorInfo = null;
        private int received = 0;
        private MBeanServerNotification receivedNotif = null;
        private Object receivedHB = null;
        public void handleNotification(Notification n, Object o) {
            if (!(n instanceof MBeanServerNotification)) {
                failed("Received an unexpected notification: "+n);
                return;
            }
            if (!hbs[0].equals(o) && !hbs[1].equals(o)) {
                failed("Unkown handback: "+o);
                return;
            }
            final MBeanServerNotification msn = (MBeanServerNotification)n;
            if (!(MBeanServerNotification.REGISTRATION_NOTIFICATION.equals(
                    msn.getType())) ||
                    !msn.getMBeanName().equals(timerName)) {
                return;
            }
            synchronized(this) {
                received++;
                if (received == 1) { 
                    receivedNotif = msn;
                    receivedHB = o;
                    return;
                }
                if (received > 2) {
                    failed("Expect to receive 2 notifs,  but get "+received);
                    return;
                }
                if (receivedHB.equals(o)) {
                    failed("Got same handback twice: "+o);
                } else if(!hbs[0].equals(o) && !hbs[1].equals(o)) {
                    failed("Unknown handback: "+o);
                } else if (receivedNotif.getSequenceNumber() !=
                        msn.getSequenceNumber()) {
                    failed("expected to receive:\n"
                            +receivedNotif
                            +"\n but got\n"+msn);
                }
                done = true;
                this.notify();
            }
        }
        private void failed(String errorInfo) {
            this.errorInfo = errorInfo;
            done = true;
            this.notify();
        }
    }
    private final static long waitingTime = 2000;
}
