public class TimerNotification extends javax.management.Notification {
    private static final long serialVersionUID = 1798492029603825750L;
    private Integer notificationID;
    public TimerNotification(String type, Object source, long sequenceNumber, long timeStamp, String msg, Integer id) {
        super(type, source, sequenceNumber, timeStamp, msg);
        this.notificationID = id;
    }
    public Integer getNotificationID() {
        return notificationID;
    }
    Object cloneTimerNotification() {
        TimerNotification clone = new TimerNotification(this.getType(), this.getSource(), this.getSequenceNumber(),
                                                        this.getTimeStamp(), this.getMessage(), notificationID);
        clone.setUserData(this.getUserData());
        return clone;
    }
}
