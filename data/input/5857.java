public class ServerNotifs {
    private static void echo(String msg) {
        System.out.println(msg);
    }
    public static void main(String[] args) {
        try {
            echo("---Create the MBeanServer...");
            MBeanServer mbs = MBeanServerFactory.createMBeanServer();
            echo("---Instantiate the RMIConnectorServer...");
            JMXServiceURL url = new JMXServiceURL("service:jmx:rmi:
            JMXConnectorServer cs =
                JMXConnectorServerFactory.newJMXConnectorServer(url,
                                                                null,
                                                                mbs);
            echo("---Register the RMIConnectorServer in the MBeanServer...");
            ObjectName on =
                new ObjectName("JMXConnectors:name=RMIConnectorServer");
            mbs.registerMBean(cs, on);
            echo("---Start the RMIConnectorServer...");
            cs.start();
            url = cs.getAddress();
            echo("---RMIConnectorServer address: " + url);
            echo("---Add a local listener to the RMIConnectorServer...");
            mbs.addNotificationListener(on, new MyListener(), null, null);
            echo("---Instantiate the RMIConnector...");
            JMXConnector c = JMXConnectorFactory.newJMXConnector(url, null);
            echo("---Open connection...");
            c.connect(null);
            Thread.sleep(100);
            echo("---Close connection...");
            c.close();
            Thread.sleep(100);
            synchronized(waiting) {
                if (!succeeded) {
                    final long waitingTime = 10000;
                    long remainingTime = waitingTime;
                    final long startTime = System.currentTimeMillis();
                    while (!succeeded && remainingTime > 0) {
                        waiting.wait(remainingTime);
                        remainingTime = waitingTime -
                            (System.currentTimeMillis() - startTime);
                    }
                }
            }
            echo("---Stop the RMIConnectorServer...");
            cs.stop();
            if (!succeeded) {
                System.out.println("Timeout, did not get all notifications!");
                System.exit(1);
            }
        } catch (MBeanException mbe) {
            echo("---Test failed.");
            echo("---Got exception: " + mbe);
            mbe.getTargetException().printStackTrace();
            System.exit(1);
        } catch (RuntimeOperationsException roe) {
            echo("---Test failed.");
            echo("---Got exception: " + roe);
            roe.getTargetException().printStackTrace();
            System.exit(1);
        } catch (Throwable t) {
            echo("---Test failed.");
            echo("---Got throwable: " + t);
            t.printStackTrace();
            System.exit(1);
        }
    }
    private static class MyListener implements NotificationListener {
        public void handleNotification(Notification n, Object o) {
            if (index == types.length) {
                return;
            }
            echo("---Got a notification: " + n.getType());
            echo(n.getMessage());
            if (n instanceof JMXConnectionNotification) {
                if (!n.getType().equals(types[index++])) {
                    System.out.println("Waiting to get a notification with " +
                                       "type: " + types[index-1] + ", but " +
                                       "got one with type: " + n.getType());
                    System.exit(1);
                }
                if (index == types.length) {
                    synchronized(waiting) {
                        succeeded = true;
                        waiting.notify();
                    }
                }
            }
        }
    }
    private static final String[] types =
        new String[] {JMXConnectionNotification.OPENED,
                      JMXConnectionNotification.CLOSED};
    private static int index = 0;
    private static int[] waiting = new int[0];
    private static boolean succeeded = false;
}
