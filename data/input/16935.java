public abstract class PeerAddressChangeNotification
    implements Notification
{
    public enum AddressChangeEvent {
       ADDR_AVAILABLE,
       ADDR_UNREACHABLE,
       ADDR_REMOVED,
       ADDR_ADDED,
       ADDR_MADE_PRIMARY,
       ADDR_CONFIRMED;
    }
    protected PeerAddressChangeNotification() {}
    public abstract SocketAddress address();
    public abstract Association association();
    public abstract AddressChangeEvent event();
}
