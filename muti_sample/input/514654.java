public class LocalGrantedReceiver extends BroadcastReceiver {
    public LocalGrantedReceiver() {
    }
    public void onReceive(Context context, Intent intent) {
        try {
            IBinder caller = intent.getIBinderExtra("caller");
            Parcel data = Parcel.obtain();
            data.writeInterfaceToken(LaunchpadActivity.LAUNCH);
            data.writeString(BroadcastTest.RECEIVER_LOCAL);
            caller.transact(BroadcastTest.GOT_RECEIVE_TRANSACTION, data, null, 0);
            data.recycle();
        } catch (RemoteException ex) {
        }
    }
}
