public class NotificationAccessControllerTest {
    public class NAC implements NotificationAccessController {
        private boolean throwException;
        public NAC(boolean throwException) {
            this.throwException = throwException;
        }
        public void addNotificationListener(
            String connectionId,
            ObjectName name,
            Subject subject)
            throws SecurityException {
            echo("addNotificationListener:");
            echo("\tconnectionId: " +  connectionId);
            echo("\tname: " +  name);
            echo("\tsubject: " +
                 (subject == null ? null : subject.getPrincipals()));
            if (throwException)
                if (name.getCanonicalName().equals("domain:name=1,type=NB")
                    &&
                    subject.getPrincipals().contains(new JMXPrincipal("role")))
                    throw new SecurityException();
        }
        public void removeNotificationListener(
            String connectionId,
            ObjectName name,
            Subject subject)
            throws SecurityException {
            echo("removeNotificationListener:");
            echo("\tconnectionId: " +  connectionId);
            echo("\tname: " +  name);
            echo("\tsubject: " +
                 (subject == null ? null : subject.getPrincipals()));
            if (throwException)
                if (name.getCanonicalName().equals("domain:name=2,type=NB")
                    &&
                    subject.getPrincipals().contains(new JMXPrincipal("role")))
                    throw new SecurityException();
        }
        public void fetchNotification(
            String connectionId,
            ObjectName name,
            Notification notification,
            Subject subject)
            throws SecurityException {
            echo("fetchNotification:");
            echo("\tconnectionId: " +  connectionId);
            echo("\tname: " +  name);
            echo("\tnotification: " +  notification);
            echo("\tsubject: " +
                 (subject == null ? null : subject.getPrincipals()));
            if (!throwException)
                if (name.getCanonicalName().equals("domain:name=2,type=NB") &&
                    subject.getPrincipals().contains(new JMXPrincipal("role")))
                    throw new SecurityException();
        }
    }
    public class CustomJMXAuthenticator implements JMXAuthenticator {
        public Subject authenticate(Object credentials) {
            String role = ((String[]) credentials)[0];
            echo("\nCreate principal with name = " + role);
            return new Subject(true,
                               Collections.singleton(new JMXPrincipal(role)),
                               Collections.EMPTY_SET,
                               Collections.EMPTY_SET);
        }
    }
    public interface NBMBean {
        public void emitNotification(int seqnum, ObjectName name);
    }
    public static class NB
        extends NotificationBroadcasterSupport
        implements NBMBean {
        public void emitNotification(int seqnum, ObjectName name) {
            if (name == null) {
                sendNotification(new Notification("nb", this, seqnum));
            } else {
                sendNotification(new Notification("nb", name, seqnum));
            }
        }
    }
    public class Listener implements NotificationListener {
        public List<Notification> notifs = new ArrayList<Notification>();
        public void handleNotification(Notification n, Object h) {
            echo("handleNotification:");
            echo("\tNotification = " + n);
            echo("\tNotification.SeqNum = " + n.getSequenceNumber());
            echo("\tHandback = " + h);
            notifs.add(n);
        }
    }
    public int checkNotifs(int size,
                           List<Notification> received,
                           List<ObjectName> expected) {
        if (received.size() != size) {
            echo("Error: expecting " + size + " notifications, got " +
                    received.size());
            return 1;
        } else {
            for (Notification n : received) {
                echo("Received notification: " + n);
                if (!n.getType().equals("nb")) {
                    echo("Notification type must be \"nb\"");
                    return 1;
                }
                ObjectName o = (ObjectName) n.getSource();
                int index = (int) n.getSequenceNumber();
                ObjectName nb = expected.get(index);
                if (!o.equals(nb)) {
                    echo("Notification source must be " + nb);
                    return 1;
                }
            }
        }
        return 0;
    }
    public int runTest(boolean enableChecks, boolean throwException)
        throws Exception {
        echo("\n=-=-= " + (enableChecks ? "Enable" : "Disable") +
             " notification access control checks " +
             (!enableChecks ? "" : (throwException ? ": add/remove " :
             ": fetch ")) + "=-=-=");
        JMXConnectorServer server = null;
        JMXConnector client = null;
        final MBeanServer mbs = MBeanServerFactory.createMBeanServer();
        try {
            final Map<String,Object> env = new HashMap<String,Object>();
            env.put("jmx.remote.authenticator", new CustomJMXAuthenticator());
            if (enableChecks) {
                env.put("com.sun.jmx.remote.notification.access.controller",
                        new NAC(throwException));
            }
            final JMXServiceURL url = new JMXServiceURL("service:jmx:rmi:
            server = JMXConnectorServerFactory.newJMXConnectorServer(url,
                                                                     env,
                                                                     mbs);
            server.start();
            final Map<String,Object> cenv = new HashMap<String,Object>();
            String[] credentials = new String[] { "role" , "password" };
            cenv.put("jmx.remote.credentials", credentials);
            client = JMXConnectorFactory.connect(server.getAddress(), cenv);
            final MBeanServerConnection mbsc =
                client.getMBeanServerConnection();
            ObjectName nb1 = ObjectName.getInstance("domain:type=NB,name=1");
            ObjectName nb2 = ObjectName.getInstance("domain:type=NB,name=2");
            ObjectName nb3 = ObjectName.getInstance("domain:type=NB,name=3");
            mbsc.createMBean(NB.class.getName(), nb1);
            mbsc.createMBean(NB.class.getName(), nb2);
            mbsc.createMBean(NB.class.getName(), nb3);
            Listener li = new Listener();
            try {
                mbsc.addNotificationListener(nb1, li, null, null);
                if (enableChecks && throwException) {
                    echo("Didn't get expected exception");
                    return 1;
                }
            } catch (SecurityException e) {
                if (enableChecks && throwException) {
                    echo("Got expected exception: " + e);
                } else {
                    echo("Got unexpected exception: " + e);
                    return 1;
                }
            }
            mbsc.addNotificationListener(nb2, li, null, null);
            mbsc.invoke(nb1, "emitNotification",
                new Object[] {0, null},
                new String[] {"int", "javax.management.ObjectName"});
            mbsc.invoke(nb2, "emitNotification",
                new Object[] {1, null},
                new String[] {"int", "javax.management.ObjectName"});
            mbsc.invoke(nb2, "emitNotification",
                new Object[] {2, nb3},
                new String[] {"int", "javax.management.ObjectName"});
            Thread.sleep(2000);
            if (!throwException)
                mbsc.removeNotificationListener(nb1, li);
            try {
                mbsc.removeNotificationListener(nb2, li);
                if (enableChecks && throwException) {
                    echo("Didn't get expected exception");
                    return 1;
                }
            } catch (SecurityException e) {
                if (enableChecks && throwException) {
                    echo("Got expected exception: " + e);
                } else {
                    echo("Got unexpected exception: " + e);
                    return 1;
                }
            }
            int result = 0;
            List<ObjectName> sources = new ArrayList();
            sources.add(nb1);
            sources.add(nb2);
            sources.add(nb3);
            if (!enableChecks) {
                result = checkNotifs(3, li.notifs, sources);
            }
            if (enableChecks && !throwException) {
                result = checkNotifs(1, li.notifs, sources);
            }
            if (enableChecks && throwException) {
                result = checkNotifs(2, li.notifs, sources);
            }
            if (result > 0) {
                return result;
            }
        } catch (Exception e) {
            echo("Failed to perform operation: " + e);
            e.printStackTrace();
            return 1;
        } finally {
            if (client != null)
                client.close();
            if (server != null)
                server.stop();
            if (mbs != null)
                MBeanServerFactory.releaseMBeanServer(mbs);
        }
        return 0;
    }
    private static void echo(String message) {
        System.out.println(message);
    }
    public static void main(String[] args) throws Exception {
        System.out.println("\nTest notification access control.");
        NotificationAccessControllerTest nact =
            new NotificationAccessControllerTest();
        int error = 0;
        error += nact.runTest(false, false);
        error += nact.runTest(true, false);
        error += nact.runTest(true, true);
        if (error > 0) {
            final String msg = "\nTest FAILED! Got " + error + " error(s)";
            System.out.println(msg);
            throw new IllegalArgumentException(msg);
        } else {
            System.out.println("\nTest PASSED!");
        }
    }
}
