public class NotificationResult implements Serializable {
    private static final long serialVersionUID = 1191800228721395279L;
    public NotificationResult(long earliestSequenceNumber,
                              long nextSequenceNumber,
                              TargetedNotification[] targetedNotifications) {
        if (targetedNotifications == null) {
            final String msg = "Notifications null";
            throw new IllegalArgumentException(msg);
        }
        if (earliestSequenceNumber < 0 || nextSequenceNumber < 0)
            throw new IllegalArgumentException("Bad sequence numbers");
        this.earliestSequenceNumber = earliestSequenceNumber;
        this.nextSequenceNumber = nextSequenceNumber;
        this.targetedNotifications = targetedNotifications;
    }
    public long getEarliestSequenceNumber() {
        return earliestSequenceNumber;
    }
    public long getNextSequenceNumber() {
        return nextSequenceNumber;
    }
    public TargetedNotification[] getTargetedNotifications() {
        return targetedNotifications;
    }
    public String toString() {
        return "NotificationResult: earliest=" + getEarliestSequenceNumber() +
            "; next=" + getNextSequenceNumber() + "; nnotifs=" +
            getTargetedNotifications().length;
    }
    private final long earliestSequenceNumber;
    private final long nextSequenceNumber;
    private final TargetedNotification[] targetedNotifications;
}
