public class TargetedNotification implements Serializable {
    private static final long serialVersionUID = 7676132089779300926L;
    public TargetedNotification(Notification notification,
                                Integer listenerID) {
        if (notification == null) throw new
            IllegalArgumentException("Invalid notification: null");
        if (listenerID == null) throw new
            IllegalArgumentException("Invalid listener ID: null");
        this.notif = notification;
        this.id = listenerID;
    }
    public Notification getNotification() {
        return notif;
    }
    public Integer getListenerID() {
        return id;
    }
    public String toString() {
        return "{" + notif + ", " + id + "}";
    }
    private final Notification notif;
    private final Integer id;
}
