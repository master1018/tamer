public class NoProcessOutgoingCallPermissionTest extends AndroidTestCase {
    private static final int WAIT_TIME = 2 * 60 * 1000;
    private static final String LOG_TAG = "NoProcessOutgoingCallPermissionTest";
    private void callPhone() {
        Uri uri = Uri.parse("tel:123456");
        Intent intent = new Intent(Intent.ACTION_CALL, uri);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);
        Log.i(LOG_TAG, "Called phone: " + uri.toString());
    }
    public void testProcessOutgoingCall() {
        Log.i(LOG_TAG, "Beginning testProcessOutgoingCall");
        OutgoingCallBroadcastReceiver rcvr = new OutgoingCallBroadcastReceiver();
        Intent ntnt = mContext.registerReceiver(rcvr,
            new IntentFilter(Intent.ACTION_NEW_OUTGOING_CALL));
        Log.i(LOG_TAG, "registerReceiver --> " + ntnt);
        if (null != ntnt) {
            Bundle xtrs = ntnt.getExtras();
            Log.i(LOG_TAG, "extras --> " + xtrs.toString());
        }
        callPhone();
        synchronized(rcvr) {
            try {
                rcvr.wait(WAIT_TIME);
            } catch (InterruptedException e) {
                Log.w(LOG_TAG, "wait for phone call interrupted");
            }
        }
        assertFalse("Outgoing call processed without proper permissions", rcvr.callReceived);
    }
    public class OutgoingCallBroadcastReceiver extends BroadcastReceiver {
        public boolean callReceived = false;
        public void onReceive(Context context, Intent intent) {
            Bundle xtrs = intent.getExtras();
            Log.e(LOG_TAG, xtrs.toString());
            callReceived = true;
        }
    }
}
