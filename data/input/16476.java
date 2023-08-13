public class ConnectionListenerNullTest {
    static final boolean optionalFlag;
    static {
        Class genericClass = null;
        try {
            genericClass =
            Class.forName("javax.management.remote.generic.GenericConnector");
        } catch (ClassNotFoundException x) {
        }
        optionalFlag = (genericClass != null);
    }
    final static String[] mandatoryList = {
        "service:jmx:rmi:
    };
    final static String[] optionalList = {
        "service:jmx:jmxmp:
    };
    public static int test(String[] urls) {
        int errCount = 0;
        for (int i=0;i<urls.length;i++) {
            try {
                final JMXServiceURL url = new JMXServiceURL(urls[i]);
                final JMXConnector c =
                    JMXConnectorFactory.newJMXConnector(url,(Map)null);
                final NotificationListener nl = null;
                final NotificationFilter   nf = null;
                final Object               h  = null;
                System.out.println("Testing " + c.getClass().getName());
                try {
                    System.out.println(
                        "addConnectionNotificationListener(null,null,null)");
                    c.addConnectionNotificationListener(nl,nf,h);
                    throw new AssertionError("No exception raised");
                } catch (NullPointerException npe) {
                }
                final NotificationListener listener = new
                   NotificationListener() {
                     public void handleNotification(Notification notification,
                                                   Object handback) {
                   }
                };
                c.addConnectionNotificationListener(listener,nf,h);
                try {
                    System.out.println(
                           "removeConnectionNotificationListener(null)");
                    c.removeConnectionNotificationListener(nl);
                    throw new AssertionError("No exception raised");
                } catch (NullPointerException npe) {
                }
                try {
                    System.out.println(
                      "removeConnectionNotificationListener(null,null,null)");
                    c.removeConnectionNotificationListener(nl,nf,h);
                    throw new AssertionError("No exception raised");
                } catch (NullPointerException npe) {
                }
                c.removeConnectionNotificationListener(listener);
                System.out.println(c.getClass().getName() +
                                   " successfully tested.");
            } catch (Exception x) {
                System.err.println("Unexpected exception for " +
                                   urls[i] + ": " + x);
                x.printStackTrace();
                errCount++;
            } catch (AssertionError e) {
                System.err.println("Unexpected assertion error for " +
                                   urls[i] + ": " + e);
                e.printStackTrace();
                errCount++;
            }
        }
        return errCount;
    }
    public static void main(String args[]) {
        int errCount = 0;
        errCount += test(mandatoryList);
        if (optionalFlag) errCount += test(optionalList);
        if (errCount > 0) {
            System.err.println("ConnectionListenerNullTest failed: " +
                               errCount + " error(s) reported.");
            System.exit(1);
        }
        System.out.println("ConnectionListenerNullTest passed.");
    }
}
