public abstract class AssociationChangeNotification
    implements Notification
{
    public enum AssocChangeEvent
    {
        COMM_UP,
       COMM_LOST,
       RESTART,
       SHUTDOWN,
       CANT_START
    }
    protected AssociationChangeNotification() {}
    public abstract Association association();
    public abstract AssocChangeEvent event();
}
