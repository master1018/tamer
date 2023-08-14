public class RMINotifTest {
    public static void main(String[] args) {
        try {
            Registry reg = null;
            int port = 6666;
            final Random r = new Random();
            while(port++<7000) {
                try {
                    reg = LocateRegistry.createRegistry(++port);
                    System.out.println("Creation of rmi registry succeeded. Running on port " + port);
                    break;
                } catch (RemoteException re) {
                }
            }
            if (reg == null) {
                System.out.println("Failed to create a RMI registry, "+
                                   "the ports from 6666 to 6999 are all occupied.");
                System.exit(1);
            }
            MBeanServer server = MBeanServerFactory.createMBeanServer();
            ObjectName mbean = new ObjectName ("Default:name=NotificationEmitter");
            server.registerMBean(new NotificationEmitter(), mbean);
            JMXServiceURL url =
                new JMXServiceURL("rmi", null, port,
                                  "/jndi/rmi:
            System.out.println("RMIConnectorServer address " + url);
            JMXConnectorServer sServer =
                JMXConnectorServerFactory.newJMXConnectorServer(url, null,
                                                                null);
            ObjectInstance ss = server.registerMBean(sServer, new ObjectName("Default:name=RmiConnectorServer"));
            sServer.start();
            JMXConnector rmiConnection =
                JMXConnectorFactory.newJMXConnector(url, null);
            rmiConnection.connect(null);
            MBeanServerConnection client = rmiConnection.getMBeanServerConnection();
            client.addNotificationListener(mbean, listener, null, null);
            Object[] params = new Object[1];
            String[] signatures = new String[1];
            params[0] = new Integer(nb);
            signatures[0] = "java.lang.Integer";
            client.invoke(mbean, "sendNotifications", params, signatures);
            synchronized (lock) {
                if (receivedNotifs != nb) {
                    lock.wait(10000);
                    System.out.println(">>> Received notifications..."+receivedNotifs);
                }
            }
            if (receivedNotifs != nb) {
                System.exit(1);
            } else {
                System.out.println("The client received all notifications.");
            }
            client.removeNotificationListener(mbean, listener);
            NotificationFilterSupport filter = new NotificationFilterSupport();
            Object o = new Object();
            client.addNotificationListener(mbean, listener, filter, o);
            client.removeNotificationListener(mbean, listener, filter, o);
            sServer.stop();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
    private static class Listener implements NotificationListener {
        public void handleNotification(Notification notif, Object handback) {
            if(++receivedNotifs == nb) {
                synchronized(lock) {
                    lock.notifyAll();
                }
            }
        }
    }
    public static class NotificationEmitter extends NotificationBroadcasterSupport implements NotificationEmitterMBean {
        public MBeanNotificationInfo[] getNotificationInfo() {
            MBeanNotificationInfo[] ntfInfoArray  = new MBeanNotificationInfo[1];
            String[] ntfTypes = new String[1];
            ntfTypes[0] = myType;
            ntfInfoArray[0] = new MBeanNotificationInfo(ntfTypes,
                                                        "javax.management.Notification",
                                                        "Notifications sent by the NotificationEmitter");
            return ntfInfoArray;
        }
        public void sendNotifications(Integer nb) {
            System.out.println("===NotificationEmitter: be asked to send notifications: "+nb);
            Notification notif;
            for (int i=1; i<=nb.intValue(); i++) {
                notif = new Notification(myType, this, i);
                sendNotification(notif);
            }
        }
        private String myType = "notification.my_notification";
    }
    public interface NotificationEmitterMBean {
        public void sendNotifications(Integer nb);
    }
    private static NotificationListener listener = new Listener();
    private static int nb = 10;
    private static int receivedNotifs = 0;
    private static int[] lock = new int[0];
}
