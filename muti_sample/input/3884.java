public abstract class JMXConnectorServer
        extends NotificationBroadcasterSupport
        implements JMXConnectorServerMBean, MBeanRegistration, JMXAddressable {
    public static final String AUTHENTICATOR =
        "jmx.remote.authenticator";
    public JMXConnectorServer() {
        this(null);
    }
    public JMXConnectorServer(MBeanServer mbeanServer) {
        this.mbeanServer = mbeanServer;
    }
    public synchronized MBeanServer getMBeanServer() {
        return mbeanServer;
    }
    public synchronized void setMBeanServerForwarder(MBeanServerForwarder mbsf)
    {
        if (mbsf == null)
            throw new IllegalArgumentException("Invalid null argument: mbsf");
        if (mbeanServer !=  null) mbsf.setMBeanServer(mbeanServer);
        mbeanServer = mbsf;
    }
    public String[] getConnectionIds() {
        synchronized (connectionIds) {
            return connectionIds.toArray(new String[connectionIds.size()]);
        }
    }
    public JMXConnector toJMXConnector(Map<String,?> env)
        throws IOException
    {
        if (!isActive()) throw new
            IllegalStateException("Connector is not active");
        JMXServiceURL addr = getAddress();
        return JMXConnectorFactory.newJMXConnector(addr, env);
    }
    @Override
    public MBeanNotificationInfo[] getNotificationInfo() {
        final String[] types = {
            JMXConnectionNotification.OPENED,
            JMXConnectionNotification.CLOSED,
            JMXConnectionNotification.FAILED,
        };
        final String className = JMXConnectionNotification.class.getName();
        final String description =
            "A client connection has been opened or closed";
        return new MBeanNotificationInfo[] {
            new MBeanNotificationInfo(types, className, description),
        };
    }
    protected void connectionOpened(String connectionId,
                                    String message,
                                    Object userData) {
        if (connectionId == null)
            throw new NullPointerException("Illegal null argument");
        synchronized (connectionIds) {
            connectionIds.add(connectionId);
        }
        sendNotification(JMXConnectionNotification.OPENED, connectionId,
                         message, userData);
    }
    protected void connectionClosed(String connectionId,
                                    String message,
                                    Object userData) {
        if (connectionId == null)
            throw new NullPointerException("Illegal null argument");
        synchronized (connectionIds) {
            connectionIds.remove(connectionId);
        }
        sendNotification(JMXConnectionNotification.CLOSED, connectionId,
                         message, userData);
    }
    protected void connectionFailed(String connectionId,
                                    String message,
                                    Object userData) {
        if (connectionId == null)
            throw new NullPointerException("Illegal null argument");
        synchronized (connectionIds) {
            connectionIds.remove(connectionId);
        }
        sendNotification(JMXConnectionNotification.FAILED, connectionId,
                         message, userData);
    }
    private void sendNotification(String type, String connectionId,
                                  String message, Object userData) {
        Notification notif =
            new JMXConnectionNotification(type,
                                          getNotificationSource(),
                                          connectionId,
                                          nextSequenceNumber(),
                                          message,
                                          userData);
        sendNotification(notif);
    }
    private synchronized Object getNotificationSource() {
        if (myName != null)
            return myName;
        else
            return this;
    }
    private static long nextSequenceNumber() {
        synchronized (sequenceNumberLock) {
            return sequenceNumber++;
        }
    }
    public synchronized ObjectName preRegister(MBeanServer mbs,
                                               ObjectName name) {
        if (mbs == null || name == null)
            throw new NullPointerException("Null MBeanServer or ObjectName");
        if (mbeanServer == null) {
            mbeanServer = mbs;
            myName = name;
        }
        return name;
    }
    public void postRegister(Boolean registrationDone) {
    }
    public synchronized void preDeregister() throws Exception {
        if (myName != null && isActive()) {
            stop();
            myName = null; 
        }
    }
    public void postDeregister() {
        myName = null;
    }
    private MBeanServer mbeanServer = null;
    private ObjectName myName;
    private final List<String> connectionIds = new ArrayList<String>();
    private static final int[] sequenceNumberLock = new int[0];
    private static long sequenceNumber;
}
