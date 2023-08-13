public class MessengerService extends Service {
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Message reply = Message.obtain();
            reply.copyFrom(msg);
            try {
                msg.replyTo.send(reply);
            } catch (RemoteException e) {
            }
        }
    };
    private final Messenger mMessenger = new Messenger(mHandler);
    public MessengerService() {
    }
    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }
}
