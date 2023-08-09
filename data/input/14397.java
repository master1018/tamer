public class UnsolicitedNotificationEvent extends java.util.EventObject {
    private UnsolicitedNotification notice;
    public UnsolicitedNotificationEvent(Object src,
        UnsolicitedNotification notice) {
        super(src);
        this.notice = notice;
    }
    public UnsolicitedNotification getNotification() {
        return notice;
    }
    public void dispatch(UnsolicitedNotificationListener listener) {
        listener.notificationReceived(this);
    }
    private static final long serialVersionUID = -2382603380799883705L;
}
