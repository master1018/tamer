public class JMXConnectionNotification extends Notification {
    private static final long serialVersionUID = -2331308725952627538L;
    public static final String OPENED = "jmx.remote.connection.opened";
    public static final String CLOSED = "jmx.remote.connection.closed";
    public static final String FAILED = "jmx.remote.connection.failed";
    public static final String NOTIFS_LOST =
        "jmx.remote.connection.notifs.lost";
    public JMXConnectionNotification(String type,
                                     Object source,
                                     String connectionId,
                                     long sequenceNumber,
                                     String message,
                                     Object userData) {
        super((String) nonNull(type),
              nonNull(source),
              Math.max(0, sequenceNumber),
              System.currentTimeMillis(),
              message);
        if (type == null || source == null || connectionId == null)
            throw new NullPointerException("Illegal null argument");
        if (sequenceNumber < 0)
            throw new IllegalArgumentException("Negative sequence number");
        this.connectionId = connectionId;
        setUserData(userData);
    }
    private static Object nonNull(Object arg) {
        if (arg == null)
            return "";
        else
            return arg;
    }
    public String getConnectionId() {
        return connectionId;
    }
    private final String connectionId;
}
