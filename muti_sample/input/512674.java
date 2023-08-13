public class LocalReceiver extends BroadcastReceiver {
    public LocalReceiver() {
    }
    public void onReceive(Context context, Intent intent) {
        String resultString = LaunchpadActivity.RECEIVER_LOCAL;
        if (BroadcastTest.BROADCAST_FAIL_REGISTER.equals(intent.getAction())) {
            resultString = "Successfully registered, but expected it to fail";
            try {
                context.registerReceiver(this, new IntentFilter("foo.bar"));
                context.unregisterReceiver(this);
            } catch (ReceiverCallNotAllowedException e) {
                resultString = LaunchpadActivity.RECEIVER_LOCAL;
            }
        } else if (BroadcastTest.BROADCAST_FAIL_BIND.equals(intent.getAction())) {
            resultString = "Successfully bound to service, but expected it to fail";
            try {
                ServiceConnection sc = new ServiceConnection() {
                    public void onServiceConnected(ComponentName name, IBinder service) {
                    }
                    public void onServiceDisconnected(ComponentName name) {
                    }
                };
                context.bindService(new Intent(context, LocalService.class), sc, 0);
                context.unbindService(sc);
            } catch (ReceiverCallNotAllowedException e) {
                resultString = LaunchpadActivity.RECEIVER_LOCAL;
            }
        } else if (LaunchpadActivity.BROADCAST_REPEAT.equals(intent.getAction())) {
            Intent newIntent = new Intent(intent);
            newIntent.setAction(LaunchpadActivity.BROADCAST_LOCAL);
            context.sendOrderedBroadcast(newIntent, null);
        }
        try {
            IBinder caller = intent.getIBinderExtra("caller");
            Parcel data = Parcel.obtain();
            data.writeInterfaceToken(LaunchpadActivity.LAUNCH);
            data.writeString(resultString);
            caller.transact(LaunchpadActivity.GOT_RECEIVE_TRANSACTION, data, null, 0);
            data.recycle();
        } catch (RemoteException ex) {
        }
    }
}
