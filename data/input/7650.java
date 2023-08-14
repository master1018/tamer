public abstract class SendFailedNotification implements Notification {
    protected SendFailedNotification() {}
    @Override
    public abstract Association association();
    public abstract SocketAddress address();
    public abstract ByteBuffer buffer();
    public abstract int errorCode();
    public abstract int streamNumber();
}
