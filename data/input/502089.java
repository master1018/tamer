public class AbortReceiver extends BroadcastReceiver
{
    public AbortReceiver()
    {
    }
    public void onReceive(Context context, Intent intent)
    {
        try {
            IBinder caller = intent.getIBinderExtra("caller");
            Parcel data = Parcel.obtain();
            data.writeInterfaceToken(LaunchpadActivity.LAUNCH);
            data.writeString(LaunchpadActivity.RECEIVER_ABORT);
            caller.transact(LaunchpadActivity.GOT_RECEIVE_TRANSACTION, data, null, 0);
            data.recycle();
        } catch (RemoteException ex) {
        }
        abortBroadcast();
    }
}
