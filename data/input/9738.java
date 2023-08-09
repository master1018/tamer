public class AbstractNotificationHandler<T>
    implements NotificationHandler<T>
{
    protected AbstractNotificationHandler() {}
    @Override
    public HandlerResult handleNotification(Notification notification,
                                            T attachment) {
        return HandlerResult.CONTINUE;
    }
    public HandlerResult handleNotification(AssociationChangeNotification notification,
                                            T attachment) {
        return HandlerResult.CONTINUE;
    }
    public HandlerResult handleNotification(PeerAddressChangeNotification notification,
                                            T attachment) {
        return HandlerResult.CONTINUE;
    }
    public HandlerResult handleNotification(SendFailedNotification notification,
                                            T attachment) {
        return HandlerResult.CONTINUE;
    }
    public HandlerResult handleNotification(ShutdownNotification notification,
                                            T attachment) {
        return HandlerResult.CONTINUE;
    }
}
